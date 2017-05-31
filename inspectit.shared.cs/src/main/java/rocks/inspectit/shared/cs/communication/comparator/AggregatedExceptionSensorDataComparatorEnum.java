package rocks.inspectit.shared.cs.communication.comparator;

import java.util.Comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;

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
	@Override
	public int compare(AggregatedExceptionSensorData o1, AggregatedExceptionSensorData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(AggregatedExceptionSensorData o1, AggregatedExceptionSensorData o2) {
		switch (this) {
		case CREATED:
			return Long.compare(o1.getCreated(), o2.getCreated());
		case RETHROWN:
			return Long.compare(o1.getPassed(), o2.getPassed());
		case HANDLED:
			return Long.compare(o1.getHandled(), o2.getHandled());
		default:
			return 0;
		}
	}
}
