package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.MapUtils;

import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;

/**
 * {@link MethodSensorAssignment} that can in addition mark starting of the invocation and minimum
 * duration of invocations to report.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "invocation-start-method-sensor-assignment")
public class InvocationStartMethodSensorAssignment extends MethodSensorAssignment {

	/**
	 * If it starts an invocation.
	 */
	@XmlAttribute(name = "starts-invocation")
	private Boolean startsInvocation = Boolean.FALSE;

	/**
	 * Invocation min duration.
	 */
	@XmlAttribute(name = "min-invocation-duration")
	private Long minInvocationDuration = Long.valueOf(0L);

	/**
	 * No-args constructor.
	 */
	public InvocationStartMethodSensorAssignment() {
	}

	/**
	 * Default constructor.
	 *
	 * @param sensorConfig
	 *            Method sensor config class begin assigned.
	 */
	public InvocationStartMethodSensorAssignment(Class<? extends IMethodSensorConfig> sensorConfig) {
		super(sensorConfig);
	}

	/**
	 * Secondary constructor.
	 *
	 * @param sensorConfig
	 *            Method sensor config class begin assigned.
	 * @param startsInvocation
	 *            Initial value for {@link #startsInvocation}.
	 */
	public InvocationStartMethodSensorAssignment(Class<? extends IMethodSensorConfig> sensorConfig, boolean startsInvocation) {
		super(sensorConfig);
		this.startsInvocation = Boolean.valueOf(startsInvocation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getSettings() {
		Map<String, Object> settings = super.getSettings();
		if (MapUtils.isEmpty(settings)) {
			settings = new HashMap<>();
		}

		// min duration
		if (minInvocationDuration > 0) {
			settings.put("minduration", minInvocationDuration);
		}

		return settings;
	}

	/**
	 * Gets {@link #startsInvocation}.
	 *
	 * @return {@link #startsInvocation}
	 */
	public boolean isStartsInvocation() {
		return startsInvocation.booleanValue();
	}

	/**
	 * Sets {@link #startsInvocation}.
	 *
	 * @param startsInvocation
	 *            New value for {@link #startsInvocation}
	 */
	public void setStartsInvocation(boolean startsInvocation) {
		this.startsInvocation = Boolean.valueOf(startsInvocation);
	}

	/**
	 * Gets {@link #minInvocationDuration}.
	 *
	 * @return {@link #minInvocationDuration}
	 */
	public long getMinInvocationDuration() {
		return minInvocationDuration.longValue();
	}

	/**
	 * Sets {@link #minInvocationDuration}.
	 *
	 * @param minInvocationDuration
	 *            New value for {@link #minInvocationDuration}
	 */
	public void setMinInvocationDuration(long minInvocationDuration) {
		this.minInvocationDuration = Long.valueOf(minInvocationDuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.minInvocationDuration == null) ? 0 : this.minInvocationDuration.hashCode());
		result = (prime * result) + ((this.startsInvocation == null) ? 0 : this.startsInvocation.hashCode());
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
		InvocationStartMethodSensorAssignment other = (InvocationStartMethodSensorAssignment) obj;
		if (this.minInvocationDuration == null) {
			if (other.minInvocationDuration != null) {
				return false;
			}
		} else if (!this.minInvocationDuration.equals(other.minInvocationDuration)) {
			return false;
		}
		if (this.startsInvocation == null) {
			if (other.startsInvocation != null) {
				return false;
			}
		} else if (!this.startsInvocation.equals(other.startsInvocation)) {
			return false;
		}
		return true;
	}

}
