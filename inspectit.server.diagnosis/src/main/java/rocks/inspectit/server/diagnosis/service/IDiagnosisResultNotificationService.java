package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;

import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * Callback for new diagnosis results. TODO: When Java 1.8. is available replace this interface with
 * a Consumer.
 *
 * @author Alexander Wert
 *
 */
public interface IDiagnosisResultNotificationService {

	/**
	 * When a new Diagnosis Results is found this callback will be called.
	 *
	 * @param problemOccurrence
	 *            new problemOccurrence
	 */
	void onNewDiagnosisResult(ProblemOccurrence problemOccurrence);

	/**
	 * When a new Collection of Diagnosis Results is found this callback will be called.
	 *
	 * @param problemOccurrences
	 *            new problemOccurrence
	 */
	void onNewDiagnosisResult(Collection<ProblemOccurrence> problemOccurrences);

}