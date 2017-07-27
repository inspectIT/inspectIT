package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;

/**
 * Implementation of the opentracing.io {@link Span}. This implementation is not thread safe, as
 * anyway one span should be bounded to one thread.
 * <p>
 * <b>Limitations:</b> This span implementation is ignoring the calls to the <code>log</code>
 * methods as we currently don't support displaying of log events in inspectIT.
 *
 * @author Ivan Senic
 *
 */
public class SpanImpl implements Span {

	/**
	 * Tracer.
	 */
	private final TracerImpl tracer;

	/**
	 * Operation name as per io.tracing specification.
	 */
	private String operationName;

	/**
	 * {@link SpanContext} of this span.
	 */
	private SpanContextImpl spanContext;

	/**
	 * Start time of the span. Microseconds since epoch.
	 */
	private long startTimeMicros;

	/**
	 * Start time nanos of the span. Can be used for more precise duration calculation.
	 */
	private long startTimeNanos;

	/**
	 * Duration of the span in microseconds. We keep the span duration as double to that we can keep
	 * the nano fraction when possible.
	 */
	private double duration;

	/**
	 * If the span should be reported to the {@link Reporter}.
	 */
	private boolean report = true;

	/**
	 * Tags of this span. We save all tag values as string.
	 */
	private Map<String, String> tags;

	/**
	 * Indicates whether the span has been finished.
	 */
	private boolean finished = false;

	/**
	 * Default constructor. Sets only the tracer.
	 *
	 * @param tracer
	 *            Tracer.
	 */
	public SpanImpl(TracerImpl tracer) {
		this.tracer = tracer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextImpl context() {
		return spanContext;
	}

	/**
	 * Starts the span and sets the start time to the current time provided by the set tracer.
	 */
	public void start() {
		start(tracer.getTimer().getCurrentTimeMicroseconds(), tracer.getTimer().getCurrentNanoTime());
	}

	/**
	 * Internal start method with start time in microseconds and nano ticks.
	 *
	 * @param startTimeMicros
	 *            Start time in microseconds. Must not be 0 or negative.
	 * @param startTimeNanos
	 *            Current nano ticks. In case this value is not zero duration will be calculated
	 *            using nano time.
	 */
	void start(long startTimeMicros, long startTimeNanos) {
		if (isStarted()) {
			return;
		}

		if (startTimeMicros <= 0) {
			throw new IllegalArgumentException("Start time in microseconds must be provided.");
		}

		this.startTimeMicros = startTimeMicros;
		this.startTimeNanos = startTimeNanos;
		tracer.spanStarted(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() {
		if (startTimeNanos != 0) {
			finishWithNanos(tracer.getTimer().getCurrentNanoTime());
		} else {
			finish(tracer.getTimer().getCurrentTimeMicroseconds());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish(long finishMicros) {
		// if already finish nothing to do
		if (isFinished()) {
			return;
		}

		// if not started we can not calculate duration
		if (isStarted()) {
			duration = finishMicros - startTimeMicros;
		}

		// inform tracer
		finished = true;
		tracer.spanEnded(this);
	}

	/**
	 * Finishes the span with the nano duration, uses {@link #startTimeNanos} to calculate final
	 * duration.
	 *
	 * @param nanos
	 *            Current nano time..
	 */
	private void finishWithNanos(long nanos) {
		// if already finish nothing to do
		if (isFinished()) {
			return;
		}

		// we must be started if finishing with nanos
		duration = (nanos - startTimeNanos) / 1000.d;

		// inform tracer
		finished = true;
		tracer.spanEnded(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates call to {@link #finish()}.
	 */
	@Override
	public void close() {
		finish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span setTag(String key, String value) {
		return setTagInternal(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span setTag(String key, boolean value) {
		return setTagInternal(key, String.valueOf(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span setTag(String key, Number value) {
		return setTagInternal(key, value.toString());
	}

	/**
	 * Internal method for span adding.
	 *
	 * @param key
	 *            Span key
	 * @param value
	 *            Span value
	 * @return This object
	 */
	private Span setTagInternal(String key, String value) {
		if (null == tags) {
			tags = new HashMap<String, String>(1, 1f);
		}
		tags.put(key, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 */
	@Override
	public Span log(Map<String, ?> fields) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 */
	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 */
	@Override
	public Span log(String event) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 */
	@Override
	public Span log(long timestampMicroseconds, String event) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 *
	 * @deprecated use {@link #log(Map)}
	 */
	@Deprecated
	@Override
	public Span log(String eventName, Object payload) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented by the inspectIT SDK.
	 *
	 * @deprecated use {@link #log(Map)}
	 */
	@Deprecated
	@Override
	public Span log(long timestampMicroseconds, String eventName, Object payload) {
		// ignored
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span setBaggageItem(String key, String value) {
		if (null != spanContext) {
			spanContext.setBaggageItem(key, value);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaggageItem(String key) {
		if (null != spanContext) {
			return spanContext.getBaggageItem(key);
		}
		return null;
	}

	/**
	 * If span is of a type client. Only returns <code>true</code> if span is explicitly declared as
	 * client.
	 *
	 * @return If span is of a type client.
	 * @see Tags#SPAN_KIND
	 */
	public boolean isClient() {
		if (tags != null) {
			String kind = tags.get(Tags.SPAN_KIND.getKey());
			return Tags.SPAN_KIND_CLIENT.equals(kind);
		}
		return false;
	}

	/**
	 * If span is of a type server. Only returns <code>false</code> if span is explicitly declared
	 * as client.
	 *
	 * @return If span is of a type server.
	 * @see Tags#SPAN_KIND
	 */
	public boolean isServer() {
		return !isClient();
	}

	/**
	 * Returns whether the span has already been started.
	 *
	 * @return Returns <code>true</code> if the span has been started otherwise <code>false</code>.
	 */
	public boolean isStarted() {
		return startTimeMicros > 0L;
	}

	/**
	 * Returns whether the span has been finished.
	 *
	 * @return Returns <code>true</code> if the span has been finished.
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Gets {@link #operationName}.
	 *
	 * @return {@link #operationName}
	 */
	public String getOperationName() {
		return this.operationName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span setOperationName(String operationName) {
		this.operationName = operationName;
		return this;
	}

	/**
	 * Gets {@link #startTimeMicros}.
	 *
	 * @return {@link #startTimeMicros}
	 */
	public long getStartTimeMicros() {
		return this.startTimeMicros;
	}

	/**
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	public double getDuration() {
		return this.duration;
	}

	/**
	 * Gets {@link #tags}.
	 *
	 * @return {@link #tags}
	 */
	public Map<String, String> getTags() {
		return this.tags;
	}

	/**
	 * Sets {@link #spanContext}.
	 *
	 * @param spanContext
	 *            New value for {@link #spanContext}
	 */
	void setSpanContext(SpanContextImpl spanContext) {
		this.spanContext = spanContext;
	}

	/**
	 * Gets {@link #report}.
	 *
	 * @return {@link #report}
	 */
	public boolean isReport() {
		return this.report;
	}

	/**
	 * Sets {@link #report}.
	 *
	 * @param report
	 *            New value for {@link #report}
	 */
	void setReport(boolean report) {
		this.report = report;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.spanContext == null) ? 0 : this.spanContext.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpanImpl other = (SpanImpl) obj;
		if (this.spanContext == null) {
			if (other.spanContext != null) {
				return false;
			}
		} else if (!this.spanContext.equals(other.spanContext)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "SpanImpl [spanContext=" + this.spanContext + ", operationName=" + this.operationName + ", duration=" + this.duration + ", report=" + this.report + ", tags=" + this.tags + "]";
	}

}
