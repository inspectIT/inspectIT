package rocks.inspectit.shared.all.communication.comparator;

import java.util.Comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.util.ObjectUtils;

/**
 * Available comparators for all {@link DefaultData} objects.
 * 
 * @author Ivan Senic
 * 
 */
public enum DefaultDataComparatorEnum implements IDataComparator<DefaultData>, Comparator<DefaultData> {

	/**
	 * Compares objects by time stamps.
	 */
	TIMESTAMP;

	/**
	 * {@inheritDoc}
	 */
	public int compare(DefaultData o1, DefaultData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(DefaultData o1, DefaultData o2) {
		switch (this) {
		case TIMESTAMP:
			return ObjectUtils.compare(o1.getTimeStamp(), o2.getTimeStamp());
		default:
			return 0;
		}
	}
}
