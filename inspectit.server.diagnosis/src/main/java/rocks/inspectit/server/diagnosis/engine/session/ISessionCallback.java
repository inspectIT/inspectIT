package rocks.inspectit.server.diagnosis.engine.session;

/**
 * A callback for handling the results or errors of a {@link Session} execution.
 *
 * @param <R>
 *            The type of result to be handled
 * @author Claudio Waldvogel
 */
public interface ISessionCallback<R> {

	/**
	 * Invoked with the result of a {@link Session} execution when it executes successful. The type
	 * of the result depends in the used {@link ISessionResultCollector}.
	 *
	 * @param result
	 *            The result of a {@link Session} execution
	 * @see ISessionResultCollector
	 */
	void onSuccess(R result);

	/**
	 * Invoked when the {@link Session} execution failed.
	 *
	 * @param throwable
	 *            The cause why the {@link Session} failed
	 */
	void onFailure(Throwable throwable);
}
