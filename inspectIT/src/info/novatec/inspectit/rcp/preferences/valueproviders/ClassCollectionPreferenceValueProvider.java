package info.novatec.inspectit.rcp.preferences.valueproviders;

import java.util.Collection;
import java.util.HashSet;

/**
 * Extension of the {@link CollectionPreferenceValueProvider} that can save classes.
 * 
 * @author Ivan Senic
 * 
 */
public class ClassCollectionPreferenceValueProvider extends CollectionPreferenceValueProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Object> getCollectionForResults() {
		return new HashSet<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getValueForCollectionMember(Object object) {
		if (object instanceof Class) {
			return ((Class<?>) object).getName();
		}
		return super.getValueForCollectionMember(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getObjectForCollectionMember(String value) {
		try {
			return Class.forName(value);
		} catch (ClassNotFoundException e) {
			return super.getValueForCollectionMember(value);
		}
	}

}
