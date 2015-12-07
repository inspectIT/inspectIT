package info.novatec.inspectit.ci.business;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This configuration element indicates that the method signature is to be used as the string value
 * for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "method-signature")
public class MethodSignatureValueSource extends StringValueSource {

	/**
	 *
	 */
	private static final long serialVersionUID = 6039699478396088534L;
}
