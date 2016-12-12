package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * This configuration element indicates that the query string of the HTTP request is to be used as
 * the string value for the {@link StringMatchingExpression}.
 * 
 * @author Marius Oehler
 *
 */
@XmlRootElement(name = "http-query-string")
public class HttpQueryStringValueSource extends StringValueSource {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		if (!InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			return new String[0];
		}
		HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
		return new String[] { httpData.getHttpInfo().getQueryString() };
	}
}
