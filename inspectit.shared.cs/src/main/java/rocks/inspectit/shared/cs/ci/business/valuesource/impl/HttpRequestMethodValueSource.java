package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.http.HttpMethod;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

/**
 * This configuration element indicates that the Request Method of the HTTP request is to be used as
 * the string value for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "http-request-method")
public class HttpRequestMethodValueSource extends StringValueSource {
	/**
	 * The array of options to select from for this value source.
	 */
	public static final String[] OPTIONS = { HttpMethod.GET.toString(), HttpMethod.POST.toString(), HttpMethod.PUT.toString(), HttpMethod.DELETE.toString(), HttpMethod.HEAD.toString(),
			HttpMethod.OPTIONS.toString(), HttpMethod.PATCH.toString(), HttpMethod.TRACE.toString() };

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		if (!InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			return new String[0];
		}
		HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
		return new String[] { httpData.getHttpInfo().getRequestMethod() };
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSelection() {
		return true;
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public String[] getOptions() {
		return OPTIONS;
	}
}
