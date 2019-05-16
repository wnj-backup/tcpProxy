package com.gd.test.server.bio;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 
 * @author pt-weinj
 *
 */
public class GdHandler extends Thread {

	private Socket s = null;
	
	public GdHandler(Socket s){
		this.s = s;
	}
	
	public void run(){
		try {
			System.out.println("接收到连接请求：" + s);
			InputStream in = s.getInputStream();
			byte[] buf = new byte[6];
			if(in.read(buf) == 6) {
				// 同步响应，头6个字节即消息长度的ascii码
				int len = Integer.parseInt(new String(buf));
				buf = new byte[len];
				in.read(buf);// 消息不大，一口吃完
				String resp = new String(buf, "gbk");// 得到的resp有可能带有mac校验值
				String mac = resp.substring(resp.lastIndexOf(">") + 1);
				String xml = resp.substring(0, resp.lastIndexOf(">") + 1);
				System.out.println(mac + "|" + xml);
				
				OutputStream out = s.getOutputStream();
				out.write("000014<xml>123</xml>".getBytes());
				out.flush();
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
}
