package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Headers;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.util.ConversionUtil;

/**
 * @author Ivan Senic
 *
 */
public class ClientInterceptorTest extends TestBase {

	ClientInterceptor interceptor;

	// test with real tracer implementation on purpose
	Tracer tracer;

	@Mock
	ClientRequestAdapter requestAdapter;

	@Mock
	BaggageInjectAdapter injectAdapter;

	@Mock
	ResponseAdapter responseAdapter;

	@BeforeMethod
	public void setupTracer() {
		tracer = new Tracer();
		interceptor = new ClientInterceptor(tracer);
		when(requestAdapter.getBaggageInjectAdapter()).thenReturn(injectAdapter);
	}

	public static class HandleRequest extends ClientInterceptorTest {

		@Test
		public void happyPath() {
			when(requestAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.URL, "value"));
			when(requestAdapter.getPropagationType()).thenReturn(PropagationType.HTTP);
			when(requestAdapter.getReferenceType()).thenReturn(ReferenceType.CHILD_OF);

			ClientSpan clientSpan = interceptor.handleRequest(requestAdapter);

			assertThat(clientSpan.getTimeStamp(), is(nullValue()));
			assertThat(clientSpan.getDuration(), is(0d));
			assertThat(clientSpan.getReferenceType(), is(ReferenceType.CHILD_OF));
			assertThat(clientSpan.getPropagationType(), is(PropagationType.HTTP));
			assertThat(clientSpan.getTags(), hasEntry((Tag) Tag.Http.URL, "value"));
			verify(injectAdapter).putBaggageItem(Headers.SPAN_ID, ConversionUtil.toHexString(clientSpan.getSpanIdent().getId()));
			verify(injectAdapter).putBaggageItem(Headers.TRACE_ID, ConversionUtil.toHexString(clientSpan.getSpanIdent().getTraceId()));
			verify(injectAdapter).putBaggageItem(Headers.SPAN_PARENT_ID, ConversionUtil.toHexString(clientSpan.getSpanIdent().getParentId()));
			assertThat(clientSpan.getSpanIdent().isRoot(), is(true));
			// assert we have correct client span in tracer
			assertThat(tracer.removeClientSpan(), is(clientSpan));
		}

		@Test
		public void twoInterceptions() {
			interceptor.handleRequest(requestAdapter);
			ClientSpan clientSpan = interceptor.handleRequest(requestAdapter);

			assertThat(clientSpan, is(not(nullValue())));
			assertThat(clientSpan.getSpanIdent().isRoot(), is(true));
			// assert we have correct client span in tracer
			assertThat(tracer.removeClientSpan(), is(clientSpan));
		}

		@Test
		public void currentSpanAlreadyExists() {
			ServerSpan currentSpan = tracer.getOrCreateCurrentSpan();

			ClientSpan clientSpan = interceptor.handleRequest(requestAdapter);

			assertThat(currentSpan.getSpanIdent().getId(), is(clientSpan.getSpanIdent().getParentId()));
			assertThat(currentSpan.getSpanIdent().getTraceId(), is(clientSpan.getSpanIdent().getTraceId()));
			// assert we have correct client span in tracer
			assertThat(tracer.removeClientSpan(), is(clientSpan));
		}

		@Test
		public void tagsNull() {
			when(requestAdapter.getTags()).thenReturn(null);

			ClientSpan clientSpan = interceptor.handleRequest(requestAdapter);

			assertThat(clientSpan.getTags().size(), is(0));
		}
	}

	public static class HandleResponse extends ClientInterceptorTest {

		@Test
		public void happyPath() {
			when(responseAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.STATUS, "200"));
			interceptor.handleRequest(requestAdapter);

			ClientSpan clientSpan = interceptor.handleResponse(responseAdapter);

			assertThat(clientSpan.getTimeStamp(), is(nullValue()));
			assertThat(clientSpan.getDuration(), is(0d));
			assertThat(clientSpan.getTags(), hasEntry((Tag) Tag.Http.STATUS, "200"));
			// assert we don't have any more client span in tracer
			assertThat(tracer.removeClientSpan(), is(nullValue()));
			// assert that current span is still existing

		}

		@Test
		public void currentSpanAlreadyExists() {
			when(responseAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.STATUS, "200"));
			ServerSpan currentSpan = tracer.getOrCreateCurrentSpan();
			interceptor.handleRequest(requestAdapter);

			ClientSpan clientSpan = interceptor.handleResponse(responseAdapter);

			assertThat(currentSpan.getSpanIdent().getId(), is(clientSpan.getSpanIdent().getParentId()));
			assertThat(currentSpan.getSpanIdent().getTraceId(), is(clientSpan.getSpanIdent().getTraceId()));
			// assert we don't have any more client span in tracer
			assertThat(tracer.removeClientSpan(), is(nullValue()));
			// assert that current span is still existing
			assertThat(tracer.getCurrentSpan(), is(not(nullValue())));
		}

		@Test
		public void noRequestHandled() {
			ClientSpan clientSpan = interceptor.handleResponse(responseAdapter);

			assertThat(clientSpan, is(nullValue()));
		}

		@Test
		public void tagsNull() {
			when(responseAdapter.getTags()).thenReturn(null);
			interceptor.handleRequest(requestAdapter);

			ClientSpan clientSpan = interceptor.handleResponse(responseAdapter);

			assertThat(clientSpan.getTags().size(), is(0));
		}

		@Test
		public void tagsCombined() {
			when(requestAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.URL, "value"));
			when(responseAdapter.getTags()).thenReturn(Collections.<Tag, String> singletonMap(Tag.Http.STATUS, "200"));
			interceptor.handleRequest(requestAdapter);

			ClientSpan clientSpan = interceptor.handleResponse(responseAdapter);
			assertThat(clientSpan.getTags().size(), is(2));
			assertThat(clientSpan.getTags(), hasEntry((Tag) Tag.Http.URL, "value"));
			assertThat(clientSpan.getTags(), hasEntry((Tag) Tag.Http.STATUS, "200"));
		}

	}
}
