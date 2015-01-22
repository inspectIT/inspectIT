package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.RemoteCallData;

import java.util.Date;
import java.util.List;

/**
 * Provides Services to access <code>RemoteCallData</code> information.
 * 
 * @author Thomas Kluge
 */
public interface RemoteCallDataDao {

	/**
	 * Returns a list of the aggregated remote data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @return The list of the aggregated timer data object.
	 */
	List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData);

	/**
	 * Returns a list of the timer data for a given template for a time frame. In this template,
	 * only the platform id is extracted.
	 * 
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the timer data object.
	 */
	List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData, Date fromDate, Date toDate);

	/**
	 * 
	 * @param platformID
	 *            The platform id.
	 * @param identification
	 *            The identification of this remote call.
	 * @param calling
	 *            If it is the remote call of the request (true) or the respone (false).
	 * @return The {@link RemoteCallData}
	 */
	RemoteCallData getRemoteCallDataByIdentification(long platformID, long identification, boolean calling);
}
