package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Comparator;

/**
 * Comparators for the {@link TimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum TimerDataComparatorEnum implements IDataComparator<TimerData>, Comparator<TimerData> {

	/**
	 * Sorts on the count.
	 */
	COUNT,

	/**
	 * Sorts on the average duration.
	 */
	AVERAGE,

	/**
	 * Sorts on the min duration.
	 */
	MIN,

	/**
	 * Sorts on the max duration.
	 */
	MAX,

	/**
	 * Sorts on the total duration.
	 */
	DURATION,

	/**
	 * Sorts on the average CPU duration.
	 */
	CPUAVERAGE,

	/**
	 * Sorts on the min CPU duration.
	 */
	CPUMIN,

	/**
	 * Sorts on the max CPU duration.
	 */
	CPUMAX,

	/**
	 * Sorts on the total CPU duration.
	 */
	CPUDURATION,

	/**
	 * Sorts on the average exclusive duration.
	 */
	EXCLUSIVEAVERAGE,

	/**
	 * Sorts on the min exclusive duration.
	 */
	EXCLUSIVEMIN,

	/**
	 * Sorts on the max exclusive duration.
	 */
	EXCLUSIVEMAX,

	/**
	 * Sorts on the total exclusive duration.
	 */
	EXCLUSIVEDURATION,

	/**
	 * Sorts on charting true/false.
	 */
	CHARTING;

	/**
	 * {@inheritDoc}
	 */
	public int compare(TimerData o1, TimerData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(TimerData o1, TimerData o2) {
		switch (this) {
		case COUNT:
			// Java5 does not have Long.compare
			return Long.valueOf(o1.getCount()).compareTo(Long.valueOf(o2.getCount()));
		case AVERAGE:
			return Double.compare(o1.getAverage(), o2.getAverage());
		case MIN:
			return Double.compare(o1.getMin(), o2.getMin());
		case MAX:
			return Double.compare(o1.getMax(), o2.getMax());
		case DURATION:
			return Double.compare(o1.getDuration(), o2.getDuration());
		case CPUAVERAGE:
			return Double.compare(o1.getCpuAverage(), o2.getCpuAverage());
		case CPUMIN:
			return Double.compare(o1.getCpuMin(), o2.getCpuMin());
		case CPUMAX:
			return Double.compare(o1.getCpuMax(), o2.getCpuMax());
		case CPUDURATION:
			return Double.compare(o1.getCpuDuration(), o2.getCpuDuration());
		case EXCLUSIVEAVERAGE:
			return Double.compare(o1.getExclusiveAverage(), o2.getExclusiveAverage());
		case EXCLUSIVEMIN:
			return Double.compare(o1.getExclusiveMin(), o2.getExclusiveMin());
		case EXCLUSIVEMAX:
			return Double.compare(o1.getExclusiveMax(), o2.getExclusiveMax());
		case EXCLUSIVEDURATION:
			return Double.compare(o1.getExclusiveDuration(), o2.getExclusiveDuration());
		case CHARTING:
			// Java5 does not have Boolean.compare
			return Boolean.valueOf(o1.isCharting()).compareTo(Boolean.valueOf(o2.isCharting()));
		default:
			return 0;
		}
	}

}
