package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
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
		case APPLICATION:
			IApplicationDefinition appDeff1 = cachedDataService.getApplicationDefinitionForId(o1.getApplicationId());
			IApplicationDefinition appDeff2 = cachedDataService.getApplicationDefinitionForId(o2.getApplicationId());

			if (null != appDeff1 && null != appDeff2) {
				return ObjectUtils.compare(appDeff1.getApplicationName().toLowerCase(), appDeff2.getApplicationName().toLowerCase());
			} else if (null != appDeff1) {
				return 1;
			} else if (null != appDeff2) {
				return -1;
			} else {
				return 0;
			}
		case USE_CASE:
			String useCase1 = null;
			String useCase2 = null;
			if (InvocationSequenceDataHelper.hasHttpTimerData(o1)) {
				useCase1 = ((HttpTimerData) o1.getTimerData()).getInspectItTaggingHeaderValue();
			}
			if (InvocationSequenceDataHelper.hasHttpTimerData(o2)) {
				useCase2 = ((HttpTimerData) o2.getTimerData()).getInspectItTaggingHeaderValue();
			}

			if (null == useCase1) {
				IBusinessTransactionDefinition btxDef1 = cachedDataService.getBusinessTransactionDefinitionForId(o1.getApplicationId(), o1.getBusinessTransactionId());
				if (null != btxDef1) {
					useCase1 = btxDef1.getBusinessTransactionName();
				}
			}

			if (null == useCase2) {
				IBusinessTransactionDefinition btxDef2 = cachedDataService.getBusinessTransactionDefinitionForId(o2.getApplicationId(), o2.getBusinessTransactionId());
				if (null != btxDef2) {
					useCase2 = btxDef2.getBusinessTransactionName();
				}
			}

			if (null != useCase1 && null != useCase2) {
				return ObjectUtils.compare(useCase1.toLowerCase(), useCase2.toLowerCase());
			} else if (null != useCase1) {
				return 1;
			} else if (null != useCase2) {
				return -1;
			} else {
				return 0;
			}
		default:
			return 0;
		}
	}

}
