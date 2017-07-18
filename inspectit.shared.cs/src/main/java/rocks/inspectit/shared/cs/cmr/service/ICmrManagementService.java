package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.cmr.CmrStatusData;
import rocks.inspectit.shared.cs.cmr.property.configuration.PropertySection;
import rocks.inspectit.shared.cs.cmr.property.update.configuration.ConfigurationUpdate;

/**
 * Service that provides general management of the CMR.
 *
 * @author Ivan Senic
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ICmrManagementService {

	/**
	 * Restarts the CMR by starting new CMR in separate JVM prior to executing shutdown.
	 */
	void restart();

	/**
	 * Shuts down the CMR.
	 */
	void shutdown();

	/**
	 * Clear whole buffer.
	 */
	void clearBuffer();

	/**
	 * Returns the current buffer status via {@link CmrStatusData}.
	 *
	 * @return {@link CmrStatusData}.
	 */
	CmrStatusData getCmrStatusData();

	/**
	 * Returns the currently existing {@link PropertySection} in the CMR configuration.
	 *
	 * @return Returns the currently existing {@link PropertySection} in the CMR configuration.
	 */
	Collection<PropertySection> getConfigurationPropertySections();

	/**
	 * Updates the current CMR configuration.
	 *
	 * @param configurationUpdate
	 *            {@link ConfigurationUpdate}
	 * @param executeRestart
	 *            If user has selected that restart should be automatically executed.
	 * @throws Exception
	 *             If {@link ConfigurationUpdate} contains not valid updates.
	 */
	void updateConfiguration(ConfigurationUpdate configurationUpdate, boolean executeRestart) throws Exception;
}
