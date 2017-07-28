package rocks.inspectit.agent.java.tracing.core.transformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;

import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Ivan Senic
 *
 */
public class SpanContextTransformerTest {

	public static class ContextToSpanIdent extends SpanContextTransformerTest {

		@Test
		public void happyPath() {
			SpanContextImpl context = new SpanContextImpl(1, 2, 3, null, Collections.<String, String> emptyMap());

			SpanIdent spanIdent = SpanContextTransformer.transformSpanContext(context);

			assertThat(spanIdent.getId(), is(1L));
			assertThat(spanIdent.getTraceId(), is(2L));
		}

		@Test
		public void contextNull() {
			SpanIdent spanIdent = SpanContextTransformer.transformSpanContext(null);

			assertThat(spanIdent, is(nullValue()));
		}

		@Test
		public void transformerInterface() {
			SpanContextImpl context = new SpanContextImpl(1, 2, 3, null, Collections.<String, String> emptyMap());

			SpanIdent spanIdent = SpanContextTransformer.INSTANCE.transform(context);

			assertThat(spanIdent.getId(), is(1L));
			assertThat(spanIdent.getTraceId(), is(2L));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void transformerInterfaceWrongClass() {
			SpanContextTransformer.INSTANCE.transform("some string");
		}
	}
}
