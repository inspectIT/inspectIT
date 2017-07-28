package rocks.inspectit.agent.java.tracing.core.transformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpanTransformerTest extends TestBase {

	public static class TransformSpan extends SpanTransformerTest {

		@Mock
		SpanImpl spanImpl;

		@Test
		public void spanNull() {
			AbstractSpan span = SpanTransformer.transformSpan(null);

			assertThat(span, is(nullValue()));
		}

		@Test
		public void contextNull() {
			when(spanImpl.context()).thenReturn(null);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span, is(nullValue()));
		}

		@Test
		public void context() {
			when(spanImpl.context()).thenReturn(new SpanContextImpl(1, 2, 3, null, Collections.<String, String> emptyMap()));

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			SpanIdent spanIdent = span.getSpanIdent();
			assertThat(spanIdent.getId(), is(1L));
			assertThat(spanIdent.getTraceId(), is(2L));
			assertThat(span.getParentSpanId(), is(3L));
		}

		@Test
		public void client() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.isClient()).thenReturn(true);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span, is(instanceOf(ClientSpan.class)));
		}

		@Test
		public void server() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.isClient()).thenReturn(false);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span, is(instanceOf(ServerSpan.class)));
		}

		@Test
		public void reference() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build(SpanContextImpl.build(), References.FOLLOWS_FROM, Collections.<String, String> emptyMap()));
			when(spanImpl.isClient()).thenReturn(false);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getReferenceType(), is(References.FOLLOWS_FROM));
		}

		@Test
		public void propagation() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.getTags()).thenReturn(Collections.singletonMap(ExtraTags.PROPAGATION_TYPE, PropagationType.JMS.toString()));

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getPropagationType(), is(PropagationType.JMS));
			assertThat(span.getTags().size(), is(0));
		}

		@Test
		public void tag() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.getTags()).thenReturn(Collections.singletonMap("key", "value"));

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", "value"));
		}

		@Test
		public void spanKindIgnored() {
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.getTags()).thenReturn(Collections.singletonMap(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER));

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getTags().size(), is(0));
		}

		@Test
		public void time() {
			long micros = 123456L;
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.getStartTimeMicros()).thenReturn(micros);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getTimeStamp(), is(not(nullValue())));
			assertThat(span.getTimeStamp().getTime(), is(123L));
		}

		@Test
		public void duration() {
			double durationMicros = 123456.8856d;
			when(spanImpl.context()).thenReturn(SpanContextImpl.build());
			when(spanImpl.getDuration()).thenReturn(durationMicros);

			AbstractSpan span = SpanTransformer.transformSpan(spanImpl);

			assertThat(span.getTimeStamp(), is(not(nullValue())));
			assertThat(span.getDuration(), is(durationMicros / 1000.d));
		}

		@Test
		public void transformerInterface() {
			when(spanImpl.context()).thenReturn(new SpanContextImpl(1, 2, 3, null, Collections.<String, String> emptyMap()));

			AbstractSpan span = SpanTransformer.INSTANCE.transform(spanImpl);

			SpanIdent spanIdent = span.getSpanIdent();
			assertThat(spanIdent.getId(), is(1L));
			assertThat(spanIdent.getTraceId(), is(2L));
			assertThat(span.getParentSpanId(), is(3L));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void transformerInterfaceWrongClass() {
			SpanTransformer.INSTANCE.transform("some string");
		}
	}

}
