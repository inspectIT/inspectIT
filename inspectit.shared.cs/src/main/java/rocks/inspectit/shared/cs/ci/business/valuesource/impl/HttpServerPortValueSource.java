package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

/**
 * This configuration element indicates that the port of the HTTP request is to be used as the
 * string value for the {@link StringMatchingExpression}.
 *
 * @author Marius Oehler
 *
 */
@XmlRootElement(name = "http-server-port")
public class HttpServerPortValueSource extends StringValueSource {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		if (!InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			return new String[0];
		}
		HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
		return new String[] { String.valueOf(httpData.getHttpInfo().getServerPort()) };
	}
}
