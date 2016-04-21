package rocks.inspectit.shared.all.communication.data.diagnosis.results;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * This class represents a TimerData Object with the relevant timer information needed for the
 * {@link #problemOccurence} objects.
 *
 * @author Christian Voegele
 *
 */
public class TimerDataProblemOccurence implements Serializable {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = -2718587405825838307L;

	/**
	 * The CPU complete duration.
	 */
	@JsonProperty(value = "cpuDuration")
	private double cpuDuration = 0;

	/**
	 * The complete duration.
	 */
	@JsonProperty(value = "duration")
	private double duration = 0;

	/**
	 * The exclusive time.
	 */
	@JsonProperty(value = "exclusiveTime")
	private double exclusiveTime;

	/**
	 * Constructor.
	 *
	 * @param invocationSequenceData
	 *            The invocationSequenceData the timer data is taken into account
	 */
	public TimerDataProblemOccurence(final InvocationSequenceData invocationSequenceData) {
		if (null == invocationSequenceData.getTimerData()) {
			throw new IllegalArgumentException("TimerData cannot be null!");
		}
		this.cpuDuration = invocationSequenceData.getTimerData().getCpuDuration();
		this.duration = invocationSequenceData.getTimerData().getDuration();
		this.exclusiveTime = InvocationSequenceDataHelper.calculateExclusiveTime(invocationSequenceData);
	}

	/**
	 * Gets {@link #cpuDuration}.
	 *
	 * @return {@link #cpuDuration}
	 */
	public final double getCpuDuration() {
		return this.cpuDuration;
	}

	/**
	 * Sets {@link #cpuDuration}.
	 *
	 * @param cpuDuration
	 *            New value for {@link #cpuDuration}
	 */
	public final void setCpuDuration(double cpuDuration) {
		this.cpuDuration = cpuDuration;
	}

	/**
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	public final double getDuration() {
		return this.duration;
	}

	/**
	 * Sets {@link #duration}.
	 *
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public final void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Gets {@link #exclusiveTime}.
	 *
	 * @return {@link #exclusiveTime}
	 */
	public final double getExclusiveTime() {
		return this.exclusiveTime;
	}

	/**
	 * Sets {@link #exclusiveTime}.
	 *
	 * @param exclusiveTime
	 *            New value for {@link #exclusiveTime}
	 */
	public final void setExclusiveTime(double exclusiveTime) {
		this.exclusiveTime = exclusiveTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.cpuDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.duration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.exclusiveTime);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
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
		TimerDataProblemOccurence other = (TimerDataProblemOccurence) obj;
		if (Double.doubleToLongBits(this.cpuDuration) != Double.doubleToLongBits(other.cpuDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.exclusiveTime) != Double.doubleToLongBits(other.exclusiveTime)) {
			return false;
		}
		return true;
	}

}
