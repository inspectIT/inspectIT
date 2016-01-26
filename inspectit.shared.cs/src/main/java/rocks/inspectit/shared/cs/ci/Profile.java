package rocks.inspectit.shared.cs.ci;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;

/**
 * Profile defines sensor assignments and exclude rules.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "profile")
public class Profile {

	/**
	 * Id of the profile.
	 */
	@XmlAttribute(name = "id", required = true)
	private String id;

	/**
	 * Name of the profile.
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * Date created.
	 */
	@XmlAttribute(name = "created-on", required = true)
	private Date createdDate;

	/**
	 * Date updated.
	 */
	@XmlAttribute(name = "updated-on")
	private Date updatedDate;

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
	 * Revision. Server for version control and updating control.
	 */
	@XmlAttribute(name = "revision")
	private int revision = 1;

	/**
	 * {@link MethodSensorAssignment}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = MethodSensorAssignment.class), @XmlElementRef(type = TimerMethodSensorAssignment.class) })
	private List<MethodSensorAssignment> methodSensorAssignments;

	/**
	 * {@link ExceptionSensorAssignment}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = ExceptionSensorAssignment.class) })
	private List<ExceptionSensorAssignment> exceptionSensorAssignments;

	/**
	 * {@link ExcludeRule}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = ExcludeRule.class) })
	private List<ExcludeRule> excludeRules;

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets {@link #name}.
	 * <p>
	 * If is common profile adds the prefix [Common] to the defined profile name.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		if (commonProfile) {
			return "[Common] " + name;
		} else {
			return name;
		}
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #description}.
	 *
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets {@link #description}.
	 *
	 * @param description
	 *            New value for {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets {@link #createdDate}.
	 *
	 * @return {@link #createdDate}
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets {@link #createdDate}.
	 *
	 * @param createdDate
	 *            New value for {@link #createdDate}
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Gets {@link #updatedDate}.
	 *
	 * @return {@link #updatedDate}
	 */
	public Date getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * Sets {@link #updatedDate}.
	 *
	 * @param updatedDate
	 *            New value for {@link #updatedDate}
	 */
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
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
	 * Gets {@link #revision}.
	 *
	 * @return {@link #revision}
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Sets {@link #revision}.
	 *
	 * @param revision
	 *            New value for {@link #revision}
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * Gets {@link #methodSensorAssignments}.
	 *
	 * @return {@link #methodSensorAssignments}
	 */
	public List<MethodSensorAssignment> getMethodSensorAssignments() {
		if (null == methodSensorAssignments) {
			return Collections.emptyList();
		}
		return methodSensorAssignments;
	}

	/**
	 * Sets {@link #methodSensorAssignments}.
	 *
	 * @param methodSensorAssignments
	 *            New value for {@link #methodSensorAssignments}
	 */
	public void setMethodSensorAssignments(List<MethodSensorAssignment> methodSensorAssignments) {
		this.methodSensorAssignments = methodSensorAssignments;
	}

	/**
	 * Gets {@link #exceptionSensorAssignments}.
	 *
	 * @return {@link #exceptionSensorAssignments}
	 */
	public List<ExceptionSensorAssignment> getExceptionSensorAssignments() {
		if (null == exceptionSensorAssignments) {
			return Collections.emptyList();
		}
		return exceptionSensorAssignments;
	}

	/**
	 * Sets {@link #exceptionSensorAssignments}.
	 *
	 * @param exceptionSensorAssignments
	 *            New value for {@link #exceptionSensorAssignments}
	 */
	public void setExceptionSensorAssignments(List<ExceptionSensorAssignment> exceptionSensorAssignments) {
		this.exceptionSensorAssignments = exceptionSensorAssignments;
	}

	/**
	 * Gets {@link #excludeRules}.
	 *
	 * @return {@link #excludeRules}
	 */
	public List<ExcludeRule> getExcludeRules() {
		if (null == excludeRules) {
			return Collections.emptyList();
		}
		return excludeRules;
	}

	/**
	 * Sets {@link #excludeRules}.
	 *
	 * @param excludeRules
	 *            New value for {@link #excludeRules}
	 */
	public void setExcludeRules(List<ExcludeRule> excludeRules) {
		this.excludeRules = excludeRules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + (commonProfile ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((exceptionSensorAssignments == null) ? 0 : exceptionSensorAssignments.hashCode());
		result = prime * result + ((excludeRules == null) ? 0 : excludeRules.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((methodSensorAssignments == null) ? 0 : methodSensorAssignments.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + revision;
		result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
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
		Profile other = (Profile) obj;
		if (active != other.active) {
			return false;
		}
		if (createdDate == null) {
			if (other.createdDate != null) {
				return false;
			}
		} else if (!createdDate.equals(other.createdDate)) {
			return false;
		}
		if (commonProfile != other.commonProfile) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (exceptionSensorAssignments == null) {
			if (other.exceptionSensorAssignments != null) {
				return false;
			}
		} else if (!exceptionSensorAssignments.equals(other.exceptionSensorAssignments)) {
			return false;
		}
		if (excludeRules == null) {
			if (other.excludeRules != null) {
				return false;
			}
		} else if (!excludeRules.equals(other.excludeRules)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodSensorAssignments == null) {
			if (other.methodSensorAssignments != null) {
				return false;
			}
		} else if (!methodSensorAssignments.equals(other.methodSensorAssignments)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (revision != other.revision) {
			return false;
		}
		if (updatedDate == null) {
			if (other.updatedDate != null) {
				return false;
			}
		} else if (!updatedDate.equals(other.updatedDate)) {
			return false;
		}
		return true;
	}

}
