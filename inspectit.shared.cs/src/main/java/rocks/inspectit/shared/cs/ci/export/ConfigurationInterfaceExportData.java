package rocks.inspectit.shared.cs.ci.export;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;

/**
 * Simple POJO to represent exported configuration interface data.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configuration-interface-export-data")
public class ConfigurationInterfaceExportData {

	/**
	 * File extension constant.
	 */
	public static final String FILE_EXTENSION = ".xml";

	/**
	 * Exported {@link Profile}s.
	 */
	@XmlElementWrapper(name = "profiles")
	@XmlElementRef(type = Profile.class)
	private Collection<Profile> profiles;

	/**
	 * Exported {@link Environment}s.
	 */
	@XmlElementWrapper(name = "environments")
	@XmlElementRef(type = Environment.class)
	private Collection<Environment> environments;

	/**
	 * If export data is empty.
	 *
	 * @return If export data is empty.
	 */
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(profiles) && CollectionUtils.isEmpty(environments);
	}

	/**
	 * Gets {@link #profiles}.
	 *
	 * @return {@link #profiles}
	 */
	public Collection<Profile> getProfiles() {
		return profiles;
	}

	/**
	 * Sets {@link #profiles}.
	 *
	 * @param profiles
	 *            New value for {@link #profiles}
	 */
	public void setProfiles(Collection<Profile> profiles) {
		this.profiles = profiles;
	}

	/**
	 * Gets {@link #environments}.
	 *
	 * @return {@link #environments}
	 */
	public Collection<Environment> getEnvironments() {
		return environments;
	}

	/**
	 * Sets {@link #environments}.
	 *
	 * @param environments
	 *            New value for {@link #environments}
	 */
	public void setEnvironments(Collection<Environment> environments) {
		this.environments = environments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((environments == null) ? 0 : environments.hashCode());
		result = prime * result + ((profiles == null) ? 0 : profiles.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConfigurationInterfaceExportData other = (ConfigurationInterfaceExportData) obj;
		if (environments == null) {
			if (other.environments != null) {
				return false;
			}
		} else if (!environments.equals(other.environments)) {
			return false;
		}
		if (profiles == null) {
			if (other.profiles != null) {
				return false;
			}
		} else if (!profiles.equals(other.profiles)) {
			return false;
		}
		return true;
	}

}
