package info.novatec.inspectit.ci.business;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This configuration element indicates that the URI of the HTTP request is to be used as the string
 * value for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "http-uri")
public class HttpUriValueSource extends StringValueSource {

	/**
	 *
	 */
	private static final long serialVersionUID = -8512736546121136491L;

}
