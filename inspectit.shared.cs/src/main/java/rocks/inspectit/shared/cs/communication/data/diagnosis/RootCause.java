package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

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
	@JsonProperty(value = "aggregatedDiagnosisTimerData")
	private AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData;

	/**
	 * This constructor creates new RootCause.
	 *
	 * @param methodIdent
	 *            The identification of the root cause method
	 * @param aggregatedDiagnosisTimerData
	 *            the aggregatedDiagnosisTimerData of the invocationSequenceData of the root Cause
	 */
	public RootCause(final long methodIdent, final AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData) {
		this.methodIdent = methodIdent;
		this.aggregatedDiagnosisTimerData = aggregatedDiagnosisTimerData;
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
	 * Gets {@link #aggregatedDiagnosisTimerData}.
	 *
	 * @return {@link #aggregatedDiagnosisTimerData}
	 */
	public final AggregatedDiagnosisTimerData getAggregatedDiagnosisTimerData() {
		return this.aggregatedDiagnosisTimerData;
	}

	/**
	 * Sets {@link #aggregatedDiagnosisTimerData}.
	 *
	 * @param aggregatedDiagnosisTimerData
	 *            New value for {@link #aggregatedDiagnosisTimerData}
	 */
	public final void setAggregatedDiagnosisTimerData(AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData) {
		this.aggregatedDiagnosisTimerData = aggregatedDiagnosisTimerData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
		result = (prime * result) + ((this.aggregatedDiagnosisTimerData == null) ? 0 : this.aggregatedDiagnosisTimerData.hashCode());
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
		if (this.aggregatedDiagnosisTimerData == null) {
			if (other.aggregatedDiagnosisTimerData != null) {
				return false;
			}
		} else if (!this.aggregatedDiagnosisTimerData.equals(other.aggregatedDiagnosisTimerData)) {
			return false;
		}
		return true;
	}

}