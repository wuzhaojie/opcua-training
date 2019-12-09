import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URISyntaxException;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.ServerListException;


public class Main {

	public static void main(String[] args) throws ServerListException, IOException, InvalidServerEndpointException, ConnectException, SecureIdentityException, ServiceException, URISyntaxException {

		OpcUaClient client = new OpcUaClient();
		
		if (client.connect())
		{
			System.out.println("���ӵ� " + client.getServerUrl());
		} else {
			System.out.println("�޷����ӵ�" + client.getServerUrl());	
			return;
		}
		
		System.out.println();
		System.out.println("�����������������");
		
		readLine();
		
		client.disconnect();
		
		System.out.println("�Ͽ�������  " + client.getServerUrl());	
	}

	public static void readLine() throws IOException
	{
		if (null == s_readStream)
		{
			s_readStream = new InputStreamReader(System.in);
		}
		
		if (null == s_readBuffer)
		{
			s_readBuffer =  new BufferedReader(s_readStream);
		}
		
		s_readBuffer.readLine();
	}
	
	private static BufferedReader s_readBuffer;
	private static InputStreamReader s_readStream;
}
