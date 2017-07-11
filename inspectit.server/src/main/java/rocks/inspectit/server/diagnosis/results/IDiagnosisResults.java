package rocks.inspectit.server.diagnosis.results;

import java.util.Collection;

/**
 * Interface of DiagnosisResults storage.
 *
 * @author Christian Voegele
 *
 * @param <R>
 */
public interface IDiagnosisResults<R> {

	/**
	 * @return Collection of diagnosis results.
	 */
	Collection<R> getDiagnosisResults();
}
