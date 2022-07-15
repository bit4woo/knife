package burp;

import java.util.concurrent.BlockingQueue;

import com.github.kevinsawicki.http.HttpRequest;

public class threadRequester extends Thread {
	private final BlockingQueue<String> inputQueue;
	private String proxyHost;
	private int proxyPort;

	public threadRequester(BlockingQueue<String> inputQueue,String proxyHost,int proxyPort,int threadNo) {
		this.inputQueue = inputQueue;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.setName(this.getClass().getName()+threadNo);
	}

	@Override
	public void run() {
		while(true){
			try {
				if (inputQueue.isEmpty() ) {
					break;
				}
				if (Thread.interrupted()){//没有起作用！
					break;
				}

				String url = inputQueue.take();
				sendRequest(url,proxyHost,proxyPort);

			} catch (Exception error) {
				error.printStackTrace(BurpExtender.getStderr());
			}
		}
	}
	
	public static void sendRequest(String url,String proxyHost,int proxyPort) {
		HttpRequest request = HttpRequest.get(url);
		//Configure proxy
		request.useProxy(proxyHost, proxyPort);

		//Accept all certificates
		request.trustAllCerts();
		//Accept all hostnames
		request.trustAllHosts();

		request.code();


		HttpRequest postRequest = HttpRequest.post(url);
		//Configure proxy
		postRequest.useProxy(proxyHost, proxyPort);
		//Accept all certificates
		postRequest.trustAllCerts();
		//Accept all hostnames
		postRequest.trustAllHosts();

		postRequest.send("test=test");
		postRequest.code();

	}
	
	public static void main(String[] args) {
		sendRequest("http://127.0.0.1:8080","127.0.0.1",8080);
	}

}
