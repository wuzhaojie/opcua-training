import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.ServerListException;


public class MainProgram {

	public static void main(String[] args) throws IOException, ServerListException, InvalidServerEndpointException, ConnectException, SecureIdentityException, ServiceException, URISyntaxException, StatusException {
		
		OpcUaClient client = new OpcUaClient();
		
		if (client.connect())
		{
			System.out.println("连接到 " + client.getServerUrl());
		} else {
			System.out.println("无法连接到 " + client.getServerUrl());	
			return;
		}
		
		Menu(client);
	}

	private static void Menu(OpcUaClient client) throws IOException, ServiceException, StatusException
	{	
		char userCommand = ' ';
	
		do 
		{			
			userCommand = PrintMemu();
			
			if (userCommand == '1')
			{
				System.out.println("浏览列表");
					
				client.browse();
			}
				
			if (userCommand == '2')
			{
				System.out.println("读取结果");
					
				client.read();
			}
				
			if (userCommand == '3')
			{
				System.out.println("执行写操作");
				
				client.write();
			}
		} while (userCommand != 'x'); 		
		
		client.disconnect();
		
		System.out.println("断开服务器 " + client.getServerUrl());	
	}
	
	public static char PrintMemu() throws IOException
	{
		System.out.println();
		System.out.println("1. 浏览地址空间");
		System.out.println("2. 读数据");
		System.out.println("3. 写数据");
		System.out.println("x. 断开服务器");
		
		String userInput = readLine();
		
		char userCommand = ' ';
		
		if (userInput.length() > 0)
		{
			userCommand = userInput.charAt(0);
		}
		
		return userCommand;
	}
	
	public static String readLine() throws IOException
	{
		if (null == s_readStream)
		{
			s_readStream = new InputStreamReader(System.in);
		}
		
		if (null == s_readBuffer)
		{
			s_readBuffer =  new BufferedReader(s_readStream);
		}
		
		return s_readBuffer.readLine();
	}
	
	private static BufferedReader s_readBuffer;
	private static InputStreamReader s_readStream;	
}
