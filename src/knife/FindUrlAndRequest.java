package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import base.RequestTask;
import base.RequestType;
import burp.*;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.SystemUtils;
import com.bit4woo.utilbox.utils.UrlUtils;


public class FindUrlAndRequest extends JMenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //JMenuItem vs. JMenu
    public FindUrlAndRequest(BurpExtender burp) {
        this.setText("^_^ Find URL And Request");
        this.addActionListener(new FindUrl_Action(burp, burp.invocation));
    }

    public static void main(String[] args) {
        String url = "./abac/aaa.jpg";
        if (url.startsWith("./")) {
            url = url.replaceFirst("\\./", "");
        }
        System.out.println(url);
    }
}

class FindUrl_Action implements ActionListener {
    private IContextMenuInvocation invocation;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    public BurpExtender burp;
    public static final String[] blackHostList = {"www.w3.org", "ns.adobe.com", "iptc.org", "openoffice.org"
            , "schemas.microsoft.com", "schemas.openxmlformats.org", "sheetjs.openxmlformats.org"};

    public FindUrl_Action(BurpExtender burp, IContextMenuInvocation invocation) {
        this.burp = burp;
        this.invocation = invocation;
        this.helpers = burp.helpers;
        this.callbacks = BurpExtender.callbacks;
        this.stderr = BurpExtender.stderr;
        this.stdout = BurpExtender.stdout;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Runnable requestRunner = new Runnable() {
            String siteBaseUrl = null;
            Set<String> baseUrls = new HashSet<String>();
            List<String> urls = new ArrayList<String>();

            @Override
            public void run() {
                try {
                    IHttpRequestResponse[] messages = invocation.getSelectedMessages();
                    if (messages == null || messages.length <= 0) {
                        return;
                    }

                    BlockingQueue<RequestTask> inputQueue = new LinkedBlockingQueue<RequestTask>();

                    try {
                        findUrls(messages[0]);

                        if (baseUrls.size() <= 0) {
                            return;
                        }
                        String baseurl = choseAndEditBaseURL(baseUrls);

                        if (null == baseurl) {
                            return;
                        }

                        for (String url : urls) {
                            if (UrlUtils.uselessExtension(url)) {
                                continue;
                            }
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                if (url.startsWith("/")) {
                                    url = url.replaceFirst("/", "");
                                }
                                if (url.startsWith("./")) {
                                    url = url.replaceFirst("\\./", "");
                                }
                                url = baseurl + url; //baseurl统一以“/”结尾；url统一删除“/”的开头
                                inputQueue.put(new RequestTask(url, RequestType.GET));
                                inputQueue.put(new RequestTask(url, RequestType.POST));
                                inputQueue.put(new RequestTask(url, RequestType.JSON));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(BurpExtender.getStderr());
                    }

                    doRequest(inputQueue, siteBaseUrl);
                } catch (Exception e1) {
                    e1.printStackTrace(stderr);
                }
            }


            /**
             * 根据当前web的baseUrl找JS，特征就是referer以它开头
             * @param currentBaseUrl
             * @return
             */
            public void findUrls(IHttpRequestResponse message) {
                HelperPlus getter = new HelperPlus(helpers);

                String current_referUrl = getter.getHeaderValueOf(true, message, "Referer");
                String current_fullUrl = getter.getFullURL(message).toString();

                if (current_referUrl != null) {
                    baseUrls.add(current_referUrl);
                }
                baseUrls.add(current_fullUrl);

                if (current_fullUrl != null) {
                    try {
						siteBaseUrl = new UrlUtils(current_referUrl).getBaseURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
                }
                if (siteBaseUrl == null) {
                    try {
						siteBaseUrl = new UrlUtils(current_fullUrl).getBaseURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
                }


                IHttpRequestResponse[] messages = BurpExtender.getCallbacks().getSiteMap(null);
                for (IHttpRequestResponse item : messages) {
                    int code = getter.getStatusCode(item);
                    URL url = getter.getFullURL(item);
                    String referUrl = getter.getHeaderValueOf(true, item, "Referer");
                    if (referUrl == null || url == null || code <= 0) {
                        continue;
                    }
                    if (!url.toString().toLowerCase().endsWith(".js")) {
                        continue;
                    }
                    if (referUrl.toLowerCase().startsWith(siteBaseUrl.toLowerCase() + "/")) {
                        byte[] respBody = getter.getBody(false, item);
                        String body = new String(respBody);
                        urls.addAll(UrlUtils.grepUrls(body));
                        baseUrls.addAll(findPossibleBaseURL(urls));
                    }
                }
            }

        };
        new Thread(requestRunner).start();
    }


    /**
     * 多线程执行请求
     *
     * @param inputQueue
     */
    public void doRequest(BlockingQueue<RequestTask> inputQueue, String referUrl) {
        Proxy proxy = Proxy.inputProxy();
        if (proxy == null) {
            return;
        }

        int max = threadNumberShouldUse(inputQueue.size());

        for (int i = 0; i <= max; i++) {
            threadRequester requester = new threadRequester(inputQueue, proxy.getHost(), proxy.getPort(), referUrl, i);
            requester.start();
        }
    }

    /**
     * 根据已有的域名梳理，预估应该使用的线程数
     * 假设1个任务需要1秒钟。线程数在1-100之间，如何选择线程数使用最小的时间？
     *
     * @param domains
     * @return
     */
    public static int threadNumberShouldUse(int domainNum) {

        int tmp = (int) Math.sqrt(domainNum);
        if (tmp <= 1) {
            return 1;
        } else if (tmp >= 10) {
            return 10;
        } else {
            return tmp;
        }
    }

    public static Set<String> findPossibleBaseURL(List<String> urls) {
        Set<String> baseURLs = new HashSet<String>();
        for (String tmpurl : urls) {
            //这部分提取的是含有协议头的完整URL地址
            if (tmpurl.toLowerCase().startsWith("http://")
                    || tmpurl.toLowerCase().startsWith("https://")) {

                try {
                    String host = new URL(tmpurl).getHost();
                    if (Arrays.asList(blackHostList).contains(host)) {
                        continue;
                    }
                } catch (Exception E) {
                    continue;
                }

                baseURLs.add(tmpurl);
            }
        }
        return baseURLs;
    }

    public static String choseAndEditBaseURL(Set<String> inputs) {

        ArrayList<String> tmpList = new ArrayList<String>(inputs);
        Collections.sort(tmpList);
        int n = inputs.size() + 1;
        String[] possibleValues = new String[n];

        // Copying contents of domains to arr[]
        System.arraycopy(tmpList.toArray(), 0, possibleValues, 0, n - 1);
        possibleValues[n - 1] = "Let Me Input";

        String selectedValue = (String) JOptionPane.showInputDialog(null,
                "Chose Base URL", "Chose And Edit Base URL",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);
        if (null != selectedValue) {
            String baseUrl = JOptionPane.showInputDialog("Confirm The Base URL", selectedValue);
            if (baseUrl == null) {
                return null;
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl.trim() + "/";
            }
            return baseUrl.trim();
        }
        return selectedValue;
    }
}
