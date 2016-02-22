package rocks.inspectit.ui.rcp.preferences.valueproviders;

import java.util.Objects;

import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.preferences.PreferenceException;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;

/**
 * Preference value provider that tries to save and load the last selected repository.
 * 
 * @author Ivan Senic
 * 
 */
public class LastSelectedRepositoryPreferenceValueProvider extends PreferenceValueProvider<RepositoryDefinition> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObjectValid(Object object) {
		return object instanceof RepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueForObject(RepositoryDefinition repositoryDefinition) throws PreferenceException {
		StringBuilder stringBuilder = new StringBuilder();
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			stringBuilder.append(CmrRepositoryDefinition.class.getName());
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
			stringBuilder.append(((CmrRepositoryDefinition) repositoryDefinition).getIp());
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
			stringBuilder.append(((CmrRepositoryDefinition) repositoryDefinition).getPort());
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			stringBuilder.append(StorageRepositoryDefinition.class.getName());
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
			stringBuilder.append(((StorageRepositoryDefinition) repositoryDefinition).getLocalStorageData().getId());
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getObjectFromValue(String value) throws PreferenceException {
		String[] splitted = value.split(PreferencesConstants.PREF_SPLIT_REGEX);
		if (splitted.length > 0) {
			String repositoryType = splitted[0];
			if (Objects.equals(repositoryType, CmrRepositoryDefinition.class.getName())) {
				if (splitted.length == 3) {
					String ip = splitted[1];
					String port = splitted[2];
					for (CmrRepositoryDefinition cmrRepositoryDefinition : InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions()) {
						if (Objects.equals(cmrRepositoryDefinition.getIp(), ip) && Objects.equals(String.valueOf(cmrRepositoryDefinition.getPort()), port)) {
							return cmrRepositoryDefinition;
						}
					}
				} else {
					throw new PreferenceException("Error trying to create last selected repository and agent from preference store.");
				}
			} else if (Objects.equals(repositoryType, StorageRepositoryDefinition.class.getName())) {
				if (splitted.length == 2) {
					String storageId = splitted[1];
					for (LocalStorageData localStorageData : InspectIT.getDefault().getInspectITStorageManager().getMountedAvailableStorages()) {
						if (Objects.equals(localStorageData.getId(), storageId)) {
							try {
								return InspectIT.getDefault().getInspectITStorageManager().getStorageRepositoryDefinition(localStorageData);
							} catch (Exception e) {
								throw new PreferenceException("Error trying to create a Storage repository definition from preference store.", e);
							}
						}
					}
				} else {
					throw new PreferenceException("Error trying to create last selected repository and agent from preference store.");
				}
			}
		}
		return null;
	}

}
