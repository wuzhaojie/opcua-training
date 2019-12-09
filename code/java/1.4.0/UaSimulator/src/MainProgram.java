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

//Ua Client开发3-模拟器
public class MainProgram {

	public static void main(String[] args) throws IOException, ServerListException, InvalidServerEndpointException, ConnectException, SecureIdentityException, ServiceException, URISyntaxException, StatusException, InterruptedException {
		
		OpcUaClient client = new OpcUaClient();
		
		if (client.connect())
		{
		} else {
			System.out.println("无法连接到 " + client.getServerUrl());	
			return;
		}	
		
		Simulator simulator = new Simulator(client);
		
		simulator.start();
		
		System.out.println("模拟器启动，输入任意键结束程序");
		
		readLine();
		
		simulator.end();
		
		client.disconnect();
		
		System.out.println("模拟器关闭");
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
