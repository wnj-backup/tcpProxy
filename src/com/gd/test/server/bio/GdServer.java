package com.gd.test.server.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO模式的服务端
 */
public class GdServer {
	
	/** 服务器实例 */
	private static ServerSocket ss;
	
	/** 连接池 */
	private static ExecutorService pool;
	
	/** 端口 */
	private static final int port = 20168;
	
	/**
	 * 启动服务器
	 */
	public static void main(String[] args) {
		new Thread(){
			@Override
			public void run() {
				System.out.println("服务器启动，端口：" + port);
				if(ss == null) {
					try {
						pool = Executors.newFixedThreadPool(30);
						ss = new ServerSocket(port);
						while(true){
							Socket s = ss.accept();
							pool.submit(new GdHandler(s));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	/**
	 * 关闭服务器
	 */

	public void close(){
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
