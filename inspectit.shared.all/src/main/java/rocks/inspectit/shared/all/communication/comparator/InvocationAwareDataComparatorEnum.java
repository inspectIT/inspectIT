package rocks.inspectit.shared.all.communication.comparator;

import java.util.Comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;

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
