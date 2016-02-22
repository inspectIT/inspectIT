package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.shared.cs.storage.LocalStorageData;

/**
 * Interface for all model components that can provide the information about the local storage.
 * 
 * @author Ivan Senic
 * 
 */
public interface ILocalStorageDataProvider {

	/**
	 * @return Returns the {@link LocalStorageData}.
	 */
	LocalStorageData getLocalStorageData();
}
