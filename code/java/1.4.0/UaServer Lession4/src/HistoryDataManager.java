import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;

import com.prosysopc.ua.nodes.DataChangeListener;
import com.prosysopc.ua.nodes.UaNode;

public class HistoryDataManager implements DataChangeListener{
	
	public void print()
	{	
		for (int i=0; i<m_historyData.size(); i++)
		{
			System.out.println(m_historyData.toString());
		}
	}
	
	public HistoryDataManager()
	{
		m_historyData = new HashMap<NodeId,ArrayList<DataValue>>();
	}
	
	public void onDataChange(UaNode uaNode, DataValue prevValue,DataValue value) {	
		NodeId id = uaNode.getNodeId();
		
		ArrayList<DataValue> nodeHistoryData = null;		
		
		if (m_historyData.containsKey(id))
		{
			nodeHistoryData = m_historyData.get(id);
		} else {
			nodeHistoryData = new ArrayList<DataValue>();
			m_historyData.put(id, nodeHistoryData);
		}
		
		if (nodeHistoryData.size() <= 100000) nodeHistoryData.add(value);
	}
	
	void getHistory(NodeId               id, 
		            DateTime             startTime, 
		            DateTime             endTime, 
		            ArrayList<DataValue> values)
	{	
		if (!m_historyData.containsKey(id)) return;

		ArrayList<DataValue> nodeHistoryData = m_historyData.get(id);
		
		boolean inverseOrder = false;
		
		DateTime start = null;
		DateTime end   = null;
		
		if (startTime.getValue() > endTime.getValue()) 
		{
			inverseOrder = true;
			start = endTime;
			end   = startTime;
		} else {
			start = startTime;
			end   = endTime;
		}

		int beginPos = -1;
		int endPos = -1;
			
		for (int i=0; i<nodeHistoryData.size() && (-1 == beginPos || -1 == endPos); i++)
		{
			DataValue value = nodeHistoryData.get(i);
				
			if (-1 == beginPos)
			{					
				if (value.getSourceTimestamp().getValue() >= start.getValue())
				{
					beginPos = i;
				}
			} else {
				if (value.getSourceTimestamp().getValue() > end.getValue())
				{
					endPos = i-1;
				}
			}
		}
			
		if (-1 != beginPos)
		{
			if (-1 == endPos)
			{
				endPos = nodeHistoryData.size()-1;
			}
			
			if (inverseOrder)
			{
				for (int j = endPos; j >= beginPos; j--)
				{
					values.add(nodeHistoryData.get(j));
				} 
			} else {
				for (int j = beginPos; j <= endPos; j++)
				{
					values.add(nodeHistoryData.get(j));
				}
			}
		}
	}
	
	private Map<NodeId,ArrayList<DataValue>> m_historyData;
}
