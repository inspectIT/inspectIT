package rocks.inspectit.shared.all.communication.comparator;

import java.util.Comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * Comparators for the {@link RemoteCallData}.
 *
 * @author Thomas Kluge
 *
 */
public enum RemoteCallDataComparatorEnum implements IDataComparator<RemoteCallData>, Comparator<RemoteCallData> {

	/**
	 * Sort on isCalling.
	 */
	IS_CALLING,

	/**
	 * Sort on identification.
	 */
	IDENTIFICATION,

	/**
	 * Sort on remote platform ID.
	 */
	REMOTE_PLATFORM_ID,

	/**
	 * Sort for Remote Type.
	 */
	REMOTE_TYPE,

	/**
	 * Sort by specific data.
	 */
	CONNECTION_SPECIFIC_DATA;

	/**
	 * {@inheritDoc}
	 */
	public int compare(RemoteCallData o1, RemoteCallData o2) {
		switch (this) {
		case IS_CALLING:
			// Java5 does not have Boolean.compare
			return Boolean.valueOf(o1.isCalling()).compareTo(Boolean.valueOf(o2.isCalling()));
		case IDENTIFICATION:
			// Java5 does not have Long.compare
			return Long.valueOf(o1.getIdentification()).compareTo(Long.valueOf(o2.getIdentification()));
		case REMOTE_PLATFORM_ID:
			// Java5 does not have Long.compare
			return Long.valueOf(o1.getRemotePlatformIdent()).compareTo(Long.valueOf(o2.getRemotePlatformIdent()));
		case REMOTE_TYPE:
			return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
		default:
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(RemoteCallData o1, RemoteCallData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

}