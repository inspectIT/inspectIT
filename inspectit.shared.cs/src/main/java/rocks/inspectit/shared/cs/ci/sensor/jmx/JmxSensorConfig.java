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
	@XmlAttribute(name = "active")
	private boolean active = true;

	/**
	 * Option to force creation of the MBean server on the JMX sensor startup.
	 * <p>
	 * <code>false</code> by default.
	 */
	@XmlAttribute(name = "forceMBeanServerCreation")
	private boolean forceMBeanServer = false;

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
		return this.active;
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
	 * Gets {@link #forceMBeanServer}.
	 *
	 * @return {@link #forceMBeanServer}
	 */
	public boolean isForceMBeanServer() {
		return this.forceMBeanServer;
	}

	/**
	 * Sets {@link #forceMBeanServer}.
	 *
	 * @param forceMBeanServer
	 *            New value for {@link #forceMBeanServer}
	 */
	public void setForceMBeanServer(boolean forceMBeanServer) {
		this.forceMBeanServer = forceMBeanServer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (this.active ? 1231 : 1237);
		result = (prime * result) + (this.forceMBeanServer ? 1231 : 1237);
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
		if (this.active != other.active) {
			return false;
		}
		if (this.forceMBeanServer != other.forceMBeanServer) {
			return false;
		}
		return true;
	}

}
