package rocks.inspectit.shared.cs.communication.data.diagnosis;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

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
	@JsonProperty(value = "timerDataProblemOccurence")
	private TimerDataProblemOccurence timerDataProblemOccurence;

	/**
	 * ArrayList of the {@link #timerData} of the calls of the rootCause.
	 */
	@JsonProperty(value = "TimerDataMethodInvocations")
	private List<TimerDataProblemOccurence> timerDataOfMethodInvocations = new ArrayList<TimerDataProblemOccurence>();

	/**
	 * This constructor creates new RootCause.
	 *
	 * @param methodIdent
	 *            The identification of the root cause method
	 * @param invocationSequenceData
	 *            the invocationSequenceData of the root Cause
	 * @param timerDataOfMethodInvocations
	 *            Map of underlying methodsNames mapped to the {@link #timerData} of the rootCause.
	 */
	public RootCause(final long methodIdent, final InvocationSequenceData invocationSequenceData, final List<TimerDataProblemOccurence> timerDataOfMethodInvocations) {
		this.methodIdent = methodIdent;
		this.timerDataProblemOccurence = new TimerDataProblemOccurence(invocationSequenceData);
		this.timerDataOfMethodInvocations = timerDataOfMethodInvocations;
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
	 * Gets {@link #timerDataProblemOccurence}.
	 *
	 * @return {@link #timerDataProblemOccurence}
	 */
	public final TimerDataProblemOccurence getTimerDataProblemOccurence() {
		return this.timerDataProblemOccurence;
	}

	/**
	 * Sets {@link #timerDataProblemOccurence}.
	 *
	 * @param timerDataProblemOccurence
	 *            New value for {@link #timerDataProblemOccurence}
	 */
	public final void setTimerDataProblemOccurence(TimerDataProblemOccurence timerDataProblemOccurence) {
		this.timerDataProblemOccurence = timerDataProblemOccurence;
	}

	/**
	 * Gets {@link #timerDataOfMethodInvocations}.
	 *
	 * @return {@link #timerDataOfMethodInvocations}
	 */
	public final List<TimerDataProblemOccurence> getTimerDataOfMethodInvocations() {
		return this.timerDataOfMethodInvocations;
	}

	/**
	 * Sets {@link #timerDataOfMethodInvocations}.
	 *
	 * @param timerDataOfMethodInvocations
	 *            New value for {@link #timerDataOfMethodInvocations}
	 */
	public final void setTimerDataOfMethodInvocations(List<TimerDataProblemOccurence> timerDataOfMethodInvocations) {
		this.timerDataOfMethodInvocations = timerDataOfMethodInvocations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
		result = (prime * result) + ((this.timerDataOfMethodInvocations == null) ? 0 : this.timerDataOfMethodInvocations.hashCode());
		result = (prime * result) + ((this.timerDataProblemOccurence == null) ? 0 : this.timerDataProblemOccurence.hashCode());
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
		if (this.timerDataOfMethodInvocations == null) {
			if (other.timerDataOfMethodInvocations != null) {
				return false;
			}
		} else if (!this.timerDataOfMethodInvocations.equals(other.timerDataOfMethodInvocations)) {
			return false;
		}
		if (this.timerDataProblemOccurence == null) {
			if (other.timerDataProblemOccurence != null) {
				return false;
			}
		} else if (!this.timerDataProblemOccurence.equals(other.timerDataProblemOccurence)) {
			return false;
		}
		return true;
	}


}