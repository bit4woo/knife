package burp;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.File;

//https://bbs.51cto.com/thread-1097189-1.html
public class RobotInput extends Robot {

	public RobotInput() throws AWTException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws AWTException
	 */
	public static void main(String[] args) throws Exception {
		test();
	}

	public static void test() throws Exception {
		// TODO Auto-generated method stub
		RobotInput robot = new RobotInput(); //创建一个robot对象
		Runtime.getRuntime().exec("cmd /c start cmd.exe");
		robot.delay(2000);        //等待 2秒

		robot.inputWithAlt(KeyEvent.VK_SPACE); //按下 alt+ 空格  //窗口最大化
		robot.keyPress( KeyEvent.VK_X);  //按下x键
		robot.delay(1000);  //等待 1秒
		robot.inputString( "大家好，我是一个小机器人，我有很多本领呢 ！"); //输入字符串
		robot.delay(3000);  //等待 3秒
		robot.keyPress( KeyEvent.VK_ENTER); // 按下 enter 换行

		robot.keyPress( KeyEvent.VK_A); //按下 a 键
		robot.delay(2000);  //等待 2秒

		robot.inputWithCtrl(KeyEvent.VK_A); //按下 ctrl+A 全选
		robot.delay(2000);  //等待 2秒
		robot.keyPress(KeyEvent.VK_DELETE); //清除
		robot.delay(3000);  //等待 3秒

		robot.inputString( "谢谢大家！！！！！");
	}

	// shift+ 按键
	public void inputWithShift(int key) {
		delay(100);
		keyPress(KeyEvent.VK_SHIFT);
		keyPress(key);
		keyRelease(key);
		keyRelease(KeyEvent.VK_SHIFT);
		delay(100);
	}

	// ctrl+ 按键
	public void inputWithCtrl(int key) {
		delay(100);
		keyPress(KeyEvent.VK_CONTROL);
		keyPress(key);
		keyRelease(key);
		keyRelease(KeyEvent.VK_CONTROL);
		delay(100);
	}

	// alt+ 按键
	public void inputWithAlt(int key) {
		delay(100);
		keyPress(KeyEvent.VK_ALT);
		keyPress(key);
		keyRelease(key);
		keyRelease(KeyEvent.VK_ALT);
		delay(100);
	}
	
	// ctrl+shift+ 按键
	public void inputWithCtrlAndShift(int key) {
		delay(100);
		keyPress(KeyEvent.VK_CONTROL);
		keyPress(KeyEvent.VK_SHIFT);
		keyPress(key);
		keyRelease(key);
		keyRelease(KeyEvent.VK_SHIFT);
		keyRelease(KeyEvent.VK_CONTROL);
		delay(100);
	}

	//输入字符串
	//win7下的cmd中crtl+v是不行的
	@Deprecated
	private void inputStringOld(String str){
		delay(100);
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();//获取剪切板
		Transferable origin = clip.getContents(null);//备份之前剪切板的内容
		Transferable tText = new StringSelection(str);
		clip.setContents(tText, null); //设置剪切板内容
		inputWithCtrl(KeyEvent.VK_V);//粘贴
		//delay(100);
		clip.setContents(origin, null);//恢复之前剪切板的内容
		delay(100);
	}

	//输入字符串
	//https://stackoverflow.com/questions/29665534/type-a-string-using-java-awt-robot
	//这种方法也有缺陷，当输入法不是英文的状态下有问题。
	@Deprecated
	private void inputStringOld1(String str){
		delay(100);
		for (char c : str.toCharArray()) {
			int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
			input(keyCode);
		}
		delay(100);
	}
	private void input(int key) {
		keyPress(key);
		keyRelease(key);
	}

	public void inputString(String str){
		delay(100);
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();//获取剪切板
		Transferable origin = clip.getContents(null);//备份之前剪切板的内容
		Transferable tText = new StringSelection(str);
		clip.setContents(tText, null); //设置剪切板内容
		
		if (Utils.isWindows10()) {//粘贴的不同实现方式
			inputWithCtrl(KeyEvent.VK_V);
		}else if (Utils.isWindows()) {
			inputWithAlt(KeyEvent.VK_SPACE);//
			InputChar(KeyEvent.VK_E);
			InputChar(KeyEvent.VK_P);
		}else if (Utils.isMac()) {
			keyPress(KeyEvent.VK_META);
			keyPress(KeyEvent.VK_C);
			keyRelease(KeyEvent.VK_C);
			keyRelease(KeyEvent.VK_META);
		}else if (Utils.isUnix()) {//Ctrl + Shift + V
			inputWithCtrlAndShift(KeyEvent.VK_C);
		}

		//delay(100);
		clip.setContents(origin, null);//恢复之前剪切板的内容
		delay(100);
	}
	
	//单个 按键

	public void InputChar(int key){
		delay(100);
		keyPress(key);
		keyRelease(key);
		delay(100);
	}

	public static void startCmdConsole() {
		try {
			Process process = null;
			if (Utils.isWindows()) {
				process = Runtime.getRuntime().exec("cmd /c start cmd.exe");
			} else {
				if (new File("/bin/sh").exists()) {
					Runtime.getRuntime().exec("/bin/sh");
				}else if (new File("/bin/bash").exists()) {
					Runtime.getRuntime().exec("/bin/bash");
				}
			}
			process.waitFor();//等待执行完成

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}