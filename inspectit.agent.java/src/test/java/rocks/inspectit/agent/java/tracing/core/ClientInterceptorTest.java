package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.locks.LockSupport;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.constants.PropagationConstants;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.TracerImpl;
import rocks.inspectit.agent.java.sdk.opentracing.util.ConversionUtil;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClientInterceptorTest extends TestBase {

	ClientInterceptor interceptor;

	// test with real tracer implementation on purpose
	TracerImpl tracer;

	@Mock
	ClientRequestAdapter<TextMap> requestAdapter;

	@Mock
	TextMap carrier;

	@Mock
	ResponseAdapter responseAdapter;

	@BeforeMethod
	public void setupTracer() {
		tracer = new TracerImpl();
		interceptor = new ClientInterceptor(tracer);
		when(requestAdapter.getFormat()).thenReturn(Format.Builtin.TEXT_MAP);
		when(requestAdapter.getCarrier()).thenReturn(carrier);
	}

	public static class HandleRequest extends ClientInterceptorTest {

		@Test
		public void happyPath() {
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(References.CHILD_OF);

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.getStartTimeMicros(), is(not(0L)));
			assertThat(span.getDuration(), is(0d));
			assertThat(span.getTags(), hasEntry(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString()));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_URL.getKey(), "value"));
			assertThat(span.context().getReferenceType(), is(nullValue()));
			verify(carrier).put(PropagationConstants.SPAN_ID, ConversionUtil.toHexString(span.context().getId()));
			verify(carrier).put(PropagationConstants.TRACE_ID, ConversionUtil.toHexString(span.context().getTraceId()));
			assertThat(span.isClient(), is(true));
			assertThat(span.isReport(), is(false));
			// assert we have correct client span in tracer
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		// not enabled until we decide how to handle to client spans
		@Test(enabled = false)
		public void twoInterceptions() {
			interceptor.handleRequest(requestAdapter);
			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span, is(not(nullValue())));
			assertThat(span.context().getParentId(), is(not(span.context().getId())));
			// assert we have correct client span in tracer
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test
		public void currentSpanAlreadyExists() {
			when(requestAdapter.getReferenceType()).thenReturn(References.FOLLOWS_FROM);
			SpanImpl serverSpan = tracer.buildSpan().start();

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.context().getParentId(), is(serverSpan.context().getId()));
			assertThat(span.context().getTraceId(), is(serverSpan.context().getTraceId()));
			assertThat(span.context().getReferenceType(), is(References.FOLLOWS_FROM));
			// assert we have correct client span in tracer
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.getTags()).thenReturn(null);

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasKey(Tags.SPAN_KIND.getKey()));
		}
	}

	public static class HandleResponse extends ClientInterceptorTest {

		@Test
		public void happyPath() {
			when(responseAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_STATUS.getKey(), "200"));
			SpanImpl span = interceptor.handleRequest(requestAdapter);
			// wait just a bit so we ensure the duration to be set
			LockSupport.parkNanos(100);

			SpanImpl returnedSpan = interceptor.handleResponse(span, responseAdapter);

			assertThat(returnedSpan == span, is(true));
			assertThat(span.getStartTimeMicros(), is(not(0L)));
			assertThat(span.getDuration(), is(greaterThan(0d)));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_STATUS.getKey(), "200"));
			// assert we don't have any more client span in tracer
			assertThat(tracer.getCurrentContext(), is(nullValue()));
		}

		@Test
		public void currentSpanAlreadyExists() {
			when(requestAdapter.getReferenceType()).thenReturn(References.CHILD_OF);
			when(responseAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_STATUS.getKey(), "200"));
			SpanImpl serverSpan = tracer.buildSpan().start();
			SpanImpl span = interceptor.handleRequest(requestAdapter);

			SpanImpl returnedSpan = interceptor.handleResponse(span, responseAdapter);

			assertThat(returnedSpan == span, is(true));
			assertThat(span.context().getParentId(), is(serverSpan.context().getId()));
			assertThat(span.context().getTraceId(), is(serverSpan.context().getTraceId()));
			// assert we don't have any more client span in tracer
			// assert that current span is still existing
			assertThat(tracer.getCurrentContext(), is(serverSpan.context()));
		}

		@Test
		public void noRequestHandled() {
			SpanImpl span = interceptor.handleResponse(null, responseAdapter);

			assertThat(span, is(nullValue()));
		}

		@Test
		public void tagsNull() {
			when(responseAdapter.getTags()).thenReturn(null);
			SpanImpl span = interceptor.handleRequest(requestAdapter);

			interceptor.handleResponse(span, responseAdapter);

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasKey(Tags.SPAN_KIND.getKey()));
		}

		@Test
		public void tagsCombined() {
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));
			when(responseAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_STATUS.getKey(), "200"));
			SpanImpl span = interceptor.handleRequest(requestAdapter);

			interceptor.handleResponse(span, responseAdapter);

			assertThat(span.getTags().size(), is(3));
			assertThat(span.getTags(), hasKey(Tags.SPAN_KIND.getKey()));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_URL.getKey(), "value"));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_STATUS.getKey(), "200"));
		}

	}
}
