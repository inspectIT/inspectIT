package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.HttpInfo;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * Aggregation for {@link HttpTimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpTimerDataAggregator implements IAggregator<HttpTimerData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 495449254866425040L;

	/**
	 * Is UIR based.
	 */
	private boolean uriBased;

	/**
	 * Should request method be included in aggregation.
	 */
	protected boolean includeRequestMethod;

	/**
	 * No-arg constructor.
	 */
	public HttpTimerDataAggregator() {
	}

	/**
	 * Default constructor that defines aggregation parameters.
	 * 
	 * @param uriBased
	 *            Is aggregation URi based.
	 * @param includeRequestMethod
	 *            Should request method be included in aggregation.
	 */
	public HttpTimerDataAggregator(boolean uriBased, boolean includeRequestMethod) {
		this.uriBased = uriBased;
		this.includeRequestMethod = includeRequestMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<HttpTimerData> aggregatedObject, HttpTimerData objectToAdd) {
		if (!uriBased) {
			if (!objectToAdd.getHttpInfo().hasInspectItTaggingHeader()) {
				// use case aggregation for elements that do not have any tagged value does not
				// make sense, thus we ignore these.
				return;
			}
		}

		aggregatedObject.aggregate(objectToAdd);

		if (!includeRequestMethod) {
			// If we have different request methods, we set the request method to "multiple"
			if (!objectToAdd.getHttpInfo().getRequestMethod().equals(aggregatedObject.getData().getHttpInfo().getRequestMethod())
					&& !aggregatedObject.getData().getHttpInfo().getRequestMethod().equals(HttpTimerData.REQUEST_METHOD_MULTIPLE)) {
				aggregatedObject.getData().getHttpInfo().setRequestMethod(HttpTimerData.REQUEST_METHOD_MULTIPLE);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IAggregatedData<HttpTimerData> getClone(HttpTimerData httpData) {
		AggregatedHttpTimerData clone = new AggregatedHttpTimerData();
		clone.setPlatformIdent(httpData.getPlatformIdent());
		clone.setSensorTypeIdent(httpData.getSensorTypeIdent());
		clone.setMethodIdent(httpData.getMethodIdent());
		clone.setCharting(httpData.isCharting());
		if (uriBased) {
			clone.getHttpInfo().setUri(httpData.getHttpInfo().getUri());
		} else {
			// Aggregation based on Usecase. We reset the URI so that we can easily know
			// that use case aggregation is used.
			clone.getHttpInfo().setUri(HttpInfo.UNDEFINED);
			clone.getHttpInfo().setInspectItTaggingHeaderValue(httpData.getHttpInfo().getInspectItTaggingHeaderValue());
		}
		clone.getHttpInfo().setRequestMethod(httpData.getHttpInfo().getRequestMethod());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(HttpTimerData httpData) {
		final int prime = 31;
		int result = 0;
		if (uriBased) {
			result = prime * result + ((httpData.getHttpInfo().getUri() == null) ? 0 : httpData.getHttpInfo().getUri().hashCode());
		} else {
			result = prime * result + ((httpData.getHttpInfo().getInspectItTaggingHeaderValue() == null) ? 0 : httpData.getHttpInfo().getInspectItTaggingHeaderValue().hashCode());
		}

		if (includeRequestMethod) {
			result = prime * result + ((httpData.getHttpInfo().getRequestMethod() == null) ? 0 : httpData.getHttpInfo().getRequestMethod().hashCode());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includeRequestMethod ? 1231 : 1237);
		result = prime * result + (uriBased ? 1231 : 1237);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpTimerDataAggregator other = (HttpTimerDataAggregator) obj;
		if (includeRequestMethod != other.includeRequestMethod) {
			return false;
		}
		if (uriBased != other.uriBased) {
			return false;
		}
		return true;
	}

}
