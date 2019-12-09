import java.util.EnumSet;

import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.transport.security.Cert;

import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;


public class CertificateValidation implements CertificateValidationListener {
	public ValidationResult onValidate(Cert certificate,
			ApplicationDescription applicationDescription,
			EnumSet<CertificateCheck> passedChecks) {			

		return ValidationResult.AcceptPermanently;
	}
}