package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.constants.PropagationConstants;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.TracerImpl;
import rocks.inspectit.agent.java.sdk.opentracing.util.ConversionUtil;
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

	ServerInterceptor interceptor;

	// test with real tracer implementation on purpose
	TracerImpl tracer;

	@Mock
	ServerRequestAdapter<TextMap> requestAdapter;

	@Mock
	TextMap carrier;

	@Mock
	ResponseAdapter responseAdapter;

	@BeforeMethod
	public void setupTracer() {
		tracer = new TracerImpl();
		interceptor = new ServerInterceptor(tracer);
		when(requestAdapter.getFormat()).thenReturn(Format.Builtin.TEXT_MAP);
		when(requestAdapter.getCarrier()).thenReturn(carrier);
	}

	public static class HandleRequest extends ServerInterceptorTest {

		@Test
		public void happyPath() {
			long spanId = 1;
			long traceId = 2;
			String baggageExtra = "extra";
			// setup
			Map<String, String> traceData = new HashMap<String, String>();
			traceData.put(PropagationConstants.SPAN_ID, ConversionUtil.toHexString(spanId));
			traceData.put(PropagationConstants.TRACE_ID, ConversionUtil.toHexString(traceId));
			traceData.put(PropagationConstants.INSPECTIT_BAGGAGE_PREFIX + baggageExtra, baggageExtra);
			when(carrier.iterator()).thenReturn(traceData.entrySet().iterator());
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getTags()).thenReturn(Collections.<String, String> singletonMap(Tags.HTTP_URL.getKey(), "value"));

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.getStartTimeMicros(), is(not(0L)));
			assertThat(span.getDuration(), is(0d));
			assertThat(span.context().getParentId(), is(spanId));
			assertThat(span.context().getTraceId(), is(traceId));
			assertThat(span.context().getBaggageItem(baggageExtra), is(baggageExtra));
			assertThat(span.getTags(), hasEntry(ExtraTags.PROPAGATION_TYPE, PropagationType.HTTP.toString()));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_URL.getKey(), "value"));
			assertThat(span.isServer(), is(true));
			assertThat(span.isReport(), is(false));
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test
		public void noCarrier() {
			when(requestAdapter.getCarrier()).thenReturn(null);

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test
		public void noTracePassed() {
			when(carrier.iterator()).thenReturn(Collections.<String, String> emptyMap().entrySet().iterator());

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test
		public void noTracePassedSpanStarted() {
			when(carrier.iterator()).thenReturn(Collections.<String, String> emptyMap().entrySet().iterator());
			SpanImpl first = tracer.buildSpan().start();

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.context().getParentId(), is(first.context().getId()));
			assertThat(span.context().getTraceId(), is(first.context().getTraceId()));
			assertThat(tracer.getCurrentContext(), is(span.context()));
		}

		@Test(enabled = false)
		public void tracePassedSpanStarted() {
			// disabled until we agree on handling this situations
			long spanId = 1;
			long traceId = 2;
			Map<String, String> traceData = new HashMap<String, String>();
			traceData.put(PropagationConstants.SPAN_ID, ConversionUtil.toHexString(spanId));
			traceData.put(PropagationConstants.TRACE_ID, ConversionUtil.toHexString(traceId));
			when(carrier.iterator()).thenReturn(traceData.entrySet().iterator());
			@SuppressWarnings("unused")
			SpanImpl first = tracer.buildSpan().start();

			SpanImpl span = interceptor.handleRequest(requestAdapter);

			assertThat(span.context().getParentId(), is(spanId));
			assertThat(span.context().getTraceId(), is(traceId));
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

	public static class HandleResponse extends ServerInterceptorTest {

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
			// assert we don't have any more context in tracer
			assertThat(tracer.getCurrentContext(), is(nullValue()));
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
			assertThat(span.getTags(), hasEntry(Tags.HTTP_URL.getKey(), "value"));
			assertThat(span.getTags(), hasEntry(Tags.HTTP_STATUS.getKey(), "200"));
			assertThat(span.getTags(), hasKey(Tags.SPAN_KIND.getKey()));
		}
	}

}
