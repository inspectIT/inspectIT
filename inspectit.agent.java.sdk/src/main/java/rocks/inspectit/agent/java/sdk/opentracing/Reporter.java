package rocks.inspectit.agent.java.sdk.opentracing;

import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;

/**
 * Reporter servers as class handling finished spans. This SDK only provides the
 * {@link rocks.inspectit.agent.java.sdk.opentracing.noop.NoopReporter} as the implementation.
 * However, inspectIT agent will provide a reporter that is able to send spans to the inspectIT CMR.
 *
 * @author Ivan Senic
 *
 */
public interface Reporter {

	/**
	 * Reports span once it's finished. Non finished spans are not reported.
	 *
	 * @param span
	 *            Span to report.
	 */
	void report(SpanImpl span);
}
