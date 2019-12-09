import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.opcfoundation.ua.common.ServiceResultException;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.server.UaServerException;


public class MainProgram {

	public static void main(String[] args) throws UaServerException, SecureIdentityException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, ServiceResultException {
		
		OpcUaServer server = new OpcUaServer();
		
		server.start();
		
		System.out.println("�������������������ֹͣ������");
		
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