import java.util.ArrayList;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.AggregateConfiguration;
import org.opcfoundation.ua.core.HistoryData;
import org.opcfoundation.ua.core.HistoryEvent;
import org.opcfoundation.ua.core.HistoryModifiedData;
import org.opcfoundation.ua.core.PerformUpdateType;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.utils.NumericRange;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.HistoryManagerListener;
import com.prosysopc.ua.server.ServiceContext;


public class HistoryManager extends HistoryDataManager implements HistoryManagerListener {
	
	public Object onReadRaw(ServiceContext serviceContext,
			TimestampsToReturn timestampsToReturn, NodeId nodeId,
			UaNode node, Object continuationPoint, DateTime startTime,
			DateTime endTime, UnsignedInteger numValuesPerNode,
			Boolean returnBounds, NumericRange indexRange,
			HistoryData historyData) throws StatusException {
		
		ArrayList<DataValue> valueArrayList = new ArrayList<DataValue>();
		
		this.getHistory(nodeId, startTime, endTime, valueArrayList);
		
		if (!valueArrayList.isEmpty())
		{
			DataValue[] historyValues = (DataValue[])valueArrayList.toArray(new DataValue[0]);				
			historyData.setDataValues(historyValues);
		}
		
		return null; 
	}
	
	public void onDeleteAtTimes(ServiceContext serviceContext,
			NodeId nodeId, UaNode node, DateTime[] reqTimes,
			StatusCode[] operationResults,
			DiagnosticInfo[] operationDiagnostics) throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public void onDeleteEvents(ServiceContext serviceContext,
			NodeId nodeId, UaNode node, byte[][] eventIds,
			StatusCode[] operationResults,
			DiagnosticInfo[] operationDiagnostics) throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public void onDeleteModified(ServiceContext serviceContext,
			NodeId nodeId, UaNode node, DateTime startTime, DateTime endTime)
			throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public void onDeleteRaw(ServiceContext serviceContext, NodeId nodeId,
			UaNode node, DateTime startTime, DateTime endTime)
			throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public Object onReadAtTimes(ServiceContext serviceContext,
			TimestampsToReturn timestampsToReturn, NodeId nodeId,
			UaNode node, Object continuationPoint, DateTime[] reqTimes,
			NumericRange indexRange, HistoryData historyData)
			throws StatusException {

		return null;
	}

	public Object onReadModified(ServiceContext serviceContext,
			TimestampsToReturn timestampsToReturn, NodeId nodeId,
			UaNode node, Object continuationPoint, DateTime startTime,
			DateTime endTime, UnsignedInteger numValuesPerNode,
			NumericRange indexRange, HistoryModifiedData historyData)
			throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public Object onReadProcessed(ServiceContext serviceContext,
			TimestampsToReturn timestampsToReturn, NodeId nodeId,
			UaNode node, Object continuationPoint, DateTime startTime,
			DateTime endTime, Double resampleInterval,
			NodeId aggregateType,
			AggregateConfiguration aggregateConfiguration,
			NumericRange indexRange, HistoryData historyData)
			throws StatusException {
		throw new StatusException(
				
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public void onUpdateData(ServiceContext serviceContext, NodeId nodeId,
			UaNode node, DataValue[] updateValues,
			PerformUpdateType performInsertReplace,
			StatusCode[] operationResults,
			DiagnosticInfo[] operationDiagnostics) throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	public void onUpdateStructureData(ServiceContext serviceContext,
			NodeId nodeId, UaNode node, DataValue[] updateValues,
			PerformUpdateType performUpdateType,
			StatusCode[] operationResults,
			DiagnosticInfo[] operationDiagnostics) throws StatusException {
		throw new StatusException(
				StatusCodes.Bad_HistoryOperationUnsupported);
	}

	
	public Object onReadEvents(ServiceContext arg0, NodeId arg1, UaNode arg2,
			Object arg3, DateTime arg4, DateTime arg5, UnsignedInteger arg6,
			org.opcfoundation.ua.core.EventFilter arg7, HistoryEvent arg8)
			throws StatusException {
		// TODO Auto-generated method stub
		return null;
	}

	public void onUpdateEvent(ServiceContext arg0, NodeId arg1, UaNode arg2,
			Variant[] arg3, org.opcfoundation.ua.core.EventFilter arg4,
			PerformUpdateType arg5, StatusCode[] arg6, DiagnosticInfo[] arg7)
			throws StatusException {
		// TODO Auto-generated method stub
	}		
}
