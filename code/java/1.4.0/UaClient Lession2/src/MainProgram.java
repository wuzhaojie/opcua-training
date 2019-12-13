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
			System.out.println("���ӵ� " + client.getServerUrl());
		} else {
			System.out.println("�޷����ӵ� " + client.getServerUrl());	
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
				System.out.println("����б�");
					
				client.browse();
			}
				
			if (userCommand == '2')
			{
				System.out.println("��ȡ���");
					
				client.read();
			}
				
			if (userCommand == '3')
			{
				System.out.println("ִ��д����");
				
				client.write();
			}
		} while (userCommand != 'x'); 		
		
		client.disconnect();
		
		System.out.println("�Ͽ������� " + client.getServerUrl());	
	}
	
	public static char PrintMemu() throws IOException
	{
		System.out.println();
		System.out.println("1. �����ַ�ռ�");
		System.out.println("2. ������");
		System.out.println("3. д����");
		System.out.println("x. �Ͽ�������");
		
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