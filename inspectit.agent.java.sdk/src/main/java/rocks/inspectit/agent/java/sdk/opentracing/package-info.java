/**
 * Contains inspectIT implementation of the opentracing.io standard.
 * <p>
 * Tracer can be obtained by calling one of the <code>get</code> methods in the
 * {@link TracerProvider}. If the inspectIT agent is running with the application where
 * {@link TracerProvider} is used, the <code>get</code> methods will always returned correctly
 * initialized {@link ExtendedTracer}. If the inspectIT is not running, then the caller can control
 * if the returned tracer is noop tracer or <code>null</code>.
 * <p>
 * It's up to user to decide to code against the io.opentracing interfaces or use directly
 * {@link ExtendedTracer} which provides some additional control.
 * <p>
 * Typical usage:
 * <p>
 * <code>
 * Tracer tracer = TracerProvider.get();<br>
 * Span span = tracer.buildSpan().withTag("key", "value").start();<br>
 * ...<br>
 * span.finish();
 * </code>
 *
 *
 * @author Ivan Senic
 *
 */
package rocks.inspectit.agent.java.sdk.opentracing;