package rocks.inspectit.agent.java.sdk.opentracing.noop;

import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;

/**
 * Noop {@link Reporter} that does nothing when spans are reported.
 *
 * @author Ivan Senic
 *
 */
public class NoopReporter implements Reporter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(SpanImpl span) {
		// does nothing
	}

}
