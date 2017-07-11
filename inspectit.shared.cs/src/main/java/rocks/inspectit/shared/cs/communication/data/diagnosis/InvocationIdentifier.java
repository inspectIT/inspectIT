package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * Abstract representation of an InvocationSequenceData, including the id to the methodName, the Id
 * to the InvocationSequenceData and the relevant {@link #TimerDataProblemOccurrence}.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class InvocationIdentifier {

	/**
	 * The id to the methodName.
	 */
	@JsonProperty(value = "methodIdent")
	private long methodIdent;

	/**
	 * The id to the invocationId.
	 */
	@JsonProperty(value = "invocationId")
	private long invocationId;

	/**
	 * Representation of relevant timerData.
	 */
	@JsonProperty(value = "diagnosisTimerData")
	private final DiagnosisTimerData diagnosisTimerData;

	/**
	 * Create new InvocationIdentifier based on InvocationSequenceData.
	 *
	 * @param invocationSequenceData
	 *            the InvocationIdentifier the InvocationIdentifier is created
	 */
	public InvocationIdentifier(final InvocationSequenceData invocationSequenceData) {
		if (null == invocationSequenceData) {
			throw new IllegalArgumentException("Cannot create InvocationIdentifier when invocationSequenceData is null.");
		}

		this.methodIdent = invocationSequenceData.getMethodIdent();
		this.invocationId = invocationSequenceData.getId();

		TimerData timerData = null;
		if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
			timerData = invocationSequenceData.getSqlStatementData();
		} else if (InvocationSequenceDataHelper.hasTimerData(invocationSequenceData)) {
			timerData = invocationSequenceData.getTimerData();
		} else {
			throw new IllegalStateException("Timer data unknown.");
		}

		this.diagnosisTimerData = new DiagnosisTimerData(timerData);
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
	 * Gets {@link #invocationId}.
	 *
	 * @return {@link #invocationId}
	 */
	public final long getInvocationId() {
		return this.invocationId;
	}

	/**
	 * Sets {@link #invocationId}.
	 *
	 * @param invocationId
	 *            New value for {@link #invocationId}
	 */
	public final void setInvocationId(long invocationId) {
		this.invocationId = invocationId;
	}

	/**
	 * Gets {@link #diagnosisTimerData}.
	 *
	 * @return {@link #diagnosisTimerData}
	 */
	public DiagnosisTimerData getDiagnosisTimerData() {
		return this.diagnosisTimerData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.diagnosisTimerData == null) ? 0 : this.diagnosisTimerData.hashCode());
		result = (prime * result) + (int) (this.invocationId ^ (this.invocationId >>> 32));
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
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
		InvocationIdentifier other = (InvocationIdentifier) obj;
		if (this.diagnosisTimerData == null) {
			if (other.diagnosisTimerData != null) {
				return false;
			}
		} else if (!this.diagnosisTimerData.equals(other.diagnosisTimerData)) {
			return false;
		}
		if (this.invocationId != other.invocationId) {
			return false;
		}
		if (this.methodIdent != other.methodIdent) {
			return false;
		}
		return true;
	}


}
