package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * Class for {@link ExceptionSensorData} aggregation.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionDataAggregator implements IAggregator<ExceptionSensorData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3605008873889674596L;

	/**
	 * Definition of aggregation type.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum ExceptionAggregationType {
		/**
		 * Aggregation based on the throw-able type and error message.
		 */
		GROUP_EXCEPTION_OVERVIEW,

		/**
		 * Aggregation based on the stack trace and error message.
		 */
		DISTINCT_STACK_TRACES,

		/**
		 * Aggregation just based on the throwable type.
		 */
		THROWABLE_TYPE;
	}

	/**
	 * Exception aggregation type.
	 */
	private ExceptionAggregationType exceptionAggregationType;

	/**
	 * No-arg constructor.
	 */
	public ExceptionDataAggregator() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param exceptionAggregationType
	 *            Exception aggregation type.
	 */
	public ExceptionDataAggregator(ExceptionAggregationType exceptionAggregationType) {
		this.exceptionAggregationType = exceptionAggregationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<ExceptionSensorData> aggregatedObject, ExceptionSensorData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
		if (null != objectToAdd.getChild()) {
			aggregate(aggregatedObject, objectToAdd.getChild());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IAggregatedData<ExceptionSensorData> getClone(ExceptionSensorData exceptionData) {
		AggregatedExceptionSensorData clone = new AggregatedExceptionSensorData();
		clone.setPlatformIdent(exceptionData.getPlatformIdent());
		clone.setSensorTypeIdent(exceptionData.getSensorTypeIdent());
		clone.setThrowableType(exceptionData.getThrowableType());
		if (exceptionAggregationType == ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW || exceptionAggregationType == ExceptionAggregationType.DISTINCT_STACK_TRACES) {
			clone.setErrorMessage(exceptionData.getErrorMessage());
		}
		if (exceptionAggregationType == ExceptionAggregationType.DISTINCT_STACK_TRACES) {
			clone.setStackTrace(getCorrectStackTrace(exceptionData.getStackTrace()));
		}
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(ExceptionSensorData exceptionSensorData) {
		final int prime = 31;
		if (exceptionAggregationType == ExceptionAggregationType.THROWABLE_TYPE) {
			int result = 0;
			result = prime * result + ((exceptionSensorData.getThrowableType() == null) ? 0 : exceptionSensorData.getThrowableType().hashCode());
			return result;
		} else if (exceptionAggregationType == ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW) {
			int result = 0;
			result = prime * result + ((exceptionSensorData.getThrowableType() == null) ? 0 : exceptionSensorData.getThrowableType().hashCode());
			result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
			return result;
		} else if (exceptionAggregationType == ExceptionAggregationType.DISTINCT_STACK_TRACES) {
			int result = 0;
			result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
			result = prime * result + ((exceptionSensorData.getStackTrace() == null) ? 0 : getCorrectStackTrace(exceptionSensorData.getStackTrace()).hashCode());
			return result;
		}
		return 0;
	}

	/**
	 * Returns the stack trace starting at the first line where the method trace starts.
	 * 
	 * @param stackTrace
	 *            Original stack trace.
	 * @return Modified stack trace.
	 */
	private String getCorrectStackTrace(String stackTrace) {
		if (null == stackTrace) {
			return null;
		} else {
			int index = stackTrace.indexOf("\n\tat");
			if (index >= 0) {
				return stackTrace.substring(index + 1);
			} else {
				return stackTrace;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exceptionAggregationType == null) ? 0 : exceptionAggregationType.hashCode());
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
		ExceptionDataAggregator other = (ExceptionDataAggregator) obj;
		if (exceptionAggregationType != other.exceptionAggregationType) {
			return false;
		}
		return true;
	}

}
