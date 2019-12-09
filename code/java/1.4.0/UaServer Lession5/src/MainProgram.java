import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.server.UaServerException;


public class MainProgram {

	public static void main(String[] args) throws UaServerException, SecureIdentityException, IOException, StatusException, InterruptedException {
		OpcUaServer server = new OpcUaServer();		

		server.start();		
		
		System.out.println("服务器启动，按任意键停止服务器");
		
		readLine();
		
		server.stop(); 
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
