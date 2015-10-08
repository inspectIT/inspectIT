package info.novatec.inspectit.cmr.service;

import java.rmi.Remote;

/**
 * This service is used by the Agent to continuously send a keep-alive signal to the CMR.
 * 
 * @author Marius Oehler
 *
 */
@ServiceInterface(exporter = ServiceExporterType.RMI, serviceId = 3)
public interface IKeepAliveService extends Remote {

	/**
	 * The period in miliseconds of the keep-alive signals.
	 */
	int KA_PERIOD = 2500;

	/**
	 * The initial delay in milliseconds. After this time, the agent sends keep-alive signals.
	 */
	int KA_INITIAL_DELAY = 2500;

	/**
	 * The duration of a timeout after no keep-alive messages are received.
	 */
	int KA_TIMEOUT = 5000;

	/**
	 * Sends a keep-alive message of the platform with the given id.
	 * 
	 * @param platformId
	 *            ID of the platform ident.
	 */
	void sendKeepAlive(long platformId);

}
