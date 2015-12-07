package info.novatec.inspectit.cmr.configuration.business.expression.impl;

import info.novatec.inspectit.cmr.configuration.business.expression.StringValueSource;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This configuration element indicates that the host name or IP is to be used as the string value
 * for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "host")
public class HostValueSource extends StringValueSource {

	/**
	 *
	 */
	private static final long serialVersionUID = 2009927612350786033L;

}
