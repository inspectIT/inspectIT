package rocks.inspectit.shared.all.communication.comparator;

import java.util.Comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.util.ObjectUtils;

/**
 * Comparators for {@link ExceptionSensorData}.
 *
 * @author Ivan Senic
 *
 */
public enum ExceptionSensorDataComparatorEnum implements IDataComparator<ExceptionSensorData>, Comparator<ExceptionSensorData> {

	/**
	 * Sort by fully qualified name of the exception.
	 */
	FQN,

	/**
	 * Sort by the error message.
	 */
	ERROR_MESSAGE;

	/**
	 * {@inheritDoc}
	 */
	public int compare(ExceptionSensorData o1, ExceptionSensorData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(ExceptionSensorData o1, ExceptionSensorData o2) {
		switch (this) {
		case FQN:
			return ObjectUtils.compare(o1.getThrowableType(), o2.getThrowableType());
		case ERROR_MESSAGE:
			return ObjectUtils.compare(o1.getErrorMessage(), o2.getErrorMessage());
		default:
			return 0;
		}
	}

}
