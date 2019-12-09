import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.ServiceContext;


public class MethodManager implements CallableListener {
	
	public MethodManager(NodeManager nodeManager)
	{
		m_nodeManager = nodeManager;
	}
	
	public boolean onCall(ServiceContext serviceContext, 
			              NodeId objectId, 
			              UaNode object, 
			              NodeId methodId, 
			              UaMethod method,	
			              final Variant[] inputArguments,
			              final StatusCode[] inputArgumentResults,
			              final DiagnosticInfo[] inputArgumentDiagnosticInfos,
			              final Variant[] outputs) throws StatusException {
		
		return m_nodeManager.processMethodCall(object, method, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos, outputs);
	}
	
	private NodeManager m_nodeManager;
}
