package burp;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import base.RequestTask;

public class threadRequester extends Thread {
	private final BlockingQueue<RequestTask> inputQueue;
	private String proxyHost;
	private int proxyPort;
	private HashMap<String,String> headers;

	public threadRequester(BlockingQueue<RequestTask> inputQueue,String proxyHost,int proxyPort,HashMap<String,String> headers,int threadNo) {
		this.inputQueue = inputQueue;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.headers = headers;
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

				RequestTask task = inputQueue.take();
				task.sendRequest(proxyHost,proxyPort,headers);
			} catch (Exception error) {
				error.printStackTrace(BurpExtender.getStderr());
			}
		}
	}

	public static void main(String[] args) {

	}

}
