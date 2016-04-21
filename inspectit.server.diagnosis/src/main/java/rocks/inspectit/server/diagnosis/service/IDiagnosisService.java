package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;

import org.apache.commons.math3.util.Pair;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This is the core interface of the diagnosis service.
 *
 * @author Alexander Wert
 *
 */
public interface IDiagnosisService {

	/**
	 * Start to diagnose the current InvocationSequenceData using a baseline. The baseline defines
	 * the minimum duration of a InvocationSequenceData that it is going to processed.
	 *
	 * @param invocation
	 *            InvocationSequenceData to diagnose
	 * @param baseline
	 *            baseline defines the minimum duration of InvocationSequenceData
	 * @return true if successful, or false if the specified waiting time elapses before space is
	 *         available
	 */
	boolean diagnose(InvocationSequenceData invocation, double baseline);

	/**
	 * Start to diagnose the current InvocationSequenceData using a baseline. The baseline defines
	 * the minimum duration of a InvocationSequenceData that it is going to processed.
	 *
	 * @param invocationBaselinePairs
	 *            A Pair of InvocationSequenceData and its minimum duration of
	 *            InvocationSequenceData (Baseline)
	 * @return number of diagnosed invocationBaselinePairs
	 */
	int diagnose(Collection<Pair<InvocationSequenceData, Double>> invocationBaselinePairs);

	/**
	 * Stops the diagnosis service and performs housekeeping.
	 *
	 * @param awaitShutdown
	 *            The flat to indicate if method will block until shutdown is complete.
	 */
	void shutdown(boolean awaitShutdown);
}
