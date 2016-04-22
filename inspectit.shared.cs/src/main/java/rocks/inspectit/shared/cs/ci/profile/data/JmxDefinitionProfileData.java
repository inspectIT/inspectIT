package rocks.inspectit.shared.cs.ci.profile.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;

/**
 * Profile data holding {@link JmxBeanSensorAssignment}s.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jmx-definition-profile-data")
public class JmxDefinitionProfileData extends AbstractProfileData<List<JmxBeanSensorAssignment>> {

	/**
	 * Name.
	 */
	private static final String NAME = "JMX Definitions";

	/**
	 * Definitions of the JMX beans/attributes to monitor.
	 */
	@XmlElementRefs({ @XmlElementRef(type = JmxBeanSensorAssignment.class) })
	private List<JmxBeanSensorAssignment> jmxBeanAssignments;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxBeanSensorAssignment> getData() {
		return jmxBeanAssignments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Gets {@link #jmxBeanAssignments}.
	 *
	 * @return {@link #jmxBeanAssignments}
	 */
	public List<JmxBeanSensorAssignment> getJmxBeanAssignments() {
		return jmxBeanAssignments;
	}

	/**
	 * Sets {@link #jmxBeanAssignments}.
	 *
	 * @param jmxBeanAssignments
	 *            New value for {@link #jmxBeanAssignments}
	 */
	public void setJmxBeanAssignments(List<JmxBeanSensorAssignment> jmxBeanAssignments) {
		this.jmxBeanAssignments = jmxBeanAssignments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((jmxBeanAssignments == null) ? 0 : jmxBeanAssignments.hashCode());
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
		JmxDefinitionProfileData other = (JmxDefinitionProfileData) obj;
		if (jmxBeanAssignments == null) {
			if (other.jmxBeanAssignments != null) {
				return false;
			}
		} else if (!jmxBeanAssignments.equals(other.jmxBeanAssignments)) {
			return false;
		}
		return true;
	}

}
