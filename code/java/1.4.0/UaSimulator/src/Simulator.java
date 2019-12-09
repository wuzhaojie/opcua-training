import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;


public class Simulator extends Thread {
	public Simulator(OpcUaClient client)
	{
		m_client = client;
		m_isStop = false;
	}
	
	public void run()
	{
		while (!m_isStop)
		{
			try {
				m_client.writeSimulatedData();
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (StatusException e) {
				e.printStackTrace();
			}
			
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void end() throws InterruptedException
	{
		m_isStop = true;
		join();
	}
	
	private OpcUaClient m_client;
	
	private volatile boolean m_isStop;
}