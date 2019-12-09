package org.opcfoundaiton.ua;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;


public class Ipv6Test {

	    static public void main(String[] notUsed) {
	        try {
	            byte[] addr = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	1};

	            InetAddress inetAddr = InetAddress.getByAddress(addr);

	            InetSocketAddress addr_8501 = new
	InetSocketAddress(inetAddr, 8501);
	            InetSocketAddress addr_8502 = new
	InetSocketAddress(inetAddr, 8502);

	            System.out.println("==> 1");

	            ServerSocket serverSocket = new ServerSocket();
	            serverSocket.bind(addr_8501); // This works

	            System.out.println("==> 2");

	            ServerSocketChannel channel = ServerSocketChannel.open();
	            channel.socket().bind(addr_8502); //This does not work

	            System.out.println("==> 3");
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }
	    }
	
}
