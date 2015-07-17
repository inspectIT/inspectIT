package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;
import info.novatec.inspectit.util.ObjectUtils;

/**
 * Comparators for {@link InvocationSequenceData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum InvocationSequenceDataComparatorEnum implements IDataComparator<InvocationSequenceData> {

	/**
	 * Sort by child count.
	 */
	CHILD_COUNT,

	/**
	 * Sort by the invocation duration.
	 */
	DURATION,

	/**
	 * Sort by the type of nested data in invocation.
	 */
	NESTED_DATA,

	/**
	 * Sort by URIs (if available in invocation root).
	 */
	URI,

	/**
	 * Sort by use cases (if available in invocation root).
	 */
	USE_CASE;

	/**
	 * {@inheritDoc}
	 */
	public int compare(InvocationSequenceData o1, InvocationSequenceData o2, ICachedDataService cachedDataService) {
		switch (this) {
		case CHILD_COUNT:
			return Long.valueOf(o1.getChildCount()).compareTo(Long.valueOf(o2.getChildCount()));
		case DURATION:
			if (InvocationSequenceDataHelper.hasTimerData(o1) && InvocationSequenceDataHelper.hasTimerData(o2)) {
				return Double.compare(o1.getTimerData().getDuration(), o2.getTimerData().getDuration());
			} else {
				return Double.compare(o1.getDuration(), o2.getDuration());
			}
		case NESTED_DATA:
			int invNested1 = 0;
			if (InvocationSequenceDataHelper.hasNestedSqlStatements(o1)) {
				invNested1 += 2;
			}
			if (InvocationSequenceDataHelper.hasNestedExceptions(o1)) {
				invNested1++;
			}
			int invNested2 = 0;
			if (InvocationSequenceDataHelper.hasNestedSqlStatements(o2)) {
				invNested2 += 2;
			}
			if (InvocationSequenceDataHelper.hasNestedExceptions(o2)) {
				invNested2++;
			}
			return invNested1 - invNested2;
		case URI:
			if (InvocationSequenceDataHelper.hasHttpTimerData(o1) && InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				String uri1 = ((HttpTimerData) o1.getTimerData()).getUri();
				String uri2 = ((HttpTimerData) o2.getTimerData()).getUri();
				return ObjectUtils.compare(uri1, uri2);
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o1)) {
				return 1;
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				return -1;
			} else {
				return 0;
			}
		case USE_CASE:
			if (InvocationSequenceDataHelper.hasHttpTimerData(o1) && InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				String useCase1 = ((HttpTimerData) o1.getTimerData()).getInspectItTaggingHeaderValue();
				String useCase2 = ((HttpTimerData) o2.getTimerData()).getInspectItTaggingHeaderValue();
				return ObjectUtils.compare(useCase1, useCase2);
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o1)) {
				return 1;
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				return -1;
			} else {
				return 0;
			}
		default:
			return 0;
		}
	}

}
