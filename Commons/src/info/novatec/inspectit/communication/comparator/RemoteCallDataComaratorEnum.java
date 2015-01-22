package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.RemoteCallData;

import java.util.Comparator;

/**
 * Comparators for the {@link RemoteCallData}.
 * 
 * @author Thomas Kluge
 * 
 */
public enum RemoteCallDataComaratorEnum implements IDataComparator<RemoteCallData>, Comparator<RemoteCallData> {

	/**
	 * Sort on isCalling.
	 */
	IS_CALLING,

	/**
	 * Sort on identification.
	 */
	IDENTIFICATION;

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
