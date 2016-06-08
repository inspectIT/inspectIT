package rocks.inspectit.server.dao;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * Provides Services to access <code>RemoteCallData</code> information.
 *
 * @author Thomas Kluge
 */
public interface RemoteCallDataDao {

	/**
	 * Returns a list of the remote call data for a given template. In this template, only the
	 * platform id is extracted.
	 *
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @return The list of the remote call data object.
	 */
	List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData);

	/**
	 * Returns a list of the remote call data for a given template for a time frame. In this
	 * template, only the platform id is extracted.
	 *
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the remote call data object.
	 */
	List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData, Date fromDate, Date toDate);

	/**
	 * Returns a remote call data object with the given platformID, identification and calling
	 * value. There 3 values are unique and identify a particular remote call data object.
	 *
	 * @param platformID
	 *            The platform id.
	 * @param identification
	 *            The identification of this remote call.
	 * @param calling
	 *            If it is the remote call of the request (true) or the response (false).
	 * @return The {@link RemoteCallData}
	 */
	RemoteCallData getRemoteCallDataByIdentification(long platformID, long identification, boolean calling);
}
