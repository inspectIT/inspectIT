package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for providing the preference strings for objects and vice verse.
 * 
 * @author Ivan Senic
 * 
 */
public final class PreferenceValueProviderFactory {

	/**
	 * Private constructor.
	 */
	private PreferenceValueProviderFactory() {
	}

	/**
	 * List of active providers.
	 */
	private static Map<String, PreferenceValueProvider<?>> preferenceValueProviders;

	static {
		preferenceValueProviders = new HashMap<String, PreferenceValueProvider<?>>();
		preferenceValueProviders.put(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS, new CmrRepositoryPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.TABLE_COLUMN_SIZE_CACHE, new MapPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.HIDDEN_TABLE_COLUMN_CACHE, new CollectionPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.TABLE_COLUMN_ORDER_CACHE, new ColumnOrderPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.LAST_SELECTED_REPOSITORY, new LastSelectedRepositoryPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.INVOCATION_FILTER_DATA_TYPES, new ClassCollectionPreferenceValueProvider());
		preferenceValueProviders.put(PreferencesConstants.JMX_PLOT_DATA_SOLVER, new MapPreferenceValueProvider());
	}

	/**
	 * Returns a String preference value for a given preference key and object. Note that key
	 * provided has to match with the {@link PreferenceValueProvider} key that works with the same
	 * object types as provided E type.
	 * 
	 * @param <E>
	 *            Type of object.
	 * @param preferenceKey
	 *            Preference key.
	 * @param object
	 *            Object to create preference value for.
	 * @return String.
	 * @throws PreferenceException
	 *             If exception occurs during execution.
	 */
	@SuppressWarnings("unchecked")
	public static <E> String getValueForObject(String preferenceKey, E object) throws PreferenceException {
		PreferenceValueProvider<?> preferenceValueProvider = preferenceValueProviders.get(preferenceKey);
		if (null != preferenceValueProvider) {
			if (preferenceValueProvider.isObjectValid(object)) {
				return ((PreferenceValueProvider<E>) preferenceValueProvider).getValueForObject(object);
			} else {
				throw new PreferenceException("Preference value for key " + preferenceKey + " could  not be obtained because the supplied object is not valid.");
			}
		} else {
			throw new PreferenceException("Preference value provider was not found for preference key: " + preferenceKey);
		}
	}

	/**
	 * Returns a object from a string preference value for a given preference key. Note that key
	 * provided has to match with the {@link PreferenceValueProvider} key that works with the same
	 * object types as provided E type.
	 * 
	 * @param <E>
	 *            Type of object.
	 * @param preferenceKey
	 *            Preference key.
	 * @param value
	 *            String preference value.
	 * @return Object.
	 * @throws PreferenceException
	 *             If exception occurs during execution.
	 */
	@SuppressWarnings("unchecked")
	public static <E> E getObjectFromValue(String preferenceKey, String value) throws PreferenceException {
		PreferenceValueProvider<?> preferenceValueProvider = preferenceValueProviders.get(preferenceKey);
		if (null != preferenceValueProvider) {
			return ((PreferenceValueProvider<E>) preferenceValueProvider).getObjectFromValue(value);
		} else {
			throw new PreferenceException("Preference value provider was not found for preference key: " + preferenceKey);
		}
	}

	/**
	 * Abstract class for preference value providers.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 *            Type that is provider working with.
	 */
	abstract static class PreferenceValueProvider<E> {

		/**
		 * @param object
		 *            Object to check.
		 * @return Returns the provider can return a preference value for the object.
		 */
		public abstract boolean isObjectValid(Object object);

		/**
		 * Returns a String for the object.
		 * 
		 * @param object
		 *            Object.
		 * @return String to save.
		 * @throws PreferenceException
		 *             If exception occurs during execution.
		 */
		public abstract String getValueForObject(E object) throws PreferenceException;

		/**
		 * Returns a object from String.
		 * 
		 * @param value
		 *            Previously saved string.
		 * @return Object of type
		 * @throws PreferenceException
		 *             If exception occurs during execution.
		 */
		public abstract E getObjectFromValue(String value) throws PreferenceException;

	}
}
