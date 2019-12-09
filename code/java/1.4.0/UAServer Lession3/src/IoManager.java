import java.util.EnumSet;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.utils.NumericRange;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.WriteAccess;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.IoManagerListener;
import com.prosysopc.ua.server.ServiceContext;


public class IoManager implements IoManagerListener {

	public IoManager(NodeManager nodeManager)
	{
		m_nodeManager = nodeManager;
	}
	
	public boolean onWriteValue(ServiceContext context, NodeId nodeId,UaVariable variable, NumericRange numericRange, DataValue dataValue)
	throws StatusException {

		m_nodeManager.processDataChanger(variable, dataValue);
		
		return true;
	}	
	
	public EnumSet<AccessLevel> onGetUserAccessLevel(ServiceContext arg0,NodeId arg1, UaVariable arg2) {
		return EnumSet.of(AccessLevel.CurrentRead,	AccessLevel.CurrentWrite, AccessLevel.HistoryRead);
	}

	public boolean onGetUserExecutable(ServiceContext arg0, NodeId arg1,UaMethod arg2) {
		return true;
	}

	public EnumSet<WriteAccess> onGetUserWriteMask(ServiceContext arg0,NodeId arg1, UaNode arg2) {
		return EnumSet.allOf(WriteAccess.class);
	}

	public void onReadNonValue(ServiceContext arg0, NodeId arg1,
			UaNode arg2, UnsignedInteger arg3, DataValue arg4)
			throws StatusException {
	}

	public void onReadValue(ServiceContext context, NodeId nodeId,UaVariable variable, NumericRange numericRange, 
			                TimestampsToReturn timeStampToReturn, DateTime time, DataValue value) throws StatusException {
	}

	public boolean onWriteNonValue(ServiceContext arg0, NodeId arg1,
			                       UaNode arg2, UnsignedInteger arg3, DataValue arg4)
			throws StatusException {
		return false;
	}
	
	private NodeManager m_nodeManager;
}
