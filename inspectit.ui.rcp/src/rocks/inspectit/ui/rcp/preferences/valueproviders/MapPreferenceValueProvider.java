package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections.MapUtils;

/**
 * This {@link PreferenceValueProvider} converts any map that has keys and values in primitive
 * warper types to preference value. Later on this preference value will be transformed to a map
 * that has both string as key and value, and thus needs transformation to initial classes of keys
 * and values.
 * 
 * @author Ivan Senic
 * 
 */
public class MapPreferenceValueProvider extends PreferenceValueProvider<Map<?, ?>> {

	/**
	 * Constant for denoting the empty map.
	 */
	private static final String EMPTY_MAP = "EMPTY_MAP";

	/**
	 * {@inheritDoc}
	 */
	public boolean isObjectValid(Object object) {
		return object instanceof Map;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueForObject(Map<?, ?> map) throws PreferenceException {
		if (MapUtils.isEmpty(map)) {
			return EMPTY_MAP;
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				stringBuilder.append(String.valueOf(entry.getKey()));
				stringBuilder.append(PreferencesConstants.PREF_SPLIT_REGEX);
				stringBuilder.append(entry.getValue());
				stringBuilder.append(PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			}
			return stringBuilder.toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<?, ?> getObjectFromValue(String value) throws PreferenceException {
		if (EMPTY_MAP.equals(value)) {
			return Collections.emptyMap();
		} else {
			Map<String, String> map = new HashMap<String, String>();
			StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			while (tokenizer.hasMoreElements()) {
				String nextEntry = tokenizer.nextToken();
				String[] splitted = nextEntry.split(PreferencesConstants.PREF_SPLIT_REGEX);
				if (splitted.length == 2) {
					map.put(splitted[0], splitted[1]);
				} else {
					throw new PreferenceException("Error loading map entry for the map saved in the preference store are not correct.  Entry key and value received values via the string '"
							+ nextEntry + "' are " + Arrays.asList(splitted) + ". Definition will be skipped.");
				}
			}
			return map;
		}
	}

}
