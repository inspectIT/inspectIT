package rocks.inspectit.agent.java.tracing.core.async.executor;

import com.google.common.base.Preconditions;

import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * This class is used to substitute {@link Runnable} object when thread correlation of executors are
 * used. Calls of {@link #run()} will be delegated to the original runnable and, in addition, a new
 * Span is started if provided.
 *
 * @author Marius Oehler
 *
 */
public class SpanStoreRunnable extends SpanStore implements Runnable {

	/**
	 * The original runnable object.
	 */
	private Runnable runnable;

	/**
	 * Constructor.
	 *
	 * @param runnable
	 *            original runnable which will be wrapped
	 */
	public SpanStoreRunnable(Runnable runnable) {
		Preconditions.checkNotNull(runnable);

		this.runnable = runnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		startSpan();

		runnable.run();

		finishSpan();
	}
}
