package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;

/**
 * This {@link PreferenceValueProvider} converts any collection that members in primitive warper
 * types to preference value. Later on this preference value will be transformed to a collection of
 * strings, and thus needs transformation to initial class of collection members.
 * 
 * @author Ivan Senic
 * 
 */
public class CollectionPreferenceValueProvider extends PreferenceValueProvider<Collection<?>> {

	/**
	 * Constant for denoting the empty collection.
	 */
	private static final String EMPTY_COLLECTION = "EMPTY_COLLECTION";

	/**
	 * {@inheritDoc}
	 */
	public boolean isObjectValid(Object object) {
		return object instanceof Collection;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueForObject(Collection<?> collection) throws PreferenceException {
		if (CollectionUtils.isEmpty(collection)) {
			return EMPTY_COLLECTION;
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			for (Object object : collection) {
				stringBuilder.append(getValueForCollectionMember(object) + PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			}
			return stringBuilder.toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<?> getObjectFromValue(String value) throws PreferenceException {
		if (EMPTY_COLLECTION.equals(value)) {
			return getCollectionForResults();
		} else {
			Collection<Object> results = getCollectionForResults();
			StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			while (tokenizer.hasMoreElements()) {
				results.add(getObjectForCollectionMember(tokenizer.nextToken()));
			}
			return results;
		}
	}

	/**
	 * Returns Collection type to use when creating resulting collection from strings.
	 * 
	 * @return Returns Collection type to use when creating resulting collection from strings.
	 */
	protected Collection<Object> getCollectionForResults() {
		return new ArrayList<Object>();
	}

	/**
	 * Returns String value for collection member.
	 * <p>
	 * Sub-classes can override.
	 * 
	 * @param object
	 *            Member.
	 * @return String value.
	 */
	protected String getValueForCollectionMember(Object object) {
		return object.toString();
	}

	/**
	 * Returns collection member for saved String value.
	 * <p>
	 * Sub-classes can override.
	 * 
	 * @param value
	 *            String value as object was saved..
	 * @return Collection member..
	 */
	protected Object getObjectForCollectionMember(String value) {
		return value;
	}

}
