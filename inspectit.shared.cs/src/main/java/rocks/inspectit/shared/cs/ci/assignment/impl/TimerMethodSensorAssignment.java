package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.MapUtils;

import rocks.inspectit.shared.cs.ci.context.AbstractContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.FieldContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ParameterContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ReturnContextCapture;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;

/**
 * Timer sensor assignment.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "timer-method-sensor-assignment")
public class TimerMethodSensorAssignment extends ChartingMethodSensorAssignment {

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
	 * List of context captures.
	 */
	@XmlElementWrapper(name = "context-captures", required = false)
	@XmlElementRefs({ @XmlElementRef(type = ReturnContextCapture.class), @XmlElementRef(type = ParameterContextCapture.class), @XmlElementRef(type = FieldContextCapture.class) })
	private List<AbstractContextCapture> contextCaptures;

	/**
	 * No-args constructor.
	 */
	public TimerMethodSensorAssignment() {
		super(TimerSensorConfig.class);
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
	 * Gets {@link #contextCaptures}.
	 *
	 * @return {@link #contextCaptures}
	 */
	public List<AbstractContextCapture> getContextCaptures() {
		return contextCaptures;
	}

	/**
	 * Sets {@link #contextCaptures}.
	 *
	 * @param contextCaptures
	 *            New value for {@link #contextCaptures}
	 */
	public void setContextCaptures(List<AbstractContextCapture> contextCaptures) {
		this.contextCaptures = contextCaptures;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.contextCaptures == null) ? 0 : this.contextCaptures.hashCode());
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
		TimerMethodSensorAssignment other = (TimerMethodSensorAssignment) obj;
		if (this.contextCaptures == null) {
			if (other.contextCaptures != null) {
				return false;
			}
		} else if (!this.contextCaptures.equals(other.contextCaptures)) {
			return false;
		}
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
