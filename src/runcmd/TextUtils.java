package runcmd;

import org.apache.commons.lang3.StringEscapeUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    /**
     * 先解Unicode，再解url，应该才是正确操作吧
     *
     * @param line
     * @return
     */
    public static String decodeAll(String line) {
        line = line.trim();

		/*
		if (false) {// &#x URF-8编码的特征，对于域名的提取不需要对它进行处理
			while (true) {
				try {
					int oldlen = line.length();
					line = StringEscapeUtils.unescapeHtml4(line);
					int currentlen = line.length();
					if (oldlen > currentlen) {
						continue;
					}else {
						break;
					}
				}catch(Exception e) {
					//e.printStackTrace(BurpExtender.getStderr());
					break;//即使出错，也要进行后续的查找
				}
			}
		}
		 */

        if (needUnicodeConvert(line)) {
            while (true) {//unicode解码
                try {
                    int oldlen = line.length();
                    line = StringEscapeUtils.unescapeJava(line);
                    int currentlen = line.length();
                    if (oldlen > currentlen) {
                        continue;
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace(BurpExtender.getStderr());
                    break;//即使出错，也要进行后续的查找
                }
            }
        }

        if (needURLConvert(line)) {
            while (true) {
                try {
                    int oldlen = line.length();
                    line = URLDecoder.decode(line);
                    int currentlen = line.length();
                    if (oldlen > currentlen) {
                        continue;
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace(BurpExtender.getStderr());
                    break;//即使出错，也要进行后续的查找
                }
            }
        }

        return line;
    }


    public static boolean needUnicodeConvert(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        //Pattern pattern = Pattern.compile("(\\\\u([A-Fa-f0-9]{4}))");//和上面的效果一样
        Matcher matcher = pattern.matcher(str.toLowerCase());
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean needURLConvert(String str) {
        Pattern pattern = Pattern.compile("(%(\\p{XDigit}{2}))");

        Matcher matcher = pattern.matcher(str.toLowerCase());
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }


    public static List<String> grepChinese(String inputText) {
        // 使用正则表达式匹配中文字符
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
        Matcher matcher = pattern.matcher(inputText);

        // 提取匹配到的中文字符
        List<String> chineseCharacters = new ArrayList<String>();
        while (matcher.find()) {
            chineseCharacters.add(matcher.group());
        }
        return chineseCharacters;
    }


    /**
     * 提取两个字符串之间的内容
     *
     * @param input 输入字符串
     * @param start 开始标记
     * @param end   结束标记
     * @return 提取的字符串内容
     */
    public static List<String> grepBetween(String start, String end, String inputText) {
        // 使用正则表达式匹配中文字符
        String regex = Pattern.quote(start) + "(.*?)" + Pattern.quote(end);
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        // Pattern.DOTALL 支持在多行中匹配内容
        Matcher matcher = pattern.matcher(inputText);

        // 提取匹配到的中文字符
        List<String> result = new ArrayList<String>();
        while (matcher.find()) {
            result.add(matcher.group(0));
        }
        return result;
    }


    /**
     * 换行符的可能性有三种，都必须考虑到
     *
     * @param input
     * @return
     */
    public static List<String> textToLines(String input) {
        String[] lines = input.split("(\r\n|\r|\n)", -1);
        List<String> result = new ArrayList<String>();
        for (String line : lines) {
            line = line.trim();
            if (!line.equalsIgnoreCase("")) {
                result.add(line.trim());
            }
        }
        return result;
    }

    /**
     * 正则表达式提取
     *
     * @param text
     * @param regex
     * @param caseSensitive
     * @param multipleLine
     * @return
     */
    public static List<String> grepWithRegex(String text, String regex, boolean caseSensitive, boolean multipleLine) {
        List<String> result = new ArrayList<>();
        if (text == null || regex == null) {
            return result;
        }

        int flags = 0;
        if (!caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (multipleLine) {
            flags |= Pattern.DOTALL;
        }

        Pattern pRegex = Pattern.compile(regex, flags);
        Matcher matcher = pRegex.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }


    /**
     * 默认忽略大小写，单行匹配
     *
     * @param text
     * @param regex
     * @return
     */
    public static List<String> grepWithRegex(String text, String regex) {
        return grepWithRegex(text, regex, false, false);
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length());
        } else {
            return string;
        }
    }


    /**
     * 文本和正则是否匹配，主要用于格式校验，比如 isValidDomain,isValidEmail等等
     *
     * @param text
     * @param regex
     * @param caseSensitive
     * @param multipleLine
     * @return
     */
    public static boolean isRegexMatch(String text, String regex, boolean caseSensitive, boolean multipleLine) {
        if (null == text || null == regex) {
            return false;
        }

        int flags = 0;
        if (!caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (multipleLine) {
            flags |= Pattern.DOTALL;
        }

        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    public static Boolean isRegexMatch(String text, String regex) {
        return isRegexMatch(text, regex, false, false);
    }
    /**
     * 获取随机字符串
     *
     * @param length
     * @return
     */
    public static String getRandomStr(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        Random random = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = str.charAt(random.nextInt(str.length()));
        }
        return new String(text);
    }


    /**
     * 字符串转unicode
     *
     * @param str
     * @return
     */
    public static String stringToUnicode(String str) {
        StringBuffer sb = new StringBuffer();
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            sb.append("\\u" + Integer.toHexString(c[i]));
        }
        return sb.toString();
    }

    /**
     * unicode转字符串
     *
     * @param unicode
     * @return
     */
    public static String unicodeToString(String unicode) {
        StringBuffer sb = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int index = Integer.parseInt(hex[i], 16);
            sb.append((char) index);
        }
        return sb.toString();
    }


    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57) {
                return false;
            }
        }
        return true;
    }

    public static List<String> removePrefixAndSuffix(List<String> input, String Prefix, String Suffix) {
        ArrayList<String> result = new ArrayList<String>();
        if (Prefix == null && Suffix == null) {
            return result;
        } else {
            if (Prefix == null) {
                Prefix = "";
            }

            if (Suffix == null) {
                Suffix = "";
            }

            List<String> content = input;
            for (String item : content) {
                if (item.startsWith(Prefix)) {
                    //https://stackoverflow.com/questions/17225107/convert-java-string-to-string-compatible-with-a-regex-in-replaceall
                    String tmp = Pattern.quote(Prefix);//自动实现正则转义
                    item = item.replaceFirst(tmp, "");
                }
                if (item.endsWith(Suffix)) {
                    String tmp = Pattern.quote(reverse(Suffix));//自动实现正则转义
                    item = reverse(item).replaceFirst(tmp, "");
                    item = reverse(item);
                }
                result.add(item);
            }
            return result;
        }
    }

    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuffer(str).reverse().toString();
    }

    public static List<Integer> allIndexesOf(String word, String guess) {
        List<Integer> result = new ArrayList<Integer>();
        int index = word.indexOf(guess);
        while (index >= 0) {
            result.add(index);
            index = word.indexOf(guess, index + 1);
        }
        return result;
    }

}
