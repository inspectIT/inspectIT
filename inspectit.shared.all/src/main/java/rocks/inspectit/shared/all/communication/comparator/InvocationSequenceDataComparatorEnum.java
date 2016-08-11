package rocks.inspectit.shared.all.communication.comparator;

import org.apache.commons.lang.StringUtils;

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
	 * Sort by business transactions (if available in invocation root).
	 */
	BUSINESS_TRANSACTION,

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
			if (InvocationSequenceDataHelper.hasNestedOutgoingRemoteCalls(o1)) {
				invNested1++;
			}
			if (InvocationSequenceDataHelper.hasNestedIncommingRemoteCalls(o1)) {
				invNested1++;
			}
			int invNested2 = 0;
			if (InvocationSequenceDataHelper.hasNestedSqlStatements(o2)) {
				invNested2 += 2;
			}
			if (InvocationSequenceDataHelper.hasNestedExceptions(o2)) {
				invNested2++;
			}
			if (InvocationSequenceDataHelper.hasNestedOutgoingRemoteCalls(o2)) {
				invNested2++;
			}
			if (InvocationSequenceDataHelper.hasNestedIncommingRemoteCalls(o2)) {
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
			ApplicationData appData1 = cachedDataService.getApplicationForId(o1.getApplicationId());
			ApplicationData appData2 = cachedDataService.getApplicationForId(o2.getApplicationId());
			String appName1 = null != appData1 ? appData1.getName().toLowerCase() : StringUtils.EMPTY;
			String appName2 = null != appData2 ? appData2.getName().toLowerCase() : StringUtils.EMPTY;
			return ObjectUtils.compare(appName1, appName2);
		case BUSINESS_TRANSACTION:
			BusinessTransactionData btData1 = cachedDataService.getBusinessTransactionForId(o1.getApplicationId(), o1.getBusinessTransactionId());
			BusinessTransactionData btData2 = cachedDataService.getBusinessTransactionForId(o2.getApplicationId(), o2.getBusinessTransactionId());
			String btName1 = null != btData1 ? btData1.getName().toLowerCase() : StringUtils.EMPTY;
			String btName2 = null != btData2 ? btData2.getName().toLowerCase() : StringUtils.EMPTY;
			return ObjectUtils.compare(btName1, btName2);
		case USE_CASE:
			if (InvocationSequenceDataHelper.hasHttpTimerData(o1) && InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				String useCase1 = ((HttpTimerData) o1.getTimerData()).getHttpInfo().getInspectItTaggingHeaderValue();
				String useCase2 = ((HttpTimerData) o2.getTimerData()).getHttpInfo().getInspectItTaggingHeaderValue();
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
