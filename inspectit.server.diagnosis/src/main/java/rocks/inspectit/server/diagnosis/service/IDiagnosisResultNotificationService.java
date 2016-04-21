/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;

import rocks.inspectit.shared.all.communication.data.diagnosis.results.ProblemOccurrence;


/**
 * Callback for new diagnosis results.
 *
 * @author Alexander Wert
 *
 */
public interface IDiagnosisResultNotificationService {

	/**
	 * @param problemOccurrence
	 *            new problemOccurrence
	 */
	void onNewDiagnosisResult(ProblemOccurrence problemOccurrence);

	/**
	 * @param problemOccurrences
	 *            new problemOccurrence
	 */
	void onNewDiagnosisResult(Collection<ProblemOccurrence> problemOccurrences);

}
