package rocks.inspectit.server.diagnosis.service.aggregation;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;

/**
 * Aggregation for {@link InvocationSequenceData}.
 *
 * Note: When {@link InvocationSequenceData} elements are aggregated, then the invocation structure
 * is ignored for aggregation.
 *
 * @author Alexander Wert, Christian Voegele, Ivan Senic
 *
 */
public final class DiagnosisDataAggregator {

	/**
	 * Static instance of the class.
	 */
	private static final DiagnosisDataAggregator INSTANCE = new DiagnosisDataAggregator();

	/**
	 * Constructor of the class.
	 */
	private DiagnosisDataAggregator() {
	}

	/**
	 *
	 * @return Returns an instance of this class
	 */
	public static DiagnosisDataAggregator getInstance() {
		return INSTANCE;
	}

	/**
	 * Aggregate the InvocationSequenceData to the AggregatedDiagnosisData.
	 *
	 * @param aggregatedObject
	 *            aggregatedObject
	 * @param objectToAdd
	 *            object to add to aggregation
	 */
	public void aggregate(AggregatedDiagnosisData aggregatedObject, InvocationSequenceData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * Convert a {@link InvocationSequenceData} to a {@link AggregatedDiagnosisData}.
	 *
	 * @param invocationSequenceData
	 *            input invocationSequenceData
	 * @return AggregatedDiagnosisData based on invocationSequenceData
	 */
	public AggregatedDiagnosisData getAggregatedDiagnosisData(InvocationSequenceData invocationSequenceData) {
		if (InvocationSequenceDataHelper.hasHttpTimerData(invocationSequenceData)) {
			return new AggregatedDiagnosisData(SourceType.HTTP, invocationSequenceData, getAggregationKey(invocationSequenceData));
		} else if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
			return new AggregatedDiagnosisData(SourceType.DATABASE, invocationSequenceData, getAggregationKey(invocationSequenceData));
		} else if (InvocationSequenceDataHelper.hasTimerData(invocationSequenceData)) {
			return new AggregatedDiagnosisData(SourceType.TIMERDATA, invocationSequenceData, getAggregationKey(invocationSequenceData));
		} else {
			throw new IllegalArgumentException("No timer data available!");
		}
	}

	/**
	 * Get key for aggregation.
	 *
	 * @param invocationSequenceData
	 *            invocationSequenceData to key should be determined
	 * @return key as object
	 */
	public Object getAggregationKey(InvocationSequenceData invocationSequenceData) {
		if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
			return new Pair<Long, String>(invocationSequenceData.getMethodIdent(), invocationSequenceData.getSqlStatementData().getSql());
		} else if (InvocationSequenceDataHelper.hasHttpTimerData(invocationSequenceData)) {
			return new Pair<Long, String>(invocationSequenceData.getMethodIdent(), ((HttpTimerData) invocationSequenceData.getTimerData()).getHttpInfo().getUri());
		} else {
			return invocationSequenceData.getMethodIdent();
		}
	}

}