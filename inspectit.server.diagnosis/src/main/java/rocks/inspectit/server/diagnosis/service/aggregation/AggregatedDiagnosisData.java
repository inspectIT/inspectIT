package rocks.inspectit.server.diagnosis.service.aggregation;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;

/**
 * Aggregated {@link InvocationSequenceData} object.
 *
 * @author Alexander Wert, Christian Voegele, Ivan Senic
 *
 */
public class AggregatedDiagnosisData {

	/**
	 * List of {@link InvocationSequenceData} instances that are part of the aggregation.
	 */
	private final List<InvocationSequenceData> rawInvocationsSequenceElements = new ArrayList<InvocationSequenceData>(1);

	/**
	 * Timing information.
	 */
	private AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData;

	/**
	 * Source type.
	 */
	private final SourceType sourceType;

	/**
	 * Method id.
	 */
	private final long methodIdent;

	/**
	 * The key used for the aggregation.
	 */
	private final Object aggregationKey;

	/**
	 * Default constructor.
	 *
	 * @param sourceType
	 *            timer sourceType
	 * @param invocationSequenceData
	 *            invocationSequenceData is used to aggregate to AggregatedDiagnosisData
	 * @param aggregationKey
	 *            aggregationKey of AggregatedDiagnosisData
	 */
	public AggregatedDiagnosisData(final SourceType sourceType, final InvocationSequenceData invocationSequenceData, final Object aggregationKey) {
		this.sourceType = sourceType;
		this.methodIdent = invocationSequenceData.getMethodIdent();
		this.aggregationKey = aggregationKey;
		aggregate(invocationSequenceData);
	}

	/**
	 * Aggregate by adding a new InvocationSequenceData.
	 *
	 * @param invocationSequenceData
	 *            InvocationSequenceData to be added to the aggregation
	 */
	public void aggregate(InvocationSequenceData invocationSequenceData) {
		TimerData timerData = null;
		switch (sourceType) {
		case DATABASE:
			timerData = invocationSequenceData.getSqlStatementData();
			break;
		case HTTP:
			timerData = invocationSequenceData.getTimerData();
			break;
		case TIMERDATA:
			timerData = invocationSequenceData.getTimerData();
			break;
		default:
			throw new IllegalStateException("Source type unknown.");
		}

		// aggregate the timer data
		if (aggregatedDiagnosisTimerData == null) {
			aggregatedDiagnosisTimerData = new AggregatedDiagnosisTimerData(timerData);
		} else {
			aggregatedDiagnosisTimerData.aggregate(timerData);
		}

		// save involved invocation
		rawInvocationsSequenceElements.add(invocationSequenceData);
	}

	/**
	 * Gets {@link #rawInvocationsSequenceElements}.
	 *
	 * @return {@link #rawInvocationsSequenceElements}
	 */
	public List<InvocationSequenceData> getRawInvocationsSequenceElements() {
		return rawInvocationsSequenceElements;
	}

	/**
	 * Returns the number of {@link InvocationSequenceData} instances that are part of the
	 * aggregation.
	 *
	 * @return Returns the number of {@link InvocationSequenceData} instances that are part of the
	 *         aggregation.
	 */
	public int size() {
		return rawInvocationsSequenceElements.size();
	}

	/**
	 * Gets {@link #sourceType}.
	 *
	 * @return {@link #sourceType}
	 */
	public SourceType getSourceType() {
		return this.sourceType;
	}

	/**
	 * Gets {@link #methodIdent}.
	 *
	 * @return {@link #methodIdent}
	 */
	public long getMethodIdent() {
		return this.methodIdent;
	}

	/**
	 * Gets {@link #aggregatedDiagnosisTimerData}.
	 *
	 * @return {@link #aggregatedDiagnosisTimerData}
	 */
	public AggregatedDiagnosisTimerData getAggregatedDiagnosisTimerData() {
		return this.aggregatedDiagnosisTimerData;
	}

	/**
	 * Sets {@link #aggregatedDiagnosisTimerData}.
	 *
	 * @param aggregatedDiagnosisTimerData
	 *            New value for {@link #aggregatedDiagnosisTimerData}
	 */
	public void setAggregatedDiagnosisTimerData(AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData) {
		this.aggregatedDiagnosisTimerData = aggregatedDiagnosisTimerData;
	}

	/**
	 * Gets {@link #aggregationKey}.
	 *
	 * @return {@link #aggregationKey}
	 */
	public Object getAggregationKey() {
		return this.aggregationKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.aggregatedDiagnosisTimerData == null) ? 0 : this.aggregatedDiagnosisTimerData.hashCode());
		result = (prime * result) + ((this.aggregationKey == null) ? 0 : this.aggregationKey.hashCode());
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
		result = (prime * result) + ((this.rawInvocationsSequenceElements == null) ? 0 : this.rawInvocationsSequenceElements.hashCode());
		result = (prime * result) + ((this.sourceType == null) ? 0 : this.sourceType.hashCode());
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
		AggregatedDiagnosisData other = (AggregatedDiagnosisData) obj;
		if (this.aggregatedDiagnosisTimerData == null) {
			if (other.aggregatedDiagnosisTimerData != null) {
				return false;
			}
		} else if (!this.aggregatedDiagnosisTimerData.equals(other.aggregatedDiagnosisTimerData)) {
			return false;
		}
		if (this.aggregationKey == null) {
			if (other.aggregationKey != null) {
				return false;
			}
		} else if (!this.aggregationKey.equals(other.aggregationKey)) {
			return false;
		}
		if (this.methodIdent != other.methodIdent) {
			return false;
		}
		if (this.rawInvocationsSequenceElements == null) {
			if (other.rawInvocationsSequenceElements != null) {
				return false;
			}
		} else if (!this.rawInvocationsSequenceElements.equals(other.rawInvocationsSequenceElements)) {
			return false;
		}
		if (this.sourceType != other.sourceType) {
			return false;
		}
		return true;
	}

}