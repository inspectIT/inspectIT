package rocks.inspectit.server.diagnosis.service.data;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Class representing a cluster of invocations during diagnosis.
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
	 */
	public CauseCluster(List<CauseCluster> clustersToMerge) {
		int distanceToParent = clustersToMerge.get(0).getDistanceToNextCluster();
		InvocationSequenceData parent = clustersToMerge.get(0).getCommonContext();
		for (int i = 0; i < distanceToParent; i++) {
			parent = parent.getParentSequence();
		}
		commonContext = parent;

		for (CauseCluster cluster : clustersToMerge) {
			causeInvocations.addAll(cluster.getCauseInvocations());
		}
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