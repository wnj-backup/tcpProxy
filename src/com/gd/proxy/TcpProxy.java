package com.gd.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * TCP代理服务器，4端口映射
 * @author pt-weinj 2016-07-14
 */
public class TcpProxy {

	private static Logger log = Logger.getLogger(TcpProxy.class.getName());

	/** 服务器实例 */
	private static ServerSocket ss1;// 入
	private static ServerSocket ss2;// 出

	/** 连接池 */
	private static ExecutorService pool1;// 入
	private static ExecutorService pool2;// 出

	/** 配置1 */
	private static final String host1 = "10.198.249.30";// 入
	//private static final String host1 = "10.245.45.89";// 入
	private static final int port1 = 20168;// 入
	private static final int port1_1 = 20168;// 入
	private static final String host2 = "128.9.6.183";// 出
	//private static final int port2 = 7778;// 出
        private static final int port2 = 20166;// 出
        private static final int port2_2 = 7778;// 出
	
	/**
	 * 启动服务器
	 */
	public static void main(String[] args) throws IOException {

		// 初始化日志工具
		System.setProperty("user.timezone","GMT+8");
//		FileHandler fileHandler = new FileHandler("./tcpProxy.log", true);
		FileHandler fileHandler = new FileHandler("E:/tcpProxy.log", true);
		fileHandler.setLevel(Level.INFO);
		fileHandler.setFormatter(new Formatter() {
			private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			public String format(LogRecord record) {
				return sdf.format(new Date())  + " : " + record.getMessage() + "\n";
			}
		});
		log.addHandler(fileHandler);

		new Thread(){
			@Override
			public void run() {

				log.info("入口服务器启动，端口：" + port1);
				if(ss1 == null) {
					try {
						pool1 = Executors.newFixedThreadPool(30);
						ss1 = new ServerSocket(port1);
						while(true){
							Socket s = ss1.accept();
							pool1.submit(new ProxyHandler(s, host1, port1_1));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		new Thread(){
			@Override
			public void run() {
				log.info("出口服务器启动，端口：" + port2);
				if(ss2 == null) {
					try {
						pool2 = Executors.newFixedThreadPool(30);
						ss2 = new ServerSocket(port2);
						while(true){
							Socket s = ss2.accept();
							pool2.submit(new ProxyHandler(s, host2, port2_2));
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
			ss1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ss2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
