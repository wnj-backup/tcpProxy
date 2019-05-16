package com.gd.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class ProxyHandler extends Thread {

	private static Logger log = Logger.getLogger(TcpProxy.class.getName());

	private static final int timeout = 3000;
	private Socket s = null;
	private String host = null;
	private int port = 0;
	
	public ProxyHandler(Socket s, String host, int port){
		this.s = s;
		this.host = host;
		this.port = port;
	}
	
	public void run(){
		try {
			
			log.info("接收到连接请求：" + s + "dest ip=" + host + ",port=" + port);
			
			InputStream in = s.getInputStream();
			int len = 0;
			byte[] bufHead = new byte[6];
			if(in.read(bufHead) == 6) {
                                // 同步响应，头6个字节即消息长度的ascii码
                                len = Integer.parseInt(new String(bufHead));
                                byte[] buf = new byte[len];

                                //in.read(buf);// 消息不大，一口吃完 // 不行，可能只读取到一部分

                                // 新的读取方式
                                int readLen = 0;
                                while(readLen < len){
                                        readLen += in.read(buf, readLen, len - readLen);
                                }

                                String resp = new String(buf, "gbk");// 得到的resp有可能带有mac校验值
				String mac = resp.substring(resp.lastIndexOf(">") + 1);
				String xml = resp.substring(0, resp.lastIndexOf(">") + 1);
				log.info(xml + "|" + mac);
				
				//---------------------下面开始转发--------------------------
				log.info("开始转发");
				
				byte[] sendBuf = mergeBytes(bufHead, buf);
				
				byte[] data = sendTcp(sendBuf);
				if(data != null)
				{
					OutputStream out = s.getOutputStream();
					out.write(data);
					out.flush();
				}

				log.info("代理结束");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(!s.isClosed()){
					s.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	// 发送TCP报文
	private byte[] sendTcp(byte[] data) {
		byte[] rt = null;
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress addr = new InetSocketAddress(host, port);
			socket.connect(addr, timeout);
			
			OutputStream out = socket.getOutputStream();
			out.write(data);
			out.flush();

			// 暂时不读取响应了
			/*
			Thread.sleep(100l);

			InputStream in = socket.getInputStream();
			int len = 0;
			byte[] bufHead = new byte[6];
			if(in.read(bufHead) == 6) {
				// 同步响应，头6个字节即消息长度的ascii码
				len = Integer.parseInt(new String(bufHead));
				byte[] buf = new byte[len];
				in.read(buf);// 消息不大，一口吃完
				String resp = new String(buf, "gbk");// 得到的resp有可能带有mac校验值
				String mac = resp.substring(resp.lastIndexOf(">") + 1);
				String xml = resp.substring(0, resp.lastIndexOf(">") + 1);
				System.out.println(xml + "|" + mac);

				rt = mergeBytes(bufHead, buf);

			}

			log.info("received msg length:" + len);*/

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
	
	// 合并字节数组
	private byte[] mergeBytes(byte[] buf1, byte[] buf2) {
		byte[] buf = new byte[buf1.length + buf2.length];
		int index = 0;
		for(byte b:buf1) {
			buf[index++] = b;
		}
		for(byte b:buf2) {
			buf[index++] = b;
		}
		return buf;
	}
}
