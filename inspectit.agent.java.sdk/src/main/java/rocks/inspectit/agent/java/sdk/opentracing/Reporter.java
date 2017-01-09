package rocks.inspectit.agent.java.sdk.opentracing;

import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;

/**
 * Reporter serves as class handling finished spans.
 * <p>
 * This SDK only provides the {@link rocks.inspectit.agent.java.sdk.opentracing.noop.NoopReporter}
 * as the implementation. However, if the inspectit agent is active on the target application, the
 * "real" reporter will be used which sends spans to the inspectIT CMR. In this case the
 * initialization of the reported and the tracer is done by inspectIT and can be obtained in
 * {@link rocks.inspectit.agent.java.sdk.opentracing.impl.TracerProvider}.
 *
 * @author Ivan Senic
 *
 */
public interface Reporter {

	/**
	 * Reports span once it's finished. Spans that are started, but not finished are not reported.
	 *
	 * @param span
	 *            Span to report.
	 */
	void report(SpanImpl span);
}
