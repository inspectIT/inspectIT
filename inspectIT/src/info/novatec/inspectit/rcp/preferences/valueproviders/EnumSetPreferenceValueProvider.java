package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provider that can save and retrieve set of enum values.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class EnumSetPreferenceValueProvider<E extends Enum<E>> extends PreferenceValueProvider<Set<E>> {

	/**
	 * Class of the enum.
	 */
	private Class<E> enumClass;

	/**
	 * @param enumClass
	 *            Class of the enum.
	 */
	public EnumSetPreferenceValueProvider(Class<E> enumClass) {
		this.enumClass = enumClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObjectValid(Object object) {
		if (object instanceof Set) {
			Set<?> set = (Set<?>) object;
			for (Object inSet : set) {
				if (!enumClass.isAssignableFrom(inSet.getClass())) {
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
	@Override
	public String getValueForObject(Set<E> collection) throws PreferenceException {
		StringBuilder stringBuilder = new StringBuilder();
		for (E object : collection) {
			stringBuilder.append(object.toString() + PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<E> getObjectFromValue(String value) throws PreferenceException {
		Set<E> results = new HashSet<E>();
		StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
		while (tokenizer.hasMoreElements()) {
			results.add(Enum.valueOf(enumClass, tokenizer.nextToken()));
		}
		return results;
	}

}
