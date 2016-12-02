package rocks.inspectit.shared.all.externalservice;

/**
 * Represents the status of an external service (like InfluxDB).
 *
 * @author Marius Oehler
 *
 */
public enum ExternalServiceStatus {

	/**
	 * The status is not known.
	 */
	UNKNOWN,

	/**
	 * The service is enabled and can be used.
	 */
	CONNECTED,

	/**
	 * The service is enabled but cannot be used.
	 */
	DISCONNECTED,

	/**
	 * The service is disabled.
	 */
	DISABLED;
}