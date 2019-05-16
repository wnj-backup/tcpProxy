package com.gd.test.server.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerHandler {
	
	private static final String ENCODING = "gbk";
	private static final String keyPath = "E:/workspace/keys/";// 需要动态
	private SelectionKey key;
	private Selector selector;
	
	public ServerHandler(SelectionKey key, Selector selector) {
		this.key = key;
		this.selector = selector;
		
		if(key.isAcceptable()) {
			doAcceptEvent();
			return;
		}
		if(key.isReadable()) {
			doReadEvent();
			return;
		}
		if(key.isWritable()) {
			doWriteEvent();
			return;
		}
	}
	
	
	// 连接事件
	private void doAcceptEvent() {
		System.out.println("请求接入...");
		SocketChannel client = null;
		ServerSocketChannel server = null;
		try {
			
			server = (ServerSocketChannel)key.channel();
			client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			
			System.out.println("client=" + client.socket());
			
		}catch(Exception e) {
			try {
				client.close();
			}catch(Exception e1) {
				System.out.println(e1);
			}
		}
	}

	// 读事件
	private void doReadEvent() {
		SocketChannel socketChannel = null;
		try {
			
			socketChannel = (SocketChannel)key.channel();
			ByteBuffer buf = ByteBuffer.allocate(6);
			buf.clear();
			if(socketChannel.read(buf) > 0) {
				int len = Integer.parseInt(new String(buf.array()));
				buf.flip();
				if(len > 0) {
					buf = ByteBuffer.allocate(len);
					buf.clear();
					socketChannel.read(buf);
					String msg = new String(buf.array(), ENCODING);
					
					String mac = msg.substring(msg.lastIndexOf(">") + 1);
					String xml = msg.substring(0, msg.lastIndexOf(">") + 1);
					System.out.println("xml=" + xml + ",mac=" + mac);
					
					
					// XML解析
					if(xml != null && !xml.equals("")) {
						try {
							if(xml.indexOf("XXXXXXRes") != -1) {
								System.out.println("识别出响应报文是：密钥申请响应");
								String pinKey = getXmlFiledValue(xml, "keyValue");
								String pinVerify = getXmlFiledValue(xml, "verifyValue");
								String macKey = getXmlFiledValue(xml, "keyValue1");
								String macVerify = getXmlFiledValue(xml, "verifyValue1");
								System.out.println(keyPath + pinKey + pinVerify + macKey + macVerify);// 已删除解密代码，保留打印
							}else if (xml.indexOf("XXXXXXRes") != -1) {
								System.out.println("识别出响应报文是：缴费单查询响应");
							}else if (xml.indexOf("Error") != -1) {
								System.out.println("识别出响应报文是：错误报文");
							}else{
								System.out.println("识别出响应报文是：其他响应报文");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				// 短连接处理模式
				if(socketChannel.isOpen()) {
					socketChannel.close();
				}
			}catch(Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	// 写事件
	private void doWriteEvent() {
		// 不需响应，什么也不用做
	}
	
	// 高性能解析出终端流水号
	public String getXmlFiledValue(String xml, String filed) {
		int begin = xml.indexOf("<" + filed + ">") + ("<" + filed + ">").length();
		int end = xml.indexOf("</" + filed + ">");
		String rs = xml.substring(begin, end);
		return rs;
	}
}
