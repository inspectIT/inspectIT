package info.novatec.inspectit.ci.business.impl;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;

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
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		if (!InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			return null;
		}
		HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
		return new String[] { httpData.getHttpInfo().getUri() };
	}

}
