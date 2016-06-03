package rocks.inspectit.shared.cs.ci;

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
	private boolean commonProfile;

	/**
	 * If profile is active. Deactivating profiles means they are not used even when they are
	 * assigned to the Environments.
	 */
	@XmlAttribute(name = "active")
	private boolean active = true;

	/**
	 * If the profile should be included in the Environment by default.
	 */
	@XmlAttribute(name = "default")
	private boolean defaultProfile;

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
		if (commonProfile) {
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
		return commonProfile;
	}

	/**
	 * Gets {@link #active}.
	 *
	 * @return {@link #active}
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets {@link #active}.
	 *
	 * @param active
	 *            New value for {@link #active}
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets {@link #defaultProfile}.
	 *
	 * @return {@link #defaultProfile}
	 */
	public boolean isDefaultProfile() {
		return defaultProfile;
	}

	/**
	 * Sets {@link #defaultProfile}.
	 *
	 * @param defaultProfile
	 *            New value for {@link #defaultProfile}
	 */
	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = defaultProfile;
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
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + (commonProfile ? 1231 : 1237);
		result = prime * result + (defaultProfile ? 1231 : 1237);
		result = prime * result + ((profileData == null) ? 0 : profileData.hashCode());
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
		if (active != other.active) {
			return false;
		}
		if (commonProfile != other.commonProfile) {
			return false;
		}
		if (defaultProfile != other.defaultProfile) {
			return false;
		}
		if (profileData == null) {
			if (other.profileData != null) {
				return false;
			}
		} else if (!profileData.equals(other.profileData)) {
			return false;
		}
		return true;
	}

}
