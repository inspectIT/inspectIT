package rocks.inspectit.shared.all.communication.comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.util.ObjectUtils;

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
	 * Sort by applications (if available in invocation root).
	 */
	APPLICATION,

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
				String uri1 = ((HttpTimerData) o1.getTimerData()).getHttpInfo().getUri();
				String uri2 = ((HttpTimerData) o2.getTimerData()).getHttpInfo().getUri();
				return ObjectUtils.compare(uri1, uri2);
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o1)) {
				return 1;
			} else if (InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				return -1;
			} else {
				return 0;
			}
		case APPLICATION:
			ApplicationData appDeff1 = cachedDataService.getApplicationForId(o1.getApplicationId());
			ApplicationData appDeff2 = cachedDataService.getApplicationForId(o2.getApplicationId());

			return ObjectUtils.compare(appDeff1.getName().toLowerCase(), appDeff2.getName().toLowerCase());
		case USE_CASE:
			String businessTransaction1 = null;
			String businessTransaction2 = null;
			if (InvocationSequenceDataHelper.hasHttpTimerData(o1)) {
				businessTransaction1 = ((HttpTimerData) o1.getTimerData()).getHttpInfo().getInspectItTaggingHeaderValue();
			}
			if (null == businessTransaction1) {
				BusinessTransactionData btxDef1 = cachedDataService.getBusinessTransactionForId(o1.getApplicationId(), o1.getBusinessTransactionId());
				if (null != btxDef1) {
					businessTransaction1 = btxDef1.getName();
				}
			}

			if (null != businessTransaction1) {
				businessTransaction1 = businessTransaction1.toLowerCase();
			}

			if (InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				businessTransaction2 = ((HttpTimerData) o2.getTimerData()).getHttpInfo().getInspectItTaggingHeaderValue();
			}
			if (null == businessTransaction2) {
				BusinessTransactionData btxDef2 = cachedDataService.getBusinessTransactionForId(o2.getApplicationId(), o2.getBusinessTransactionId());
				if (null != btxDef2) {
					businessTransaction2 = btxDef2.getName();
				}
			}

			if (null != businessTransaction2) {
				businessTransaction2 = businessTransaction2.toLowerCase();
			}

			return ObjectUtils.compare(businessTransaction1, businessTransaction2);
		default:
			return 0;
		}
	}

}
