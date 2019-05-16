package com.gd.test.server.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * NIO模式的服务端
 */
public class NIOServer  implements Runnable{

	private ServerSocketChannel socketChannel;
	private Selector selector;
	private final int port = 20168;
	
	public NIOServer() {
		try {
			
			socketChannel = ServerSocketChannel.open();
			selector = Selector.open();
			ServerSocket socket = socketChannel.socket();
			socket.bind(new InetSocketAddress(port));
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void run() {
		System.out.println("服务器启动，监听：" + port);
		while(true) {
			try {
				selector.select();
				Iterator<SelectionKey> itor = selector.selectedKeys().iterator();
				while(itor.hasNext()) {
					SelectionKey key = itor.next();
					itor.remove();
					new ServerHandler(key, selector);
				}				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new NIOServer()).start();
	}
}
