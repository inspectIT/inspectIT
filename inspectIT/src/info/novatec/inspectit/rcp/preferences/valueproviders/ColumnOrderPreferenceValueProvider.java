package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Custom {@link PreferenceValueProvider} for the map of column orders in the tables.
 * 
 * @author Ivan Senic
 * 
 */
public class ColumnOrderPreferenceValueProvider extends PreferenceValueProvider<Map<Integer, int[]>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObjectValid(Object object) {
		return object instanceof Map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueForObject(Map<Integer, int[]> map) throws PreferenceException {
		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<Integer, int[]> entry : map.entrySet()) {
			stringBuilder.append(String.valueOf(entry.getKey()));
			stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
			for (int i : entry.getValue()) {
				stringBuilder.append(String.valueOf(i));
				stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
			}
			stringBuilder.append(PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, int[]> getObjectFromValue(String value) throws PreferenceException {
		Map<Integer, int[]> map = new HashMap<Integer, int[]>();
		StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
		while (tokenizer.hasMoreElements()) {
			String nextEntry = tokenizer.nextToken();
			String[] splitted = nextEntry.split(PreferencesConstants.PREF_SPLIT_REGEX);

			Integer key = null;
			try {
				key = Integer.valueOf(splitted[0]);
			} catch (Exception e) {
				throw new PreferenceException("Key value of the saved column order preference could not be loaded.", e);
			}

			int[] valueArray = new int[splitted.length - 1];
			for (int i = 1; i < splitted.length; i++) {
				try {
					valueArray[i - 1] = Integer.parseInt(splitted[i]);
				} catch (Exception e) {
					throw new PreferenceException("Value array of the saved column order preference could not be loaded.", e);
				}
			}

			map.put(key, valueArray);
		}
		return map;
	}

}
