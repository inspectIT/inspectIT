package rocks.inspectit.shared.cs.ci.sensor.jmx;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;

/**
 * Configuration for the JMX sensor.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "jmx-loading-sensor-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class JmxSensorConfig implements ISensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.jmx.JmxSensor";

	/**
	 * Defines is sensor is active.
	 * <p>
	 * <code>true</code> by default.
	 */
	@XmlAttribute(name = "active", required = false)
	private Boolean active = Boolean.TRUE;

	/**
	 * Option to force creation of the MBean server on the JMX sensor startup.
	 * <p>
	 * <code>false</code> by default.
	 */
	@XmlAttribute(name = "forceMBeanServerCreation", required = false)
	private Boolean forceMBeanServer = Boolean.FALSE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getParameters() {
		return Collections.<String, Object> singletonMap("forceMBeanServer", forceMBeanServer);
	}

	/**
	 * Gets {@link #active}.
	 *
	 * @return {@link #active}
	 */
	public boolean isActive() {
		return this.active.booleanValue();
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
	 * Gets {@link #forceMBeanServer}.
	 *
	 * @return {@link #forceMBeanServer}
	 */
	public boolean isForceMBeanServer() {
		return this.forceMBeanServer.booleanValue();
	}

	/**
	 * Sets {@link #forceMBeanServer}.
	 *
	 * @param forceMBeanServer
	 *            New value for {@link #forceMBeanServer}
	 */
	public void setForceMBeanServer(boolean forceMBeanServer) {
		this.forceMBeanServer = Boolean.valueOf(forceMBeanServer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.active == null) ? 0 : this.active.hashCode());
		result = (prime * result) + ((this.forceMBeanServer == null) ? 0 : this.forceMBeanServer.hashCode());
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
		JmxSensorConfig other = (JmxSensorConfig) obj;
		if (this.active == null) {
			if (other.active != null) {
				return false;
			}
		} else if (!this.active.equals(other.active)) {
			return false;
		}
		if (this.forceMBeanServer == null) {
			if (other.forceMBeanServer != null) {
				return false;
			}
		} else if (!this.forceMBeanServer.equals(other.forceMBeanServer)) {
			return false;
		}
		return true;
	}

}
