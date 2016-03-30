package info.novatec.inspectit.cmr.service.rest.unsafe;

import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Part of the special unsafe access to the CMR services built for the REST services.
 * 
 * This class is the center of the unsafe access chain and is responsible for persisting the method names.
 * 
 * The re-wiring of the interfaces is necessary, because the REST services do not provide authentication mechanisms.
 * 
 * The normal method names are mapped to unsafe methods, if there exists a permission test for the inspectIT client,
 * so the REST services still have unrestricted access.
 */
public class UnsafeCmrManagementService implements IUnsafeCmrManagementService {
	
	/**
	 * Linking the interfaces.
	 */
	@Autowired
	private IUnsafeEntryForCmrManagement entry;

	@Override
	public void restart() {
		entry.unsafeRestart();
	}
	
	@Override
	public void shutdown() {
		entry.unsafeShutdown();
	}

	@Override
	public void clearBuffer() {
		entry.clearBuffer();
	}

	@Override
	public CmrStatusData getCmrStatusData() {
		return entry.getCmrStatusData();
	}

	@Override
	public void addDroppedDataCount(int count) {
		entry.addDroppedDataCount(count);
	}

	@Override
	public int getDroppedDataCount() {
		return entry.getDroppedDataCount();
	}

	@Override
	public Collection<PropertySection> getConfigurationPropertySections() {
		return entry.getConfigurationPropertySections();
	}

	@Override
	public void updateConfiguration(ConfigurationUpdate configurationUpdate, boolean executeRestart) throws Exception {
		entry.updateConfiguration(configurationUpdate, executeRestart);
	}
}
