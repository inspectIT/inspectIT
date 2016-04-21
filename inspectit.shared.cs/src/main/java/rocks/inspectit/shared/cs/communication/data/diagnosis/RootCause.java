package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * This class represents a rootCause of a problem. The rootCause consists of a methodName, the timer
 * of the methodName and the underlying MethodCalls and TimerData.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class RootCause {

	/**
	 * The identification of the root cause method.
	 */
	@JsonProperty(value = "methodIdent")
	private long methodIdent;

	/**
	 * Relevant timerData of RootCause.
	 */
	@JsonProperty(value = "timerDataProblemOccurrence")
	private TimerDataProblemOccurrence timerDataProblemOccurrence;

	/**
	 * This constructor creates new RootCause.
	 *
	 * @param methodIdent
	 *            The identification of the root cause method
	 * @param timerData
	 *            the timerData of the invocationSequenceData of the root Cause
	 */
	public RootCause(final long methodIdent, final TimerData timerData) {
		this.methodIdent = methodIdent;
		this.timerDataProblemOccurrence = new TimerDataProblemOccurrence(timerData);
	}

	/**
	 * Gets {@link #methodIdent}.
	 *
	 * @return {@link #methodIdent}
	 */
	public final long getMethodIdent() {
		return this.methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 *
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public final void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * Gets {@link #timerDataProblemOccurrence}.
	 *
	 * @return {@link #timerDataProblemOccurrence}
	 */
	public final TimerDataProblemOccurrence getTimerDataProblemOccurrence() {
		return this.timerDataProblemOccurrence;
	}

	/**
	 * Sets {@link #timerDataProblemOccurrence}.
	 *
	 * @param timerDataProblemOccurrence
	 *            New value for {@link #timerDataProblemOccurrence}
	 */
	public final void setTimerDataProblemOccurrence(TimerDataProblemOccurrence timerDataProblemOccurrence) {
		this.timerDataProblemOccurrence = timerDataProblemOccurrence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
		result = (prime * result) + ((this.timerDataProblemOccurrence == null) ? 0 : this.timerDataProblemOccurrence.hashCode());
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
		RootCause other = (RootCause) obj;
		if (this.methodIdent != other.methodIdent) {
			return false;
		}
		if (this.timerDataProblemOccurrence == null) {
			if (other.timerDataProblemOccurrence != null) {
				return false;
			}
		} else if (!this.timerDataProblemOccurrence.equals(other.timerDataProblemOccurrence)) {
			return false;
		}
		return true;
	}

}