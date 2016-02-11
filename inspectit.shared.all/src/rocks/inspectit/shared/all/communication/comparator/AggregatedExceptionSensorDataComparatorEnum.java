package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;

import java.util.Comparator;

/**
 * Comparators for {@link AggregatedExceptionSensorData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum AggregatedExceptionSensorDataComparatorEnum implements IDataComparator<AggregatedExceptionSensorData>, Comparator<AggregatedExceptionSensorData> {

	/**
	 * Sort by amount of created exceptions.
	 */
	CREATED,

	/**
	 * Sort by amount of re-thrown exceptions.
	 */
	RETHROWN,

	/**
	 * Sort by amount of handled exceptions.
	 */
	HANDLED;

	/**
	 * {@inheritDoc}
	 */
	public int compare(AggregatedExceptionSensorData o1, AggregatedExceptionSensorData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(AggregatedExceptionSensorData o1, AggregatedExceptionSensorData o2) {
		switch (this) {
		case CREATED:
			return Long.valueOf(o1.getCreated()).compareTo(Long.valueOf(o2.getCreated()));
		case RETHROWN:
			return Long.valueOf(o1.getPassed()).compareTo(Long.valueOf(o2.getPassed()));
		case HANDLED:
			return Long.valueOf(o1.getHandled()).compareTo(Long.valueOf(o2.getHandled()));
		default:
			return 0;
		}
	}
}
