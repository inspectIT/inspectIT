package rocks.inspectit.shared.cs.ci.sensor.platform;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;

/**
 * Abstract class for all platform sensor configurations.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ ClassLoadingSensorConfig.class, CompilationSensorConfig.class, CpuSensorConfig.class, MemorySensorConfig.class, RuntimeSensorConfig.class, SystemSensorConfig.class,
		ThreadSensorConfig.class })
public abstract class AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Defines is sensor is active.
	 * <p>
	 * <code>true</code> by default.
	 */
	@XmlAttribute(name = "active", required = true)
	private boolean active = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

	/**
	 * Gets {@link #active}.
	 *
	 * @return {@link #active}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (active ? 1231 : 1237);
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
		AbstractPlatformSensorConfig other = (AbstractPlatformSensorConfig) obj;
		if (active != other.active) {
			return false;
		}
		return true;
	}

}
