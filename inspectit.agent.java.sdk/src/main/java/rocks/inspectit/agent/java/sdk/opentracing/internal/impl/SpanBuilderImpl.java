package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import rocks.inspectit.agent.java.sdk.opentracing.Timer;

/**
 * Builder for the span. Note that builder is delegating the calls to the span that's created
 * immediately on the builder initialization. This way we save the copying of the data to the span
 * later on.
 * <p>
 * <b>Limitations:</b> This span implementation is saving only one (first) referenced context as the
 * parent. For any additional referenced contexts only the baggage propagation will be done.
 *
 * @author Ivan Senic
 *
 */
public class SpanBuilderImpl implements SpanBuilder {

	/**
	 * Span being created.
	 */
	private final SpanImpl span;

	/**
	 * Timer to use if startTimestamp is not provided in the builder.
	 */
	private final Timer timer;

	/**
	 * Collected baggage from all parents.
	 */
	private final Map<String, String> baggage = new HashMap<String, String>();

	/**
	 * Manually specified timestamp.
	 */
	private long startTimestamp;

	/**
	 * Parent context. Can be <code>null</code> to denote the new span.
	 */
	private SpanContextImpl parent;

	/**
	 * Reference type to the {@link #parent} context.
	 */
	private String referenceType;

	/**
	 * Creates new span builder.
	 *
	 * @param tracer
	 *            {@link TracerImpl}
	 * @param operationName
	 *            Operation name.
	 */
	public SpanBuilderImpl(TracerImpl tracer, String operationName) {
		this.timer = tracer.getTimer();
		this.span = new SpanImpl(tracer);
		this.span.setOperationName(operationName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Entry<String, String>> baggageItems() {
		return baggage.entrySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl asChildOf(SpanContext parent) {
		if (null != parent) {
			addReference(References.CHILD_OF, parent);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl asChildOf(Span parent) {
		if (null != parent) {
			addReference(References.CHILD_OF, parent.context());
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the {@link #parent} is not set this method will set the passed context as the main
	 * referenced context. All the baggage from the context will be passed no matter if the
	 * {@link #parent} is already set or not.
	 */
	@Override
	public SpanBuilderImpl addReference(String referenceType, SpanContext referencedContext) {
		if (null != referencedContext) {
			if (References.CHILD_OF.equals(referenceType) || References.FOLLOWS_FROM.equals(referenceType)) {
				// we will set the main parent only if it's not set already
				if ((null == parent) && (referencedContext instanceof SpanContextImpl)) {
					this.parent = (SpanContextImpl) referencedContext;
					this.referenceType = referenceType;
				}

			}
			// for now we only directly reference one parent, but collect baggage from all
			// reference contexts
			withBaggageFrom(referencedContext);
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl withTag(String key, String value) {
		span.setTag(key, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl withTag(String key, boolean value) {
		span.setTag(key, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl withTag(String key, Number value) {
		span.setTag(key, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanBuilderImpl withStartTimestamp(long microseconds) {
		this.startTimestamp = microseconds;
		return this;
	}

	/**
	 * Explicitly states that the span created should not be reported.
	 *
	 * @return {@link SpanBuilderImpl} for connecting.
	 */
	public SpanBuilderImpl doNotReport() {
		this.span.setReport(false);
		return this;
	}

	/**
	 * Collects baggage from parent. Copied from the io.opentracing implementation.
	 *
	 * @param from
	 *            context to copy baggage from
	 */
	private void withBaggageFrom(SpanContext from) {
		Iterable<Entry<String, String>> baggageItems = from.baggageItems();
		if ((null == baggageItems) || (null == baggageItems.iterator())) {
			return;
		}

		for (Entry<String, String> baggageItem : baggageItems) {
			baggage.put(baggageItem.getKey(), baggageItem.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanImpl start() {
		// build
		build();

		// if startTimestamp was not specified in the builder, we use the timer
		long nanoTime = 0;
		if (startTimestamp <= 0) {
			startTimestamp = timer.getCurrentTimeMicroseconds();
			nanoTime = timer.getCurrentNanoTime();
		}

		// set start time as last operation so get more accuracy
		span.start(startTimestamp, nanoTime);

		return span;
	}

	/**
	 * Builds (resolves the context) the span but does not start it. The method returns the built
	 * span.
	 *
	 * @return the span which has been built
	 */
	public SpanImpl build() {
		// resolve context
		SpanContextImpl context = SpanContextImpl.build(parent, referenceType, baggage);
		span.setSpanContext(context);

		return span;
	}
}
