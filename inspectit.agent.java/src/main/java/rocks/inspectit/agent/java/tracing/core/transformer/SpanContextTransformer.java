package rocks.inspectit.agent.java.tracing.core.transformer;

import org.apache.commons.collections.Transformer;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Span context transformer knows to to translate opentracing.io based {@link SpanContextImpl} to
 * the {@link SpanIdent}.
 *
 * @author Ivan Senic
 *
 */
public final class SpanContextTransformer implements Transformer {

	/**
	 * Instance for usage.
	 */
	public static final SpanContextTransformer INSTANCE = new SpanContextTransformer();

	/**
	 * Private, use {@link #INSTANCE} or {@link #transformSpanContext(SpanContextImpl)}.
	 */
	private SpanContextTransformer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanIdent transform(Object input) {
		if (input instanceof SpanContextImpl) {
			return transformSpanContext((SpanContextImpl) input);
		}
		throw new IllegalArgumentException("Can not transform the instance of " + input.getClass());
	}

	/**
	 * Transforms the {@link SpanContextImpl} to the {@link SpanIdent}.
	 *
	 * @param context
	 *            context.
	 * @return {@link SpanIdent}.
	 */
	public static SpanIdent transformSpanContext(SpanContextImpl context) {
		if (null == context) {
			return null;
		}

		return new SpanIdent(context.getId(), context.getTraceId(), context.getParentId());
	}

}
