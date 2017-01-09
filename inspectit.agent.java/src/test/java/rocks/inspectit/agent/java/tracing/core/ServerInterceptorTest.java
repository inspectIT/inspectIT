package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ServerInterceptorTest extends TestBase {

	@InjectMocks
	ServerInterceptor interceptor;

	@Mock
	TracerImpl tracer;

	@Mock
	SpanImpl span;

	public static class HandleRequest extends ServerInterceptorTest {

		@Mock
		ServerRequestAdapter<TextMap> requestAdapter;

		@Mock
		TextMap carrier;

		@Mock
		SpanBuilderImpl spanBuilder;

		@Mock
		SpanContextImpl context;

		@BeforeMethod
		public void setup() {
			when(requestAdapter.getFormat()).thenReturn(Format.Builtin.TEXT_MAP);
			when(requestAdapter.getCarrier()).thenReturn(carrier);
			when(tracer.buildSpan()).thenReturn(spanBuilder);
			when(spanBuilder.start()).thenReturn(span);
		}

		@Test
		public void happyPath() {
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));
			when(tracer.extract(Format.Builtin.TEXT_MAP, carrier)).thenReturn(context);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan();
			verify(tracer).extract(Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).asChildOf(context);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).withTag(Tags.HTTP_URL.getKey(), "value");
			verify(spanBuilder).start();
			verifyNoMoreInteractions(tracer, spanBuilder);
			verifyZeroInteractions(span, context);
		}

		@Test
		public void noTracePassed() {
			when(tracer.extract(Format.Builtin.TEXT_MAP, carrier)).thenReturn(null);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan();
			verify(tracer).extract(Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).asChildOf((SpanContextImpl) null);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(spanBuilder).start();
			verifyNoMoreInteractions(tracer, spanBuilder);
			verifyZeroInteractions(span, context);
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.getTags()).thenReturn(null);
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(tracer.extract(Format.Builtin.TEXT_MAP, carrier)).thenReturn(context);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan();
			verify(tracer).extract(Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).asChildOf(context);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(spanBuilder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString());
			verify(spanBuilder).start();
			verifyNoMoreInteractions(tracer, spanBuilder);
			verifyZeroInteractions(span, context);
		}

		@Test
		public void propagationNull() {
			when(requestAdapter.getPropagationType()).thenReturn(null);
			when(tracer.extract(Format.Builtin.TEXT_MAP, carrier)).thenReturn(context);

			SpanImpl result = interceptor.handleRequest(requestAdapter);

			assertThat(result, is(span));
			verify(tracer).buildSpan();
			verify(tracer).extract(Format.Builtin.TEXT_MAP, carrier);
			verify(spanBuilder).asChildOf(context);
			verify(spanBuilder).doNotReport();
			verify(spanBuilder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(spanBuilder).start();
			verifyNoMoreInteractions(tracer, spanBuilder);
			verifyZeroInteractions(span, context);
		}
	}

	public static class HandleResponse extends ServerInterceptorTest {

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
