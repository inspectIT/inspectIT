package rocks.inspectit.server.diagnosis.service.data;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Class representing a cluster.
 *
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
public class CauseCluster {

	/**
	 * Holds the {@link InvocationSequenceData} of the cluster.
	 */
	private final List<InvocationSequenceData> causeInvocations = new ArrayList<InvocationSequenceData>();

	/**
	 * Saves the distance to the next cluster.
	 */
	private int distanceToNextCluster = Integer.MAX_VALUE;

	/**
	 * The depth of commonContext within the invocationSequence.
	 */
	private int depthOfCommonContext = -1;

	/**
	 * The deepest node in the invocation tree that subsumes all {@link InvocationSequenceData} the
	 * cluster holds.
	 */
	private final InvocationSequenceData commonContext;

	/**
	 * Adds an {@link InvocationSequenceData} to the list of elements the cluster holds. Is used
	 * only for initial cluster. {@link commonContext} is initially the element with which the
	 * cluster is created.
	 *
	 * @param causeInvocation
	 *            The {@link InvocationSequenceData} this cluster is initially build with.
	 */
	public CauseCluster(InvocationSequenceData causeInvocation) {
		if (causeInvocation == null) {
			throw new IllegalArgumentException("Invocations are null");
		}

		causeInvocations.add(causeInvocation);
		commonContext = causeInvocation;
	}

	/**
	 * Creates a new cluster from the passed list of {@link #CauseCluster}. Sets the
	 * <code>commonContext</code> to the parent of the new cluster. The parent subsumes all
	 * {@link InvocationSequenceData} the cluster currently holds.
	 *
	 * @param clustersToMerge
	 *            List with clusters this cluster is merged with.
	 * @param globalContext
	 *            The globalContext to check that the new commonContext is not higher than the
	 *            globalContext.
	 */
	public CauseCluster(List<CauseCluster> clustersToMerge, InvocationSequenceData globalContext) {
		if ((clustersToMerge == null) || (clustersToMerge.isEmpty())) {
			throw new IllegalArgumentException("Clusters are not allowed to be null or empty!");
		}

		// determines the cluster with the lowest depths in InvocationSequence. Takes the parent of
		// this sequence as common context.
		int minCommonContextDepth = Integer.MAX_VALUE;
		InvocationSequenceData minCommonContext = clustersToMerge.get(0).getCommonContext();
		for (CauseCluster causeCluster : clustersToMerge) {
			if (causeCluster.getDepthOfCommonContext() < minCommonContextDepth) {
				minCommonContextDepth = causeCluster.getDepthOfCommonContext();
				minCommonContext = causeCluster.getCommonContext();
			}
		}

		// check that the commonContext is not above the commonContext
		if (minCommonContext == globalContext) { // NOPMD no equals on purpose
			commonContext = minCommonContext;
		} else {
			commonContext = minCommonContext.getParentSequence();
		}

		for (CauseCluster cluster : clustersToMerge) {
			causeInvocations.addAll(cluster.getCauseInvocations());
		}
	}

	/**
	 * Gets {@link #depthOfCommonContext}.
	 *
	 * @return {@link #depthOfCommonContext}
	 */
	public final int getDepthOfCommonContext() {
		return this.depthOfCommonContext;
	}

	/**
	 * Sets {@link #depthOfCommonContext}.
	 *
	 * @param depthOfCommonContext
	 *            New value for {@link #depthOfCommonContext}
	 */
	public final void setDepthOfCommonContext(int depthOfCommonContext) {
		this.depthOfCommonContext = depthOfCommonContext;
	}

	/**
	 * @return {@link #causeInvocations} the cluster holds.
	 */
	public List<InvocationSequenceData> getCauseInvocations() {
		return causeInvocations;
	}

	/**
	 * @return {@link #distanceToNextCluster}.
	 */
	public int getDistanceToNextCluster() {
		return distanceToNextCluster;
	}

	/**
	 * Sets {@link #distanceToNextCluster}.
	 *
	 * @param distanceToNextCluster
	 *            New value for {@link #distanceToNextCluster}
	 */
	public void setDistanceToNextCluster(int distanceToNextCluster) {
		this.distanceToNextCluster = distanceToNextCluster;
	}

	/**
	 * Gets {@link #commonContext}.
	 *
	 * @return {@link #commonContext}
	 */
	public InvocationSequenceData getCommonContext() {
		return commonContext;
	}

}