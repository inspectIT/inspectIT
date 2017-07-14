package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.LinkedList;
import java.util.List;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.server.diagnosis.service.data.DiagnosisHelper;
import rocks.inspectit.server.diagnosis.service.rules.InvocationSequenceDataIterator;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;


/**
 * Rule for detecting the <code>Problem Context</code> within an {@link InvocationSequenceData}. The
 * <code>Problem Context</code> is located between the <code>Global Context</code> and a
 * <code>Time Wasting Operation</code>. The <code>Problem Context</code> is the deepest node in the
 * invocation tree that subsumes one performance problem. This rule is triggered third in the rule
 * pipeline.
 *
 * @author Alexander Wert, Alper Hidiroglu, Christian Voegele
 *
 */
@Rule(name = "ProblemContextRule")
public class ProblemContextRule {

	/**
	 * Exclusive time of cluster has to be higher than 80 percent of
	 * <code>Time Wasting Operation's</code> exclusive time in order to be a significant cluster.
	 */
	private static final double PROPORTION = 0.8;

	/**
	 * In case the clustering does not find a significant cluster the clustering will be stopped.
	 */
	private static boolean stopClustering = false;

	/**
	 * Injection of the <code>Global Context</code>.
	 */
	@TagValue(type = RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT)
	private InvocationSequenceData globalContext;

	/**
	 * Each <code>Time Wasting Operation</code> has exactly one corresponding <code>Problem
	 * Context</code>.
	 */
	@TagValue(type = RuleConstants.DIAGNOSIS_TAG_TIME_WASTING_OPERATIONS)
	private AggregatedDiagnosisData timeWastingOperation;

	/**
	 * Rule execution.
	 *
	 * The exclusive times of all InvocationSequenceData in the Time Wasting Operation are summed
	 * up. The InvocationSequenceData are clustered until there is a cluster with a significant high
	 * exclusive time.
	 *
	 * @return DIAGNOSIS_TAG_PROBLEM_CONTEXT
	 */
	@Action(resultTag = RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT)
	public CauseCluster action() {
		List<InvocationSequenceData> causeInvocations = timeWastingOperation.getRawInvocationsSequenceElements();

		if (causeInvocations.size() > 1) {
			return getSignificantCluster(populateCauseCluster());
		} else if (causeInvocations.size() == 1) {
			return getCauseCluster(causeInvocations.get(0));
		} else {
			throw new RuntimeException("TimeWastingOperation has no elements");
		}
	}

	/**
	 * In case there is just one cause invocation then the parent of the problem context is the
	 * cause invocation, otherwise a new instance of CauseCluster will be created with the only
	 * invocation in the list.
	 *
	 * @param causeInvocation
	 *            Invocation to get the parent of it if is the only one itself it has no parent.
	 * @return Returns the CauseCluster.
	 */
	private CauseCluster getCauseCluster(InvocationSequenceData causeInvocation) {
		if ((causeInvocation.getParentSequence() != null) && !(causeInvocation.equals(globalContext))) {
			CauseCluster causeCluster = new CauseCluster(causeInvocation.getParentSequence());
			causeCluster.getCauseInvocations().clear();
			causeCluster.getCauseInvocations().add(causeInvocation);
			return causeCluster;
		} else {
			return new CauseCluster(causeInvocation);
		}
	}

	/**
	 * Creates list with clusters. For each InvocationSequenceData in the Time Wasting Operation a
	 * cluster is build initially.
	 *
	 * @return Returns a list of causeCluster.
	 */
	private List<CauseCluster> populateCauseCluster() {
		List<InvocationSequenceData> causeInvocations = timeWastingOperation.getRawInvocationsSequenceElements();
		List<CauseCluster> causeClusters = new LinkedList<>();

		for (InvocationSequenceData invocation : causeInvocations) {
			causeClusters.add(new CauseCluster(invocation));
		}
		return causeClusters;
	}

	/**
	 * Gets the most significant cluster in the list.
	 *
	 * @param causeClusters
	 *            List of clusters to get the most significant.
	 * @return Returns the most significant cluster in the list.
	 */
	private CauseCluster getSignificantCluster(List<CauseCluster> causeClusters) {
		// Checks if there is already a cluster with higher duration ratio
		// from overallExclusiveDuration.
		CauseCluster significantCluster = getHighDurationCluster(causeClusters, timeWastingOperation.getAggregatedDiagnosisTimerData().getExclusiveDuration());

		// Iterates as long as there is no significantCluster.
		while ((null == significantCluster) && !stopClustering) {
			calculateDistancesToNextCluster(causeClusters);
			causeClusters = mergeClusters(causeClusters);
			significantCluster = getHighDurationCluster(causeClusters, timeWastingOperation.getAggregatedDiagnosisTimerData().getExclusiveDuration());
		}

		// This rule does not return the Problem Context directly, but the significant cluster.
		// The Problem Context is the deepest node in the invocation tree that subsumes all
		// InvocationSequenceData the significant cluster holds and can be accessed via
		// cluster.getCommonContext().
		return significantCluster;
	}

