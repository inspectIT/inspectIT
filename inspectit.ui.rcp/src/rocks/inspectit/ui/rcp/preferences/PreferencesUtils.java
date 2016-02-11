package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Utility for using preferences stores.
 * 
 * @author Ivan Senic
 * 
 */
public final class PreferencesUtils {

	/**
	 * Save the instance to the preference store.
	 */
	private static ScopedPreferenceStore preferenceStore = InspectIT.getDefault().getPreferenceStore();

	/**
	 * Private constructor.
	 */
	private PreferencesUtils() {
	}

	/**
	 * Saves a boolean value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveBooleanValue(String name, boolean value, boolean isDefault) {
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occurred trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Returns boolean value from the preferences. Same as calling
	 * {@link #getBooleanValue(PreferenceKey, false)}.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @return Boolean value.
	 */
	public static boolean getBooleanValue(String name) {
		return getBooleanValue(name, false);
	}

	/**
	 * Returns boolean value from the preferences.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param isDefault
	 *            Should default value be retrieved.
	 * @return Boolean value.
	 */
	public static boolean getBooleanValue(String name, boolean isDefault) {
		boolean value;
		if (isDefault) {
			value = preferenceStore.getDefaultBoolean(name);
		} else {
			value = preferenceStore.getBoolean(name);
		}
		return value;
	}

	/**
	 * Saves a double value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveDoubleValue(String name, double value, boolean isDefault) {
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occurred trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Returns double value from the preferences. Same as calling
	 * {@link #getDoubleValue(PreferenceKey, false)}.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @return Double value.
	 */
	public static double getDoubleValue(String name) {
		return getDoubleValue(name, false);
	}

	/**
	 * Returns double value from the preferences.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param isDefault
	 *            Should default value be retrieved.
	 * @return Double value.
	 */
	public static double getDoubleValue(String name, boolean isDefault) {
		double value;
		if (isDefault) {
			value = preferenceStore.getDefaultDouble(name);
		} else {
			value = preferenceStore.getDouble(name);
		}
		return value;
	}

	/**
	 * Saves a long value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveLongValue(String name, long value, boolean isDefault) {
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occurred trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Returns long value from the preferences. Same as calling
	 * {@link #getLongValue(PreferenceKey, false)}.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @return Long value.
	 */
	public static long getLongValue(String name) {
		return getLongValue(name, false);
	}

	/**
	 * Returns long value from the preferences.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param isDefault
	 *            Should default value be retrieved.
	 * @return Long value.
	 */
	public static long getLongValue(String name, boolean isDefault) {
		long value;
		if (isDefault) {
			value = preferenceStore.getDefaultLong(name);
		} else {
			value = preferenceStore.getLong(name);
		}
		return value;
	}

	/**
	 * Saves a int value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveIntValue(String name, int value, boolean isDefault) {
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occurred trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Returns int value from the preferences. Same as calling
	 * {@link #getIntValue(PreferenceKey, false)}.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @return Int value.
	 */
	public static int getIntValue(String name) {
		return getIntValue(name, false);
	}

	/**
	 * Returns int value from the preferences.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param isDefault
	 *            Should default value be retrieved.
	 * @return Int value.
	 */
	public static int getIntValue(String name, boolean isDefault) {
		int value;
		if (isDefault) {
			value = preferenceStore.getDefaultInt(name);
		} else {
			value = preferenceStore.getInt(name);
		}
		return value;
	}

	/**
	 * Saves a string value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveStringValue(String name, String value, boolean isDefault) {
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occurred trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Returns string value from the preferences. Same as calling
	 * {@link #getStringValue(PreferenceKey, false)}.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @return String value.
	 */
	public static String getStringValue(String name) {
		return getStringValue(name, false);
	}

	/**
	 * Returns string value from the preferences.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param isDefault
	 *            Should default value be retrieved.
	 * @return String value.
	 */
	public static String getStringValue(String name, boolean isDefault) {
		String value;
		if (isDefault) {
			value = preferenceStore.getDefaultString(name);
		} else {
			value = preferenceStore.getString(name);
		}
		return value;
	}

