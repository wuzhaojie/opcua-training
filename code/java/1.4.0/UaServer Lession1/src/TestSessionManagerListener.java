import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.server.Session;
import com.prosysopc.ua.server.SessionManagerListener;


public class TestSessionManagerListener implements SessionManagerListener {

  @Override
  public boolean onActivateSession(Session session, UserIdentity userIdentity) throws StatusException {
    return false;
  }

  @Override
  public void onCancelSession(Session arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onCloseSession(Session arg0, boolean arg1) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onCreateSession(Session arg0) throws StatusException {    // TODO Auto-generated method stub

  }
}
