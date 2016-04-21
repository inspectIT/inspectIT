package rocks.inspectit.server.diagnosis.service;

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
	 * the minimum duration of a InvocationSequenceData. When the duration of the
	 * InvocationSequenceData is below the baseline then is will not be analyzed.
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
	 * Stops the diagnosis service and performs housekeeping.
	 *
	 * @param awaitShutdown
	 *            The flat to indicate if method will block until shutdown is complete.
	 */
	void shutdown(boolean awaitShutdown);
}
