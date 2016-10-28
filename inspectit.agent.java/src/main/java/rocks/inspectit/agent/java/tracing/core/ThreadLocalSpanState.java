package rocks.inspectit.agent.java.tracing.core;

import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.Span;

/**
 * Helper class that resolving the spans we have per each thread.
 *
 * @author Ivan Senic
 *
 */
public class ThreadLocalSpanState {

	/**
	 * Holds information about the current span per thread.
	 */
	private final ThreadLocal<ServerSpan> currentSpan = new ThreadLocal<ServerSpan>();

	/**
	 * Holds information about the current client span. Client spans are calls made to the other
	 * spans.
	 */
	private final ThreadLocal<ClientSpan> currentClientSpan = new ThreadLocal<ClientSpan>();

	/**
	 * Get's the current span.
	 *
	 * @return Get's the current span.
	 * @see #currentSpan
	 */
	public ServerSpan getCurrentSpan() {
		return currentSpan.get();
	}

	/**
	 * Sets the current span.
	 *
	 * @param span
	 *            {@link Span}
	 * @see #currentSpan
	 */
	public void setCurrentSpan(ServerSpan span) {
		currentSpan.set(span);
	}

	/**
	 * Gets the current client span.
	 *
	 * @return Gets the current client span.
	 * @see #currentClientSpan
	 */
	public ClientSpan getCurrentClientSpan() {
		return currentClientSpan.get();
	}

	/**
	 * Sets the current client span.
	 *
	 * @param span
	 *            {@link ReferenceSpan}
	 * @see #currentClientSpan
	 */
	public void setCurrentClientSpan(ClientSpan span) {
		currentClientSpan.set(span);
	}

}
