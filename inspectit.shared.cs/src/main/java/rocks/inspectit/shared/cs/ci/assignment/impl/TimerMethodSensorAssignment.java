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
public class TimerMethodSensorAssignment extends MethodSensorAssignment {

	/**
	 * If it is charting.
	 */
	@XmlAttribute(name = "charting")
	private boolean charting;

	/**
	 * If it starts an invocation.
	 */
	@XmlAttribute(name = "starts-invocation")
	private boolean startsInvocation;

	/**
	 * Invocation min duration.
	 */
	@XmlAttribute(name = "min-invocation-duration")
	private long minInvocationDuration;

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

		// charting
		if (charting) {
			settings.put("charting", Boolean.TRUE);
		}

		// min duration
		if (minInvocationDuration > 0) {
			settings.put("minduration", minInvocationDuration);
		}

		return settings;
	}

	/**
	 * Gets {@link #charting}.
	 *
	 * @return {@link #charting}
	 */
	public boolean isCharting() {
		return charting;
	}

	/**
	 * Sets {@link #charting}.
	 *
	 * @param charting
	 *            New value for {@link #charting}
	 */
	public void setCharting(boolean charting) {
		this.charting = charting;
	}

	/**
	 * Gets {@link #startsInvocation}.
	 *
	 * @return {@link #startsInvocation}
	 */
	public boolean isStartsInvocation() {
		return startsInvocation;
	}

	/**
	 * Sets {@link #startsInvocation}.
	 *
	 * @param startsInvocation
	 *            New value for {@link #startsInvocation}
	 */
	public void setStartsInvocation(boolean startsInvocation) {
		this.startsInvocation = startsInvocation;
	}

	/**
	 * Gets {@link #minInvocationDuration}.
	 *
	 * @return {@link #minInvocationDuration}
	 */
	public long getMinInvocationDuration() {
		return minInvocationDuration;
	}

	/**
	 * Sets {@link #minInvocationDuration}.
	 *
	 * @param minInvocationDuration
	 *            New value for {@link #minInvocationDuration}
	 */
	public void setMinInvocationDuration(long minInvocationDuration) {
		this.minInvocationDuration = minInvocationDuration;
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
		result = prime * result + (charting ? 1231 : 1237);
		result = prime * result + ((contextCaptures == null) ? 0 : contextCaptures.hashCode());
		result = prime * result + (int) (minInvocationDuration ^ (minInvocationDuration >>> 32));
		result = prime * result + (startsInvocation ? 1231 : 1237);
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
		if (charting != other.charting) {
			return false;
		}
		if (contextCaptures == null) {
			if (other.contextCaptures != null) {
				return false;
			}
		} else if (!contextCaptures.equals(other.contextCaptures)) {
			return false;
		}
		if (minInvocationDuration != other.minInvocationDuration) {
			return false;
		}
		if (startsInvocation != other.startsInvocation) {
			return false;
		}
		return true;
	}

}
