package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;

import java.util.Collection;

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
	 * Reports that an amount of data has been dropped.
	 * 
	 * @param count
	 *            Dropped amount.
	 */
	void addDroppedDataCount(int count);

	/**
	 * Returns the number of data objects that have been dropped on the CMR, due to the high
	 * incoming load.
	 * 
	 * @return Returns the number of data objects that have been dropped on the CMR, due to the high
	 *         incoming load.
	 */
	int getDroppedDataCount();

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
