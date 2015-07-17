package info.novatec.inspectit.cmr.service;

/**
 * This interface is used to retrieve the status of the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IServerStatusService {

	/**
	 * Enumeration that denotes the server status.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum ServerStatus {

		/**
		 * Server offline.
		 */
		SERVER_OFFLINE,

		/**
		 * Server online.
		 */
		SERVER_ONLINE,

		/**
		 * Server starting.
		 */
		SERVER_STARTING,

		/**
		 * Server stopping.
		 */
		SERVER_STOPPING;

		/**
		 * The key that denotes the current state of the IDs in the registration database. The
		 * cached data service must check against this key and refresh the cache if the key is not
		 * the same as on the last check of the key.
		 */
		private String registrationIdsValidationKey;

		/**
		 * Gets {@link #registrationIdsValidationKey}.
		 * 
		 * @return {@link #registrationIdsValidationKey}
		 */
		public String getRegistrationIdsValidationKey() {
			return registrationIdsValidationKey;
		}

		/**
		 * Sets {@link #registrationIdsValidationKey}.
		 * 
		 * @param registrationIdsValidationKey
		 *            New value for {@link #registrationIdsValidationKey}
		 */
		public void setRegistrationIdsValidationKey(String registrationIdsValidationKey) {
			this.registrationIdsValidationKey = registrationIdsValidationKey;
		}

	}

	/**
	 * String returned for version not available.
	 */
	String VERSION_NOT_AVAILABLE = "n/a";

	/**
	 * Returns the current server status.
	 * 
	 * @return The server status.
	 */
	ServerStatus getServerStatus();

	/**
	 * Returns the current version of the server.
	 * 
	 * @return the current version of the server.
	 */
	String getVersion();

}
