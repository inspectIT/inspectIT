package rocks.inspectit.shared.cs.cmr.service;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * Service to access the {@link RemoteCallData}.
 *
 * @author Thomas Kluge
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IRemoteCallDataAccessService {

	/**
	 * Returns a list of the {@link RemoteCallData} for a given template. In this template, only the
	 * platform id is extracted.
	 *
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @return The list of the {@link RemoteCallData} object.
	 */
	List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData);

	/**
	 * Returns a list of the remote call data for a given template. In this template, only the
	 * platform id is extracted.
	 *
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the {@link RemoteCallData} object.
	 */
	List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData, Date fromDate, Date toDate);

	/**
	 * Returns a remote call data object with the given platformID, identification and calling
	 * value. There 3 values are unique and identify a particular remote call data object.
	 *
	 * @param remotePlatformIdent
	 *            The remotePlatformIdent of the requesting platform.
	 * @param identification
	 *            The identitication of the remote call.
	 * @param calling
	 *            True if it is the request. False if it is the response.
	 * @return Returns the remote Call Object.
	 */
	RemoteCallData getRemoteCallData(long remotePlatformIdent, long identification, boolean calling);

}