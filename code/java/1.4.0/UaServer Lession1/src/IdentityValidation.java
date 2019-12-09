import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.server.Session;
import com.prosysopc.ua.server.UserValidator;
import org.opcfoundation.ua.core.UserTokenType;

public class IdentityValidation implements UserValidator {

  public boolean onValidate(Session session, UserIdentity userIdentity) throws StatusException {

    if (userIdentity.getType().equals(UserTokenType.UserName)) {
      System.out.println("用户名:" + userIdentity.getName() + "---" + "密码:" + userIdentity.getPassword());
      if (userIdentity.getName().equals("opcua") && userIdentity.getPassword().equals("opcua")) {
        System.out.println("用户认证成功");
        return true;
      }
    }

    return false;
  }

/*	@Override
	public boolean onValidate(Session arg0, ServerUserIdentity arg1) throws StatusException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onValidationError(Session arg0, UserIdentityToken arg1, Exception arg2) {
		// TODO Auto-generated method stub
		
	}*/
}