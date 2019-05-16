package com.gd.test.client;

import java.io.IOException;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 客户端测试工具
 * @author pt-weinj 2016-07-14
 */
public class GdPayTool {
	
	private static final int connetTimeout = 3000;
	private static final int readTimeout = 60000;
	private static final int readInterval = 1000;
	private static final String ENCODING = "ISO-8859-1";
	private static final String instId = "100000000000000";// 需要动态
	private static final String partnerCode = "111";// 需要动态
	private static final String host = "10.248.95.48";// 需要动态
	private static final int port = 7778;// 需要动态

	
	private static long counter = 1000000000l;
	
	/**
	 * 申请密钥
	 */
	public static String applyKey(String partnerCode, String operationDate) {
		
		String msg = "<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>" +
				"<in>" +
				"<head>" +
				"<Version>1.0.1</Version>" +
				"<InstId>" + instId + "</InstId>" +
				"<AnsTranCode>XXXXXXXReq</AnsTranCode>" +
				"<TrmSeqNum>" + getTrmSeqNum() + "</TrmSeqNum>" +
				"</head>" +
				"<tin>" +
				"<partnerCode>" + partnerCode + "</partnerCode>" +
				"<operationDate>" + operationDate + "</operationDate>" +
				"</tin>" +
				"</in>";
		msg = getMsgLenth(msg) + msg;
		System.out.println("发送请求：" + msg);
		String result = sendTcp(msg);
		System.out.println("接收响应：" + result);
		
		//TODO xml解析
		
		return null;
	}
	
	/**
	 * 查单
	 */
	public static void queryBill() {
		String msg = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
				"<in>" +
				"<head>" +
				"<Version>1.0.1</Version>" +
				"<InstId>" + instId + "</InstId>" +
				"<AnsTranCode>XXXXXXReq</AnsTranCode>" +
				"<TrmSeqNum>" + getTrmSeqNum() + "</TrmSeqNum>" +
				"</head>" +
				"<tin>" +
				"<billKey>033920120612872800111</billKey>" +
				"<companyId>021009014</companyId>" +
				"<beginNum>1</beginNum>" +
				"<queryNum>1</queryNum>" +
				"<filed1></filed1>" +
				"<filed2>1</filed2>" +
				"<filed3></filed3>" +
				"<filed4></filed4>" +
				"</tin>" +
				"</in>";

		msg = getMsgLenth(msg) + msg;
		System.out.println("发送请求：" + msg);
		String result = sendTcp(msg);
		System.out.println("接收响应：" + result);
		
	}

	/**
	 * 销账
	 */
	public static void payBill() {
		
	}
	
	/**
	 * 发送TCP数据（短连接）
	 */
	private static String sendTcp(String msg) {
		String rt = null;
		Socket socket = null;
		try {
			System.out.println("host=" + host + ",port=" + port);
			socket = new Socket();
			SocketAddress addr = new InetSocketAddress(host, port);
			socket.connect(addr, connetTimeout);
			
			OutputStream out = socket.getOutputStream();
			out.write(msg.getBytes("gbk"));
			out.flush();
			
			// 异步从数据库读（无缓存工具，直接使用数据库）
			boolean isFind = false;
			long start = System.currentTimeMillis();
			while(System.currentTimeMillis() - start < readTimeout) {
				// TODO 尝试从数据库中获取相应
				
				Thread.sleep(readInterval);
			}
			
			// TODO 如果没匹配到，如何返回？
			if(!isFind) {
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return rt;
	}
	
	/**
	 * 计算消息长度
	 */
	private static String getMsgLenth(String msg) {
		String len = String.valueOf(msg.length());
		for(int i=len.length(); i < 6 ; i++) {
			len = "0" + len;
		}
		return len;
	}
	
	/**
	 * 获得消息流水号 
	 * TODO 通过ORACLE序列实现？
	 */
	private static synchronized String getTrmSeqNum() {
		return System.currentTimeMillis() + "" + counter++;
	}
	
	// 高性能解析出终端流水号
	public static String getXmlFiledValue(String xml, String filed) {
		int begin = xml.indexOf("<" + filed + ">") + ("<" + filed + ">").length();
		int end = xml.indexOf("</" + filed + ">");
		String rs = xml.substring(begin, end);
		return rs;
	}
	
	public static void main(String[] args) throws Exception {
		
		if(args != null && args.length > 0) {
			if(args[0].equals("1")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				applyKey(partnerCode, sdf.format(new Date()));
			}else if(args[0].equals("2")) {

			}else {
				queryBill();
			}

		}else {
			queryBill();
		}
	}
}
