package info.novatec.inspectit.cmr.configuration.business.expression.impl;

import info.novatec.inspectit.cmr.configuration.business.expression.StringValueSource;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This configuration element indicates that the value of an HTTP parameter is to be used as the
 * string value for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "http-parameter-value")
public class HttpParameterValueSource extends StringValueSource {

	/**
	 *
	 */
	private static final long serialVersionUID = -4448346872365618403L;

	/**
	 * Name of the HTTP parameter.
	 */
	@XmlAttribute(name = "parameter-name")
	private String parameterName;

	/**
	 * Default constructor.
	 */
	public HttpParameterValueSource() {
	}

	/**
	 * Constructor.
	 *
	 * @param parameterName
	 *            {@link #parameterName}
	 */
	public HttpParameterValueSource(String parameterName) {
		super();
		this.parameterName = parameterName;
	}

	/**
	 * Gets {@link #parameterName}.
	 *
	 * @return {@link #parameterName}
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Sets {@link #parameterName}.
	 *
	 * @param parameterName
	 *            New value for {@link #parameterName}
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

}
