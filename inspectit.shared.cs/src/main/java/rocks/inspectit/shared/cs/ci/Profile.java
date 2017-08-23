package rocks.inspectit.shared.cs.ci;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;

/**
 * Profile defines different data based on the provided profile data.
 *
 * @see AbstractProfileData
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "profile")
public class Profile extends AbstractCiData {

	/**
	 * Is it the "common" profile.
	 */
	@XmlAttribute(name = "common")
	private Boolean commonProfile = Boolean.FALSE;

	/**
	 * If profile is active. Deactivating profiles means they are not used even when they are
	 * assigned to the Environments.
	 */
	@XmlAttribute(name = "active")
	private Boolean active = Boolean.TRUE;

	/**
	 * If the profile should be included in the Environment by default.
	 */
	@XmlAttribute(name = "default")
	private Boolean defaultProfile = Boolean.FALSE;

	/**
	 * Type of profile data this profile is holding.
	 */
	@XmlElementRef
	private AbstractProfileData<?> profileData;

	/**
	 * Gets {@link #name}.
	 * <p>
	 * If is common profile adds the prefix [Common] to the defined profile name.
	 *
	 * @return {@link #name}
	 */
	@Override
	public String getName() {
		if (isCommonProfile()) {
			return "[Common] " + super.getName();
		} else {
			return super.getName();
		}
	}

	/**
	 * Gets {@link #commonProfile}.
	 *
	 * @return {@link #commonProfile}
	 */
	public boolean isCommonProfile() {
		return commonProfile.booleanValue();
	}

	/**
	 * Gets {@link #active}.
	 *
	 * @return {@link #active}
	 */
	public boolean isActive() {
		return active.booleanValue();
	}

	/**
	 * Sets {@link #active}.
	 *
	 * @param active
	 *            New value for {@link #active}
	 */
	public void setActive(boolean active) {
		this.active = Boolean.valueOf(active);
	}

	/**
	 * Gets {@link #defaultProfile}.
	 *
	 * @return {@link #defaultProfile}
	 */
	public boolean isDefaultProfile() {
		return defaultProfile.booleanValue();
	}

	/**
	 * Sets {@link #defaultProfile}.
	 *
	 * @param defaultProfile
	 *            New value for {@link #defaultProfile}
	 */
	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = Boolean.valueOf(defaultProfile);
	}

	/**
	 * Gets {@link #profileData}.
	 *
	 * @return {@link #profileData}
	 */
	public AbstractProfileData<?> getProfileData() {
		return profileData;
	}

	/**
	 * Sets {@link #profileData}.
	 *
	 * @param profileData
	 *            New value for {@link #profileData}
	 */
	public void setProfileData(AbstractProfileData<?> profileData) {
		this.profileData = profileData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.active == null) ? 0 : this.active.hashCode());
		result = (prime * result) + ((this.commonProfile == null) ? 0 : this.commonProfile.hashCode());
		result = (prime * result) + ((this.defaultProfile == null) ? 0 : this.defaultProfile.hashCode());
		result = (prime * result) + ((this.profileData == null) ? 0 : this.profileData.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Profile other = (Profile) obj;
		if (this.active == null) {
			if (other.active != null) {
				return false;
			}
		} else if (!this.active.equals(other.active)) {
			return false;
		}
		if (this.commonProfile == null) {
			if (other.commonProfile != null) {
				return false;
			}
		} else if (!this.commonProfile.equals(other.commonProfile)) {
			return false;
		}
		if (this.defaultProfile == null) {
			if (other.defaultProfile != null) {
				return false;
			}
		} else if (!this.defaultProfile.equals(other.defaultProfile)) {
			return false;
		}
		if (this.profileData == null) {
			if (other.profileData != null) {
				return false;
			}
		} else if (!this.profileData.equals(other.profileData)) {
			return false;
		}
		return true;
	}

	/**
	 * Converts a given collection of profiles into a collection of profile names.
	 * 
	 * @param profiles
	 *            Collection of profiles
	 * @return Collection of all profile names
	 */
	public static Collection<String> convertProfileListToNameStringList(Collection<Profile> profiles) {
		Collection<String> profileNames = new ArrayList<String>();
		for (Profile profile : profiles) {
			profileNames.add(profile.getName());
		}
		return profileNames;
	}

}
