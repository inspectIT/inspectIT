package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.assignment.ISensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;

/**
 * MBean assignment. Defines one or more monitoring JMX attributes.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jmx-bean-assignment")
public class JmxBeanSensorAssignment implements ISensorAssignment<JmxSensorConfig> {

	/**
	 * Domain that the MBean object belongs to.
	 */
	@XmlAttribute(name = "domain", required = true)
	private String domain;

	/**
	 * Map of object name parameters to be used for finding the object name.
	 */
	@XmlElementWrapper(name = "object-name-parameters", required = true)
	private Map<String, String> objectNameParameters;

	/**
	 * List of attributes to monitor.
	 */
	@XmlElementWrapper(name = "attributes", required = true)
	private Set<String> attributes;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends JmxSensorConfig> getSensorConfigClass() {
		return JmxSensorConfig.class;
	}

	/**
	 * Gets {@link #domain}.
	 *
	 * @return {@link #domain}
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Sets {@link #domain}.
	 *
	 * @param domain
	 *            New value for {@link #domain}
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Gets {@link #objectNameParameters}.
	 *
	 * @return {@link #objectNameParameters}
	 */
	public Map<String, String> getObjectNameParameters() {
		return objectNameParameters;
	}

	/**
	 * Sets {@link #objectNameParameters}.
	 *
	 * @param objectNameParameters
	 *            New value for {@link #objectNameParameters}
	 */
	public void setObjectNameParameters(Map<String, String> objectNameParameters) {
		this.objectNameParameters = objectNameParameters;
	}

	/**
	 * Gets {@link #attributes}.
	 *
	 * @return {@link #attributes}
	 */
	public Set<String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets {@link #attributes}.
	 *
	 * @param attributes
	 *            New value for {@link #attributes}
	 */
	public void setAttributes(Set<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((attributes == null) ? 0 : attributes.hashCode());
		result = (prime * result) + ((domain == null) ? 0 : domain.hashCode());
		result = (prime * result) + ((objectNameParameters == null) ? 0 : objectNameParameters.hashCode());
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
		JmxBeanSensorAssignment other = (JmxBeanSensorAssignment) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		if (objectNameParameters == null) {
			if (other.objectNameParameters != null) {
				return false;
			}
		} else if (!objectNameParameters.equals(other.objectNameParameters)) {
			return false;
		}
		return true;
	}

}
