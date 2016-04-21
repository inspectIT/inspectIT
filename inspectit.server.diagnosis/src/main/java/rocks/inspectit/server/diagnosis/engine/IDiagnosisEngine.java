package rocks.inspectit.server.diagnosis.engine;

import java.util.Map;

/**
 * This is the core interface of the diagnosis engine.
 *
 * @param <I>
 *            The input type to be analyzed.
 * @author Claudio Waldvogel, Alexander Wert
 */
public interface IDiagnosisEngine<I> {

	/**
	 * Starts analyzing the given input.
	 *
	 * @param input
	 *            Any kind of object to be analyzed.
	 * @throws DiagnosisEngineException
	 *             Throws this type of exception if an failure occurs during diagnosis.
	 */
	void analyze(I input) throws DiagnosisEngineException;

	/**
	 * Starts analyzing the given input with additional session specific variables.
	 *
	 * @param input
	 *            Any kind of object to be analyzed.
	 * @param variables
	 *            Variables to be available while processing the input.
	 * @throws DiagnosisEngineException
	 *             Throws this type of exception if an failure occurs during diagnosis.
	 */
	void analyze(I input, Map<String, ?> variables) throws DiagnosisEngineException;

	/**
	 * Stops the engine and performs housekeeping.
	 *
	 * @param awaitShutdown
	 *            The flat to indicate if method will block until shutdown is complete.
	 */
	void shutdown(boolean awaitShutdown);

}