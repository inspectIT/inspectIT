package rocks.inspectit.ui.rcp.preferences.valueproviders;

import rocks.inspectit.ui.rcp.preferences.PreferenceException;
import rocks.inspectit.ui.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

/**
 * Provider that can save and retrieve an enum value.
 *
 * @param <E>
 *            the enumeration class
 *
 * @author Marius Oehler
 *
 */
public class EnumPreferenceValueProvider<E extends Enum<E>> extends PreferenceValueProvider<E> {

	/**
	 * Class of the enum.
	 */
	private Class<E> enumClass;

	/**
	 * @param enumClass
	 *            Class of the enum.
	 */
	public EnumPreferenceValueProvider(Class<E> enumClass) {
		this.enumClass = enumClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObjectValid(Object object) {
		return enumClass.isAssignableFrom(object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueForObject(E object) throws PreferenceException {
		return object.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getObjectFromValue(String value) throws PreferenceException {
		return Enum.valueOf(enumClass, value);
	}
}