	/**
	 * General method for saving an object to the preference store. Note that the preference key has
	 * to be mapped in the {@link PreferenceValueProviderFactory} to the provider that can handle
	 * the type of object passed.
	 * 
	 * @param preferenceKey
	 *            Preference key.
	 * @param object
	 *            Object to save.
	 * @param isDefault
	 *            Is it a default preference value.
	 */
	public static void saveObject(String preferenceKey, Object object, boolean isDefault) {
		try {
			// if null is passed save still
			if (null == object) {
				saveStringValue(preferenceKey, "", isDefault);
				return;
			}

			String value = PreferenceValueProviderFactory.getValueForObject(preferenceKey, object);
			if (value != null && !"".equals(value)) {
				saveStringValue(preferenceKey, value, isDefault);
			}
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to save object to the preference store with preference key: " + preferenceKey, e, -1);
		}
	}

	/**
	 * General method for loading an object from the preference store. Note that the preference key
	 * has to be mapped in the {@link PreferenceValueProviderFactory} to the provider that can
	 * handle the type of type E passed.
	 * 
	 * @param <E>
	 *            Type of object.
	 * @param preferenceKey
	 *            Preference key.
	 * @return Saved object or null.
	 */
	public static <E> E getObject(String preferenceKey) {
		try {
			String value = preferenceStore.getString(preferenceKey);
			if (value == null || "".equals(value)) {
				return null;
			}
			return PreferenceValueProviderFactory.getObjectFromValue(preferenceKey, value);
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to load object from the preference store with preference key: " + preferenceKey, e, -1);
			return null;
		}
	}

	/**
	 * Loads the primitive collection from a preference store. Note that the preference key provided
	 * has to be mapped to the
	 * {@link info.novatec.inspectit.rcp.preferences.valueproviders.CollectionPreferenceValueProvider}
	 * in the {@link PreferenceValueProviderFactory}.
	 * 
	 * @param <E>
	 *            Type of objects in the collection.
	 * @param preferenceKey
	 *            Preference key.
	 * @param collection
	 *            Collection to add objects to. Can not be null.
	 * @param wantedClass
	 *            Runtime class of elements that need to be created in the collection.
	 */
	public static <E> void loadPrimitiveCollection(String preferenceKey, Collection<E> collection, Class<E> wantedClass) {
		String value = preferenceStore.getString(preferenceKey);
		if (value == null || "".equals(value)) {
			return;
		}
		try {
			Collection<String> stringCollection = PreferenceValueProviderFactory.getObjectFromValue(preferenceKey, value);
			StringToPrimitiveTransformUtil.transformStringCollection(stringCollection, collection, wantedClass);
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to load primitive collection from the preference store with preference key: " + preferenceKey, e, -1);
		}
	}

	/**
	 * Loads the primitive keys and values map from a preference store. Note that the preference key
	 * provided has to be mapped to the
	 * {@link info.novatec.inspectit.rcp.preferences.valueproviders.MapPreferenceValueProvider} in
	 * the {@link PreferenceValueProviderFactory}.
	 * 
	 * @param <K>
	 *            Type of key.
	 * @param <V>
	 *            Type of value.
	 * @param preferenceKey
	 *            Preference key.
	 * @param map
	 *            Map to put entries to. Can not be null.
	 * @param keyClass
	 *            Runtime class of elements that are keys.
	 * @param valueClass
	 *            Runtime class of elements that are values.
	 */
	public static <K, V> void loadPrimitiveMap(String preferenceKey, Map<K, V> map, Class<K> keyClass, Class<V> valueClass) {
		String value = preferenceStore.getString(preferenceKey);
		if (value == null || "".equals(value)) {
			return;
		}
		try {
			Map<String, String> stringCollection = PreferenceValueProviderFactory.getObjectFromValue(preferenceKey, value);
			StringToPrimitiveTransformUtil.transformStringMap(stringCollection, map, keyClass, valueClass);
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to load primitive map from the preference store with preference key: " + preferenceKey, e, -1);
		}
	}

	/**
	 * Save given repository definitions to the preference store.
	 * 
	 * @param repositoryDefinitions
	 *            {@link CmrRepositoryDefinition} to save.
	 * @param isDefault
	 *            Is it a default setting.
	 */
	public static void saveCmrRepositoryDefinitions(List<CmrRepositoryDefinition> repositoryDefinitions, boolean isDefault) {
		saveObject(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS, repositoryDefinitions, isDefault);
	}

	/**
	 * Returns the list of {@link CmrRepositoryDefinition} that exists in the preference store.
	 * 
	 * @return he list of {@link CmrRepositoryDefinition} that exists in the preference store.
	 */
	public static List<CmrRepositoryDefinition> getCmrRepositoryDefinitions() {
		return getObject(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS);
	}

}
