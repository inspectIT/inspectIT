package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.Map.Entry;

import org.testng.annotations.Test;

import io.opentracing.References;

/**
 * @author Ivan Senic
 *
 */
public class SpanContextImplTest {

	public static class Build extends SpanContextImplTest {

		@Test
		public void rootContext() {
			SpanContextImpl context = SpanContextImpl.build();

			assertThat(context.getTraceId(), is(context.getId()));
			assertThat(context.getParentId(), is(context.getId()));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
		}

		@Test
		public void rootContextWithBaggage() {
			SpanContextImpl context = SpanContextImpl.build(Collections.singletonMap("k", "v"));

			assertThat(context.getTraceId(), is(context.getId()));
			assertThat(context.getParentId(), is(context.getId()));
			assertThat(context.baggageItems().iterator().hasNext(), is(true));
			Entry<String, String> entry = context.baggageItems().iterator().next();
			assertThat(entry.getKey(), is("k"));
			assertThat(entry.getValue(), is("v"));
		}

		@Test
		public void rootContextWithBaggageNull() {
			SpanContextImpl context = SpanContextImpl.build(null);

			assertThat(context.getTraceId(), is(context.getId()));
			assertThat(context.getParentId(), is(context.getId()));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
		}

		@Test
		public void childContext() {
			SpanContextImpl parent = SpanContextImpl.build();

			SpanContextImpl context = SpanContextImpl.build(parent, References.FOLLOWS_FROM, Collections.<String, String> emptyMap());

			assertThat(context.getTraceId(), is(parent.getTraceId()));
			assertThat(context.getParentId(), is(parent.getId()));
			assertThat(context.getId(), is(not(parent.getId())));
			assertThat(context.getId(), is(not(parent.getTraceId())));
			assertThat(context.getId(), is(not(parent.getParentId())));
			assertThat(context.getReferenceType(), is(References.FOLLOWS_FROM));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
		}

		@Test
		public void childContextWithBaggage() {
			SpanContextImpl parent = SpanContextImpl.build();

			SpanContextImpl context = SpanContextImpl.build(parent, References.CHILD_OF, Collections.singletonMap("k", "v"));

			assertThat(context.getTraceId(), is(parent.getTraceId()));
			assertThat(context.getParentId(), is(parent.getId()));
			assertThat(context.getId(), is(not(parent.getId())));
			assertThat(context.getId(), is(not(parent.getTraceId())));
			assertThat(context.getId(), is(not(parent.getParentId())));
			assertThat(context.getReferenceType(), is(References.CHILD_OF));
			assertThat(context.baggageItems().iterator().hasNext(), is(true));
			Entry<String, String> entry = context.baggageItems().iterator().next();
			assertThat(entry.getKey(), is("k"));
			assertThat(entry.getValue(), is("v"));
		}

		@Test
		public void childContextReferenceNull() {
			SpanContextImpl parent = SpanContextImpl.build();

			SpanContextImpl context = SpanContextImpl.build(parent, null, Collections.<String, String> emptyMap());

			assertThat(context.getReferenceType(), is(nullValue()));
		}

		@Test
		public void childContextParentNull() {
			SpanContextImpl context = SpanContextImpl.build(null, References.CHILD_OF, Collections.<String, String> emptyMap());

			assertThat(context.getTraceId(), is(context.getId()));
			assertThat(context.getParentId(), is(context.getId()));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
			assertThat(context.getReferenceType(), is(nullValue()));
		}

		@Test
		public void extractedContext() {
			long id = 1;
			long traceId = 2;

			SpanContextImpl context = SpanContextImpl.buildExtractedContext(id, traceId, Collections.singletonMap("k", "v"));

			assertThat(context.getTraceId(), is(traceId));
			assertThat(context.getId(), is(id));
			assertThat(context.getReferenceType(), is(nullValue()));
			assertThat(context.baggageItems().iterator().hasNext(), is(true));
			Entry<String, String> entry = context.baggageItems().iterator().next();
			assertThat(entry.getKey(), is("k"));
			assertThat(entry.getValue(), is("v"));
		}
	}

}
