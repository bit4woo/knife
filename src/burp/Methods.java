/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

/**
 *
 * @author abdul.wahab
 */
public class Methods {
    
    public static JMenu add_MenuItem_and_listener(JMenu menu, String[] itemList, Object actionListener){
        for(int i = 0; i < itemList.length; i++){
            JMenuItem item = new JMenuItem(itemList[i]);
            item.addActionListener((ActionListener) actionListener);
            menu.add(item);
        }
        return menu;
    }
    
    public static JMenu Create_Main_Menu(JMenu MainMenu, String[] MainMenuItems, String[][] SubMenuItems, Object actionListener){
        for(int i=0; i < MainMenuItems.length; i++){
            JMenu menu = new JMenu(MainMenuItems[i]);
            menu = add_MenuItem_and_listener(menu, SubMenuItems[i], actionListener);
            if(MainMenuItems[i].equals("Basic Statements")){
                MainMenu.add(new JSeparator());
                MainMenu.add(menu);
                MainMenu.add(new JSeparator());
            }else{
                MainMenu.add(menu);
            }
        }
        return MainMenu;
    }
    
    
    public static String prompt_and_validate_input(String prompt, String str){
        String user_input = JOptionPane.showInputDialog(prompt, str);
        while(user_input.trim().equals("")){
            user_input = JOptionPane.showInputDialog(prompt, str);
        }
        return user_input.trim();
    }
    
//    public static byte[] do_modify_request(byte[] request, int[] selectedIndex, String modifiedString){
//        byte[] modString = modifiedString.getBytes();
//        byte[] newRequest = new byte[request.length + modifiedString.length() - (selectedIndex[1]-selectedIndex[0])];
//        System.arraycopy(request, 0, newRequest, 0, selectedIndex[0]);
//        System.arraycopy(modString, 0, newRequest, selectedIndex[0], modString.length);
//        System.arraycopy(request, selectedIndex[1], newRequest, selectedIndex[0]+modString.length, request.length-selectedIndex[1]);
//        return newRequest;
//    }
    
    public static byte[] do_modify_request(byte[] request, int[] selectedIndex, byte[] payloadByte){
    	
 /*       byte[] preSelectedPortion = Arrays.copyOfRange(request, 0, selectedIndex[0]);
		byte[] postSelectedPortion = Arrays.copyOfRange(request, selectedIndex[1], request.length);
	
        byte[] newRequestResponseBytes = ArrayUtils.addAll(preSelectedPortion, modifiedString);
		newRequestResponseBytes = ArrayUtils.addAll(newRequestResponseBytes, postSelectedPortion);
		// same as below method
		 * */
		if (payloadByte.equals(null)){
			return request;
		}
        byte[] newRequest = new byte[request.length + payloadByte.length - (selectedIndex[1]-selectedIndex[0])];
        System.arraycopy(request, 0, newRequest, 0, selectedIndex[0]);//选中位置的前面部分
        System.arraycopy(payloadByte, 0, newRequest, selectedIndex[0], payloadByte.length);//新的内容替换选中内容
        System.arraycopy(request, selectedIndex[1], newRequest, selectedIndex[0]+payloadByte.length, request.length-selectedIndex[1]);//选中位置的后面部分
        return newRequest;
    }
    
    public static void show_message(String str1, String str2){
        JOptionPane.showMessageDialog(null, str1, str2, 0);
    }
    
//    public static byte[] getContent(String filePath) throws Exception{
//    	File f = new File(filePath);
//		if (!f.exists()) {
//			return null;
//		}
//		byte[] payloadByte = Files.readAllBytes(f.toPath());
//		return payloadByte;
//    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length 指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length 指定长度
     * @param size 指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f 开始位置
     * @param t 结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    /**
     * 获取随机字符串
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        Random random = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = str.charAt(random.nextInt(str.length()));
        }
        return new String(text);
    }

    /**
     * 将10进制转换为16进制
     * @param decimal 10进制
     * @return 16进制
     */
    public static String decimalToHex(int decimal) {
        String hex = Integer.toHexString(decimal);
        return  hex.toUpperCase();
    }
    
    

	public static  byte[] encoding(byte[] body,int len,boolean useComment) throws UnsupportedEncodingException {
        String bodyString = new String(body, "UTF-8");

        List<String> str_list = Methods.getStrList(bodyString,len);
        String encoding_body = "";
        for(String str:str_list){
            if(useComment){
                encoding_body += String.format("%s;%s",Methods.decimalToHex(str.length()),Methods.getRandomString(10));
            }else{
                encoding_body += Methods.decimalToHex(str.length());
            }
            encoding_body += "\r\n";
            encoding_body += str;
            encoding_body += "\r\n";
        }
        encoding_body += "0\r\n\r\n";
        
        return encoding_body.getBytes();
    }

    public static byte[] decoding(byte[] body) throws UnsupportedEncodingException {
        String bodyStr = new String(body, "UTF-8");

        // decoding
        String[] array_body = bodyStr.split("\r\n");
        List<String> list_string_body = Arrays.asList(array_body);
        List<String> list_body = new ArrayList<String>(list_string_body);
        list_body.remove(list_body.size()-1);
        String decoding_body = "";
        for(int i=0;i<list_body.size();i++){
            int n = i%2;
            if(n != 0){
                decoding_body += list_body.get(i);
            }
        }

        return decoding_body.getBytes();
    }

    
    public static void main(String args[]) {
    	String a = "aaaa";
    	System.out.println(a.length());
    	System.out.println(a.getBytes().length);
    }
}
