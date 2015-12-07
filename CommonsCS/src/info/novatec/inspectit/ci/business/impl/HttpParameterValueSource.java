package info.novatec.inspectit.ci.business.impl;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;

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
	 * Name of the HTTP parameter.
	 */
	@XmlAttribute(name = "parameter-name", required = true)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		if (InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
			if (null != httpData.getParameters()) {
				return httpData.getParameters().get(this.getParameterName());
			}
		}
		return new String[0];
	}

}
