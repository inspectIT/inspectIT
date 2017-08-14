package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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
		return true;
	}

}
