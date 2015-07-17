package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;

/**
 * Value provider for list of {@link CmrRepositoryDefinition}s.
 * 
 * @author Ivan Senic
 * 
 */
class CmrRepositoryPreferenceValueProvider extends PreferenceValueProvider<List<CmrRepositoryDefinition>> {

	/**
	 * Constant for denoting the empty list.
	 */
	private static final String EMPTY_LIST = "EMPTY_LIST";

	/**
	 * {@inheritDoc}
	 */
	public boolean isObjectValid(Object object) {
		if (object instanceof List) {
			List<?> list = (List<?>) object;
			for (Object objectInCollection : list) {
				if (!objectInCollection.getClass().equals(CmrRepositoryDefinition.class)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueForObject(List<CmrRepositoryDefinition> repositoryDefinitions) {
		if (CollectionUtils.isEmpty(repositoryDefinitions)) {
			return EMPTY_LIST;
		} else {
			StringBuilder value = new StringBuilder();
			for (CmrRepositoryDefinition cmrRepositoryDefinition : repositoryDefinitions) {
				value.append(cmrRepositoryDefinition.getIp() + PreferencesConstants.PREF_SPLIT_REGEX + cmrRepositoryDefinition.getPort() + PreferencesConstants.PREF_SPLIT_REGEX
						+ cmrRepositoryDefinition.getName() + PreferencesConstants.PREF_SPLIT_REGEX);
				if (null != cmrRepositoryDefinition.getDescription()) {
					value.append(cmrRepositoryDefinition.getDescription());
				}
				value.append(PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			}
			return value.toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CmrRepositoryDefinition> getObjectFromValue(String value) throws PreferenceException {
		if (EMPTY_LIST.equals(value)) {
			return Collections.emptyList();
		} else {
			List<CmrRepositoryDefinition> returnList = new ArrayList<CmrRepositoryDefinition>();
			StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			while (tokenizer.hasMoreTokens()) {
				String nextValue = tokenizer.nextToken();
				String[] splitted = nextValue.split(PreferencesConstants.PREF_SPLIT_REGEX);
				CmrRepositoryDefinition cmrRepositoryDefinition = null;
				if (splitted.length == 2) {
					try {
						cmrRepositoryDefinition = new CmrRepositoryDefinition(splitted[0], Integer.parseInt(splitted[1]));
					} catch (Exception e) {
						throw new PreferenceException("Error trying to create a CMR repository definition from preference store.", e);
					}
				} else if (splitted.length > 2) {
					try {
						cmrRepositoryDefinition = new CmrRepositoryDefinition(splitted[0], Integer.parseInt(splitted[1]), splitted[2]);
					} catch (Exception e) {
						throw new PreferenceException("Error trying to create a CMR repository definition from preference store.", e);
					}
				} else {
					throw new PreferenceException("CMR repository definition values saved in the preference store are not correct. Received values via the string '" + nextValue + "' are "
							+ Arrays.asList(splitted) + ". Definition will be skipped.");
				}

				if (null != cmrRepositoryDefinition && splitted.length > 3) {
					cmrRepositoryDefinition.setDescription(splitted[3]);
				}
				returnList.add(cmrRepositoryDefinition);
			}
			return returnList;
		}
	}

}