	/**
	 * Identifies after each merge if there is a {@link #CauseCluster} with a significant high
	 * exclusive time. If so, the {@link #CauseCluster} is returned. Otherwise returns
	 * <code>null</code>.
	 *
	 * @param causeClusters
	 *            List with clusters.
	 * @param overallExclusiveDuration
	 *            The summed up exclusive time of all {@link InvocationSequenceData} the
	 *            <code>Time Wasting Operation</code> holds.
	 * @return Significant cluster.
	 */
	private CauseCluster getHighDurationCluster(List<CauseCluster> causeClusters, double overallExclusiveDuration) {
		CauseCluster maxExclusiveDurationCluster = causeClusters.get(0);
		double maxExclusiveDurationSum = 0;
		for (CauseCluster cluster : causeClusters) {
			double exclusiveDurationSum = 0.0;
			for (InvocationSequenceData invocation : cluster.getCauseInvocations()) {
				exclusiveDurationSum += DiagnosisHelper.getExclusiveDuration(invocation);
			}

			if (exclusiveDurationSum > maxExclusiveDurationSum) {
				maxExclusiveDurationSum = exclusiveDurationSum;
				maxExclusiveDurationCluster = cluster;
			}

			if (exclusiveDurationSum > (PROPORTION * overallExclusiveDuration)) {
				return cluster;
			}
		}

		// in case no significant cluster can be found the cluster with the highest duration will be
		// returned.
		if (stopClustering) {
			return maxExclusiveDurationCluster;
		} else {
			return null;
		}
	}


	/**
	 * Merges {@link #CauseCluster}.
	 *
	 * @param causeClusters
	 *            List with clusters.
	 * @return List with merged clusters.
	 */
	private List<CauseCluster> mergeClusters(List<CauseCluster> causeClusters) {
		if (causeClusters.size() == 1) {
			throw new RuntimeException("Only one cluster to merge. That is not allowed.");
		}

		boolean merged = false;
		int distance = 1;
		List<CauseCluster> newClusters = new LinkedList<>();
		List<CauseCluster> clustersToMerge = new LinkedList<>();
		while (!merged && !stopClustering) {
			clustersToMerge.clear();
			newClusters.clear();

			// determines the cluster with the same distanceToNextCluster
			for (CauseCluster cluster : causeClusters) {
				if (cluster.getDistanceToNextCluster() == distance) {
					clustersToMerge.add(cluster);
				} else {
					newClusters.add(cluster);
				}
			}

			// merges these cluster in case at least two are found. Then the clustering will
			// terminate.
			if (clustersToMerge.size() > 1) {
				newClusters.add(new CauseCluster(clustersToMerge, globalContext));
				merged = true;
			} else if (clustersToMerge.size() == 1) {
				newClusters.add(clustersToMerge.get(0));
			}

			// in case no merge was done the distance increases.
			distance++;

			// in case no clusters could be merged the clustering will run into endless loop. This
			// will be avoided here.
			if (distance == Short.MAX_VALUE) {
				stopClustering = true;
			}
		}
		return newClusters;
	}

	/**
	 * Calculates for each {@link #CauseCluster} the distance to the next cluster. With the
	 * calculated distances it is decided which clusters will be merged.
	 *
	 * @param causeClusters
	 *            List with clusters from which the distances are calculated.
	 */
	private void calculateDistancesToNextCluster(List<CauseCluster> causeClusters) {
		int nextClusterIndex = 0;
		CauseCluster nextCluster = causeClusters.get(nextClusterIndex);
		CauseCluster currentCluster = null;
		// Starts from Global Context
		InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(globalContext, true);

		int currentCauseDepth = -1;
		while (iterator.hasNext() && (nextClusterIndex < causeClusters.size())) {
			if (nextCluster.getCommonContext() == iterator.next()) {
				if (null != currentCluster) {
					int depthDistance = Math.abs(currentCauseDepth - iterator.currentDepth()) + 1;
					currentCluster.setDistanceToNextCluster(depthDistance);
					currentCluster.setDepthOfCommonContext(currentCauseDepth);
					nextCluster.setDistanceToNextCluster(depthDistance);
					nextCluster.setDepthOfCommonContext(iterator.currentDepth());
				}

				currentCluster = nextCluster;
				nextClusterIndex++;
				if (nextClusterIndex < causeClusters.size()) {
					nextCluster = causeClusters.get(nextClusterIndex);
				}
				currentCauseDepth = iterator.currentDepth();
			}
		}
	}
}
