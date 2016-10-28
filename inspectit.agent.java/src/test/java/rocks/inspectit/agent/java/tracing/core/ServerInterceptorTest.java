package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Headers;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.util.ConversionUtil;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ServerInterceptorTest extends TestBase {

	ServerInterceptor interceptor;

	// test with real tracer implementation on purpose
	Tracer tracer;

	@Mock
	ServerRequestAdapter requestAdapter;

	@Mock
	BaggageExtractAdapter extractAdapter;

	@Mock
	ResponseAdapter responseAdapter;

	@BeforeMethod
	public void setupTracer() {
		tracer = new Tracer();
		interceptor = new ServerInterceptor(tracer);
		when(requestAdapter.getBaggageExtractAdapter()).thenReturn(extractAdapter);
	}

	public static class HandleRequest extends ServerInterceptorTest {

		@Test
		public void happyPath() {
			long spanId = 1;
			long traceId = 2;
			long parentId = 3;
			when(extractAdapter.getBaggageItem(Headers.SPAN_ID)).thenReturn(ConversionUtil.toHexString(spanId));
			when(extractAdapter.getBaggageItem(Headers.TRACE_ID)).thenReturn(ConversionUtil.toHexString(traceId));
			when(extractAdapter.getBaggageItem(Headers.SPAN_PARENT_ID)).thenReturn(ConversionUtil.toHexString(parentId));
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.URL, "value"));

			ServerSpan serverSpan = interceptor.handleRequest(requestAdapter);

			assertThat(serverSpan.getTimeStamp(), is(nullValue()));
			assertThat(serverSpan.getDuration(), is(0d));
			assertThat(serverSpan.getSpanIdent().getId(), is(spanId));
			assertThat(serverSpan.getSpanIdent().getTraceId(), is(traceId));
			assertThat(serverSpan.getSpanIdent().getParentId(), is(parentId));
			assertThat(serverSpan.getPropagationType(), is(PropagationType.HTTP));
			assertThat(serverSpan.getTags(), hasEntry((Tag) Tag.Http.URL, "value"));
			assertThat(tracer.getCurrentSpan(), is(serverSpan));
		}

		@Test
		public void noTracePassed() {
			ServerSpan serverSpan = interceptor.handleRequest(requestAdapter);

			assertThat(serverSpan.getSpanIdent().isRoot(), is(true));
			assertThat(tracer.getCurrentSpan(), is(serverSpan));
		}

		@Test
		public void noTracePassedSpanStarted() {
			ServerSpan currentSpan = tracer.getOrCreateCurrentSpan();

			ServerSpan serverSpan = interceptor.handleRequest(requestAdapter);

			assertThat(serverSpan.getSpanIdent(), is(not(currentSpan.getSpanIdent())));
			assertThat(serverSpan.getSpanIdent().isRoot(), is(true));
			assertThat(tracer.getCurrentSpan() == serverSpan, is(true));
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.getTags()).thenReturn(null);

			ServerSpan serverSpan = interceptor.handleRequest(requestAdapter);

			assertThat(serverSpan.getTags().size(), is(0));
		}
	}

	public static class HandleResponse extends ServerInterceptorTest {

		@Test
		public void happyPath() {
			when(responseAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.STATUS, "200"));
			interceptor.handleRequest(requestAdapter);

			ServerSpan serverSpan = interceptor.handleResponse(responseAdapter);

			assertThat(serverSpan.getTimeStamp(), is(nullValue()));
			assertThat(serverSpan.getDuration(), is(0d));
			assertThat(serverSpan.getTags(), hasEntry((Tag) Tag.Http.STATUS, "200"));
			// assert we don't have any more server span in tracer
			assertThat(tracer.getCurrentSpan(), is(nullValue()));
		}

		@Test
		public void noRequestHandled() {
			ServerSpan serverSpan = interceptor.handleResponse(responseAdapter);

			assertThat(serverSpan, is(nullValue()));
		}

		@Test
		public void tagsNull() {
			when(responseAdapter.getTags()).thenReturn(null);
			interceptor.handleRequest(requestAdapter);

			ServerSpan serverSpan = interceptor.handleResponse(responseAdapter);

			assertThat(serverSpan.getTags().size(), is(0));
		}

		@Test
		public void tagsCombined() {
			when(requestAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.URL, "value"));
			when(responseAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.STATUS, "200"));
			interceptor.handleRequest(requestAdapter);

			ServerSpan serverSpan = interceptor.handleResponse(responseAdapter);
			assertThat(serverSpan.getTags().size(), is(2));
			assertThat(serverSpan.getTags(), hasEntry((Tag) Tag.Http.URL, "value"));
			assertThat(serverSpan.getTags(), hasEntry((Tag) Tag.Http.STATUS, "200"));
		}
	}

}
