package rocks.inspectit.agent.java.tracing.core.transformer;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Span transformer knows to to translate opentracing.io based {@link SpanImpl} to the
 * {@link AbstractSpan}.
 *
 * @author Ivan Senic
 *
 */
public final class SpanTransformer implements Transformer {

	/**
	 * Instance for usage.
	 */
	public static final SpanTransformer INSTANCE = new SpanTransformer();

	/**
	 * Private, use {@link #INSTANCE} or {@link #transformSpan(SpanImpl)}.
	 */
	private SpanTransformer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractSpan transform(Object input) {
		if (input instanceof SpanImpl) {
			return transformSpan((SpanImpl) input);
		}
		throw new IllegalArgumentException("Can not transform the instance of " + input.getClass());
	}

	/**
	 * Transforms the opentracing.io span to our internal representation.
	 *
	 * @param spanImpl
	 *            {@link SpanImpl}.
	 * @return {@link AbstractSpan}.
	 */
	public static AbstractSpan transformSpan(SpanImpl spanImpl) {
		// check not null
		if (null == spanImpl) {
			return null;
		}

		// context to ident
		SpanIdent ident = SpanContextTransformer.transformSpanContext(spanImpl.context());
		if (null == ident) {
			// no sense in creating span without ident
			return null;
		}

		AbstractSpan span = createCorrectSpanType(spanImpl);
		span.setSpanIdent(ident);

		// transform to inspectIT way of time handling
		long timestampMillis = spanImpl.getStartTimeMicros() / 1000;
		double durationMillis = spanImpl.getDuration() / 1000.0d;
		span.setTimeStamp(new Timestamp(timestampMillis));
		span.setDuration(durationMillis);

		// reference
		span.setReferenceType(spanImpl.context().getReferenceType());

		// operation name (we save as tag)
		if (null != spanImpl.getOperationName()) {
			span.addTag(ExtraTags.OPERATION_NAME, spanImpl.getOperationName());
		}

		// tags
		if (MapUtils.isNotEmpty(spanImpl.getTags())) {
			// extra for propagation
			String propagation = spanImpl.getTags().get(ExtraTags.PROPAGATION_TYPE);
			span.setPropagationType(PropagationType.safeValueOf(propagation));


			for (Map.Entry<String, String> entry : spanImpl.getTags().entrySet()) {
				if (!isTagIgnored(entry.getKey())) {
					span.addTag(entry.getKey(), entry.getValue());
				}
			}
		}

		// TODO what do we do about log data
		// we could add that to the ParameterContentData

		return span;
	}

	/**
	 * Creates {@link ClientSpan} or {@link ServerSpan} based on the information from the given
	 * {@link SpanImpl}.
	 *
	 * @param spanImpl
	 *            opentracing span impl
	 * @return {@link AbstractSpan}
	 */
	private static AbstractSpan createCorrectSpanType(SpanImpl spanImpl) {
		if (spanImpl.isClient()) {
			ClientSpan clientSpan = new ClientSpan();
			return clientSpan;
		} else {
			ServerSpan serverSpan = new ServerSpan();
			return serverSpan;
		}
	}

	/**
	 * If tags key is ignored for the copy to the our span representation. Ignored are: propagation
	 * type and spin kind.
	 *
	 * @param key
	 *            tag key
	 * @return if tag with given key should be ignored when copied.
	 */
	private static boolean isTagIgnored(String key) {
		return ExtraTags.PROPAGATION_TYPE.equals(key) || Tags.SPAN_KIND.getKey().equals(key);
	}
}
