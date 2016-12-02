package rocks.inspectit.server.externalservice;

import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;

/**
 * Interface to query status information of an external service (like InfluxDB).
 *
 * @author Marius Oehler
 *
 */
public interface IExternalService {

	/**
	 * Returns the current status of the service.
	 *
	 * @return the current {@link ExternalServiceStatus}
	 */
	ExternalServiceStatus getServiceStatus();

	/**
	 * Returns the type of the service.
	 *
	 * @return the {@link ExternalServiceType}
	 */
	ExternalServiceType getServiceType();
}
