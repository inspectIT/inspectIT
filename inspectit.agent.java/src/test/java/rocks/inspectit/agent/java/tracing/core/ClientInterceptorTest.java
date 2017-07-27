package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClientInterceptorTest extends TestBase {

	@InjectMocks
	ClientInterceptor interceptor;

	@Mock
	TracerImpl tracer;

	@Mock
	SpanImpl span;

	public static class HandleRequest extends ClientInterceptorTest {

		@Mock
		SpanBuilderImpl spanBuilder;

		@Mock
		SpanContextImpl context;

		@Mock
		ClientRequestAdapter<TextMap> requestAdapter;

		@Mock
		TextMap carrier;

		@BeforeMethod
		public void setup() {
			when(requestAdapter.getFormat()).thenReturn(Format.Builtin.TEXT_MAP);
			when(requestAdapter.getCarrier()).thenReturn(carrier);
			when(tracer.buildSpan(anyString(), anyString(), anyBoolean())).thenReturn(spanBuilder);
			when(spanBuilder.start()).thenReturn(span);
			when(span.context()).thenReturn(context);
		}

		@Test
		public void happyPath() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(References.CHILD_OF);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.CHILD_OF, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).withTag(Tags.HTTP_URL.getKey(), "value");
			verify(spanBuilder).start();
			verify(span).context();
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}

		@Test
		public void spanShouldNotStart() {
			when(requestAdapter.startClientSpan()).thenReturn(false);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(nullValue()));
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getTags()).thenReturn(null);
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(References.CHILD_OF);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.CHILD_OF, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).start();
			verify(span).context();
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}

		@Test
		public void propagationNull() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getPropagationType()).thenReturn(null);
			when(requestAdapter.getReferenceType()).thenReturn(References.CHILD_OF);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.CHILD_OF, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).start();
			verify(span).context();
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}
	}

	public static class HandleAsyncRequest extends ClientInterceptorTest {

		@Mock
		SpanBuilderImpl spanBuilder;

		@Mock
		SpanContextImpl context;

		@Mock
		AsyncClientRequestAdapter<TextMap> requestAdapter;

		@Mock
		TextMap carrier;

		@Mock
		SpanStoreAdapter spanStoreAdapter;

		@BeforeMethod
		public void setup() {
			when(requestAdapter.getFormat()).thenReturn(Format.Builtin.TEXT_MAP);
			when(requestAdapter.getCarrier()).thenReturn(carrier);
			when(requestAdapter.getSpanStoreAdapter()).thenReturn(spanStoreAdapter);
			when(tracer.buildSpan(anyString(), anyString(), anyBoolean())).thenReturn(spanBuilder);
			when(spanBuilder.build()).thenReturn(span);
			when(span.context()).thenReturn(context);
		}

		@Test
		public void happyPath() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(References.FOLLOWS_FROM);

			SpanImpl result = interceptor.handleAsyncRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.FOLLOWS_FROM, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).withTag(Tags.HTTP_URL.getKey(), "value");
			verify(spanBuilder).build();
			verify(span).context();
			ArgumentCaptor<SpanStore> spanStoreCaptor = ArgumentCaptor.forClass(SpanStore.class);
			verify(spanStoreAdapter).setSpanStore(spanStoreCaptor.capture());
			assertThat(spanStoreCaptor.getValue().getSpan(), is(span));
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}

		@Test
		public void spanShouldNotStart() {
			when(requestAdapter.startClientSpan()).thenReturn(false);

			SpanImpl result = interceptor.handleAsyncRequest(requestAdapter);

			assertThat(result, is(nullValue()));
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getTags()).thenReturn(null);
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(References.FOLLOWS_FROM);

			SpanImpl result = interceptor.handleAsyncRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.FOLLOWS_FROM, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).build();
			verify(span).context();
			ArgumentCaptor<SpanStore> spanStoreCaptor = ArgumentCaptor.forClass(SpanStore.class);
			verify(spanStoreAdapter).setSpanStore(spanStoreCaptor.capture());
			assertThat(spanStoreCaptor.getValue().getSpan(), is(span));
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}

		@Test
		public void propagationNull() {
			when(requestAdapter.startClientSpan()).thenReturn(true);
			when(requestAdapter.getPropagationType()).thenReturn(null);
			when(requestAdapter.getReferenceType()).thenReturn(References.FOLLOWS_FROM);

			SpanImpl result = interceptor.handleAsyncRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan(null, References.FOLLOWS_FROM, true);
			verify(tracer).inject(context, Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
			verify(spanBuilder).build();
			verify(span).context();
			ArgumentCaptor<SpanStore> spanStoreCaptor = ArgumentCaptor.forClass(SpanStore.class);
			verify(spanStoreAdapter).setSpanStore(spanStoreCaptor.capture());
			assertThat(spanStoreCaptor.getValue().getSpan(), is(span));
			verifyNoMoreInteractions(tracer, spanBuilder, span);
			verifyZeroInteractions(context);
		}
	}

	public static class HandleResponse extends ClientInterceptorTest {

		@Mock
		ResponseAdapter responseAdapter;

		@Test
		public void happyPath() {
			when(responseAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_STATUS.getKey(), "200"));

			SpanImpl result = interceptor.handleResponse(span, responseAdapter);

			assertThat(result, is(span));
			verify(span).setTag(Tags.HTTP_STATUS.getKey(), "200");
			verify(span).finish();
			verifyNoMoreInteractions(span);
			verifyZeroInteractions(tracer);
		}

		@Test
		public void noRequestHandled() {
			SpanImpl result = interceptor.handleResponse(null, responseAdapter);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(tracer, span);
		}

		@Test
		public void tagsNull() {
			when(responseAdapter.getTags()).thenReturn(null);

			interceptor.handleResponse(span, responseAdapter);

			verify(span).finish();
			verifyNoMoreInteractions(span);
			verifyZeroInteractions(tracer);
		}


	}
}
