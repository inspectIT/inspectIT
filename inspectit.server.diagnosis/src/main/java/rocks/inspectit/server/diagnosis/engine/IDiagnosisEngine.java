package rocks.inspectit.server.diagnosis.engine;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

/**
 * This is the core interface of the diagnosis engine.
 *
 * @param <I>
 *            The input type to be analyzed.
 * @author Claudio Waldvogel
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
	void analyze(I input, SessionVariables variables) throws DiagnosisEngineException;

	/**
	 * Stops the engine and performs housekeeping.
	 *
	 * @param awaitShutdown
	 *            The flat to indicate if method will block until shutdown is complete.
	 * @throws Exception
	 *             in case of any error.
	 */
	void shutdown(boolean awaitShutdown) throws Exception;

}