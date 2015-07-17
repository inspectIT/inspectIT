package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.InvocationAwareData;

import java.util.Comparator;

/**
 * Comparators for the {@link InvocationAwareData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum InvocationAwareDataComparatorEnum implements IDataComparator<InvocationAwareData>, Comparator<InvocationAwareData> {

	/**
	 * Sorting by invocation affiliation percentage.
	 */
	INVOCATION_AFFILIATION;

	/**
	 * {@inheritDoc}
	 */
	public int compare(InvocationAwareData o1, InvocationAwareData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(InvocationAwareData o1, InvocationAwareData o2) {
		switch (this) {
		case INVOCATION_AFFILIATION:
			return Double.compare(o1.getInvocationAffiliationPercentage(), o2.getInvocationAffiliationPercentage());
		default:
			return 0;
		}
	}

}
