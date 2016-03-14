package rocks.inspectit.shared.cs.ci.profile.data;

import java.util.Objects;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract profile data class.
 *
 * @param <D>
 *            type of data it can return
 *
 * @author Ivan Senic
 *
 */
@XmlSeeAlso({ SensorAssignmentProfileData.class, ExcludeRulesProfileData.class, JmxDefinitionProfileData.class })
public abstract class AbstractProfileData<D> {

	/**
	 * Returns data maintained by this profile data.
	 *
	 * @return Returns data maintained by this profile data.
	 */
	public abstract D getData();

	/**
	 * Returns display name for this profile data.
	 *
	 * @return Returns display name for this profile data.
	 */
	public abstract String getName();

	/**
	 * Returns if the this profile data is same as of given class.
	 *
	 * @param profileDataClass
	 *            class to check.
	 * @return Returns if the this profile data is same as of given class.
	 *
	 * @param <A>
	 *            type of return data
	 * @param <E>
	 *            profile data class
	 */
	public <A, E extends AbstractProfileData<A>> boolean isOfType(Class<E> profileDataClass) {
		return Objects.equals(profileDataClass, getClass());
	}

	/**
	 * Returns this instance of the profile data if's instance of the given class.
	 *
	 * @param profileDataClass
	 *            class to check.
	 * @return Returns this instance of the profile data if's instance of the given class.
	 *
	 * @param <A>
	 *            type of return data
	 * @param <E>
	 *            profile data class
	 */
	@SuppressWarnings("unchecked")
	public <A, E extends AbstractProfileData<A>> E getIfInstance(Class<E> profileDataClass) {
		if (isOfType(profileDataClass)) {
			// safe to cast as
			return (E) this;
		} else {
			return null;
		}
	}

	/**
	 * Returns data holding by this profile data class if it's instance of the given class. If not
	 * then <code>null</code> is returned.
	 *
	 * @param profileDataClass
	 *            class to check.
	 * @return Returns data holding by this profile data class if it's instance of the given class.
	 *         If not then <code>null</code> is returned.
	 *
	 * @param <A>
	 *            type of return data
	 * @param <E>
	 *            profile data class
	 */
	@SuppressWarnings("unchecked")
	public <A, E extends AbstractProfileData<A>> A getData(Class<E> profileDataClass) {
		if (isOfType(profileDataClass)) {
			// safe to cast as
			return (A) getData();
		} else {
			return null;
		}
	}

}
