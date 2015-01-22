package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.Comparator;

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
	TIMESTAMP,
	/**
	 * Compares object by platformIdent.
	 */
	PLATFORM_IDENT;

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
		case PLATFORM_IDENT:
			// Java5 does not have Long.compare
			return Long.valueOf(o1.getPlatformIdent()).compareTo(Long.valueOf(o2.getPlatformIdent()));
		default:
			return 0;
		}
	}
}
