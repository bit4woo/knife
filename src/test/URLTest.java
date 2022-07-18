package test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class URLTest
{
   public static void main(String [] args)
   {
      try
      {
//         URL url = new URL("http://www.runoob.com/index.html?language=cn#j2se");
         URL url = new URL("www.runoob.com/index.html?language=cn#j2se");
//         url = new URL("http://127.0.0.1:5084/..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\..\\\\\\etc/passwd");
         System.out.println("URL 为：" + url.toString());
         System.out.println("协议为：" + url.getProtocol());
         System.out.println("验证信息：" + url.getAuthority());
         System.out.println("文件名及请求参数：" + url.getFile());
         System.out.println("主机名：" + url.getHost());
         System.out.println("路径：" + url.getPath());
         System.out.println("端口：" + url.getPort());
         System.out.println("默认端口：" + url.getDefaultPort());
         System.out.println("请求参数：" + url.getQuery());
         System.out.println("定位位置：" + url.getRef());
         
         String path = url.getFile();
         String camFile = new File(path).getCanonicalFile().toString();
         System.out.println(File.separator);
         camFile = camFile.substring(camFile.indexOf(File.separator));
         System.out.println(camFile);
         File fullName = new File(new File("e:\\aaaa"),camFile.toString());
         System.out.println(fullName);
         FileUtils.write(fullName, "111");
      }catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
