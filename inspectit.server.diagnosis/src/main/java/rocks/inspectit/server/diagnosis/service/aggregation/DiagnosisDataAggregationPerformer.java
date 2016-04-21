package rocks.inspectit.server.diagnosis.service.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This class defines the process for aggregation of DiagnosisData. As input a List of
 * {@link InvocationSequenceData} is taken and aggregated using the {@link DiagnosisDataAggregator}
 * to a List of {@link AggregatedDiagnosisData}.
 *
 * @author Christian Voegele
 *
 */
public class DiagnosisDataAggregationPerformer {

	/**
	 * Map for caching of AggregatedDiagnosisData.
	 */
	private Map<Object, AggregatedDiagnosisData> diagnosisDataAggregationMap;

	/**
	 * {@link DiagnosisDataAggregator} used.
	 */
	private DiagnosisDataAggregator diagnosisDataAggregator;

	/**
	 * Default constructor.
	 *
	 */
	public DiagnosisDataAggregationPerformer() {
		this.diagnosisDataAggregator = new DiagnosisDataAggregator();
		this.diagnosisDataAggregationMap = new HashMap<>();
	}

	/**
	 * Add one InvocationSequenceData to the aggregation.
	 *
	 * @param invocationSequenceData
	 *            invocationSequenceData to be aggregated
	 */
	public void aggregateInvocationSequenceData(InvocationSequenceData invocationSequenceData) {
		Object key = diagnosisDataAggregator.getAggregationKey(invocationSequenceData);
		AggregatedDiagnosisData aggregatedObject = diagnosisDataAggregationMap.get(key);
		if (null != aggregatedObject) {
			diagnosisDataAggregator.aggregate(aggregatedObject, invocationSequenceData);
		} else {
			aggregatedObject = diagnosisDataAggregator.getAggregatedDiagnosisData(invocationSequenceData);
			diagnosisDataAggregationMap.put(key, aggregatedObject);
		}
	}

	/**
	 * Aggregate the list of InvocationSequenceData.
	 *
	 * @param invocationSequenceDataList
	 *            list of invocationSequenceDataList to be aggregated
	 */
	public void aggregateInvocationSequenceDataList(List<InvocationSequenceData> invocationSequenceDataList) {
		for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
			aggregateInvocationSequenceData(invocationSequenceData);
		}
	}

	/**
	 * Returns aggregation results.
	 *
	 * @return Returns aggregation results.
	 */
	public List<AggregatedDiagnosisData> getAggregationResultList() {
		List<AggregatedDiagnosisData> returnList = new ArrayList<>();
		for (AggregatedDiagnosisData aggregatedData : diagnosisDataAggregationMap.values()) {
			returnList.add(aggregatedData);
		}
		return returnList;
	}

}
