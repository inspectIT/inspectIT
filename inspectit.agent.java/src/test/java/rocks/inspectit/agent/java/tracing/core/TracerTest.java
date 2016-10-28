package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Headers;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.all.tracing.util.ConversionUtil;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class TracerTest extends TestBase {

	@InjectMocks
	Tracer tracer;

	public static class GetOrCreateCurrentSpan extends TracerTest {

		@Test
		public void create() {
			ServerSpan span = tracer.getOrCreateCurrentSpan();

			assertThat(span, is(not(nullValue())));
			assertThat(span.getSpanIdent(), is(not(nullValue())));
			assertThat(tracer.getCurrentSpan(), is(span));
		}

		@Test
		public void callTwice() {
			ServerSpan first = tracer.getOrCreateCurrentSpan();

			ServerSpan span = tracer.getOrCreateCurrentSpan();

			assertThat(span, is(not(nullValue())));
			assertThat(span.getSpanIdent(), is(not(nullValue())));
			assertThat(first == span, is(true));
			assertThat(tracer.getCurrentSpan(), is(span));
		}
	}

	public static class GetCurrentSpan extends TracerTest {

		@Test
		public void notCreated() {
			ServerSpan span = tracer.getCurrentSpan();

			assertThat(span, is(nullValue()));
		}
	}

	public static class UpdateCurrentSpan extends TracerTest {

		@Test
		public void notCreated() {
			ServerSpan span = new ServerSpan();
			span.setSpanIdent(SpanIdent.build());

			tracer.updateCurrentSpan(span);

			assertThat(tracer.getCurrentSpan(), is(span));
		}

		@Test
		public void created() {
			ServerSpan span = new ServerSpan();
			span.setSpanIdent(SpanIdent.build());
			tracer.getOrCreateCurrentSpan();

			tracer.updateCurrentSpan(span);

			assertThat(tracer.getCurrentSpan(), is(span));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void noSpanIdent() {
			ServerSpan span = new ServerSpan();

			tracer.updateCurrentSpan(span);
		}

		@Test
		public void updatedWithNull() {
			tracer.getOrCreateCurrentSpan();

			tracer.updateCurrentSpan(null);

			assertThat(tracer.getCurrentSpan(), is(nullValue()));
		}
	}

	public static class RemoveCurrentSpan extends TracerTest {

		@Test
		public void notCreated() {
			ServerSpan span = tracer.removeCurrentSpan();

			assertThat(span, is(nullValue()));
			assertThat(tracer.getCurrentSpan(), is(nullValue()));
		}

		@Test
		public void created() {
			ServerSpan created = tracer.getOrCreateCurrentSpan();

			ServerSpan span = tracer.removeCurrentSpan();

			assertThat(span == created, is(true));
			assertThat(tracer.getCurrentSpan(), is(nullValue()));
		}
	}

	public static class CreateClientSpan extends TracerTest {

		@Test
		public void noServerSpanCreated() {
			ClientSpan clientSpan = tracer.createClientSpan();

			assertThat(clientSpan, is(notNullValue()));
			assertThat(clientSpan.getSpanIdent().isRoot(), is(true));
		}

		@Test
		public void serverSpanCreated() {
			ServerSpan serverSpan = tracer.getOrCreateCurrentSpan();

			ClientSpan clientSpan = tracer.createClientSpan();

			assertThat(clientSpan, is(notNullValue()));
			assertThat(serverSpan, is(notNullValue()));
			assertThat(clientSpan.getSpanIdent().getTraceId(), is(serverSpan.getSpanIdent().getTraceId()));
			assertThat(clientSpan.getSpanIdent().getParentId(), is(serverSpan.getSpanIdent().getId()));
			assertThat(clientSpan.getSpanIdent().isRoot(), is(false));
		}

		@Test
		public void twoCreations() {
			tracer.getOrCreateCurrentSpan();
			tracer.createClientSpan();

			ClientSpan clientSpan = tracer.createClientSpan();

			assertThat(tracer.removeClientSpan(), is(clientSpan));
		}

		@Test
		public void twoClientSpans() {
			tracer.createClientSpan();
			tracer.removeClientSpan();

			ClientSpan secondClientSpan = tracer.createClientSpan();

			assertThat(secondClientSpan, is(notNullValue()));
			assertThat(secondClientSpan.getSpanIdent().isRoot(), is(true));
		}
	}

	public static class RemoveClientSpan extends TracerTest {

		@Test
		public void notCreated() {
			ClientSpan clientSpan = tracer.removeClientSpan();

			assertThat(clientSpan, is(nullValue()));
		}

		@Test
		public void created() {
			ClientSpan created = tracer.createClientSpan();

			ClientSpan clientSpan = tracer.removeClientSpan();

			assertThat(clientSpan == created, is(true));
		}
	}

	public static class InjectToRequest extends TracerTest {

		@Mock
		BaggageInjectAdapter adapter;

		@Test
		public void happyPath() {
			SpanIdent spanIdent = SpanIdent.build();

			tracer.injectToRequest(adapter, spanIdent);

			verify(adapter).putBaggageItem(Headers.SPAN_ID, ConversionUtil.toHexString(spanIdent.getId()));
			verify(adapter).putBaggageItem(Headers.TRACE_ID, ConversionUtil.toHexString(spanIdent.getTraceId()));
			verify(adapter).putBaggageItem(Headers.SPAN_PARENT_ID, ConversionUtil.toHexString(spanIdent.getParentId()));
			verifyNoMoreInteractions(adapter);
		}

		@Test
		public void adapterSpanIdent() {
			tracer.injectToRequest(adapter, null);

			verifyZeroInteractions(adapter);
		}
	}

	public static class ExtractFromRequest extends TracerTest {

		@Mock
		BaggageExtractAdapter adapter;

		@Test
		public void happyPath() {
			long spanId = 1;
			long traceId = 2;
			long parentId = 3;
			when(adapter.getBaggageItem(Headers.SPAN_ID)).thenReturn(ConversionUtil.toHexString(spanId));
			when(adapter.getBaggageItem(Headers.TRACE_ID)).thenReturn(ConversionUtil.toHexString(traceId));
			when(adapter.getBaggageItem(Headers.SPAN_PARENT_ID)).thenReturn(ConversionUtil.toHexString(parentId));

			SpanIdent spanIdent = tracer.extractFromRequest(adapter);

			assertThat(spanIdent.getId(), is(spanId));
			assertThat(spanIdent.getTraceId(), is(traceId));
			assertThat(spanIdent.getParentId(), is(parentId));
		}

		@Test
		public void noData() {
			SpanIdent spanIdent = tracer.extractFromRequest(adapter);

			assertThat(spanIdent, is(nullValue()));
		}

		@Test
		public void missingId() {
			long traceId = 2;
			long parentId = 3;
			when(adapter.getBaggageItem(Headers.TRACE_ID)).thenReturn(ConversionUtil.toHexString(traceId));
			when(adapter.getBaggageItem(Headers.SPAN_PARENT_ID)).thenReturn(ConversionUtil.toHexString(parentId));

			SpanIdent spanIdent = tracer.extractFromRequest(adapter);

			assertThat(spanIdent, is(nullValue()));
		}

		@Test
		public void missingTraceId() {
			long spanId = 1;
			long parentId = 3;
			when(adapter.getBaggageItem(Headers.SPAN_ID)).thenReturn(ConversionUtil.toHexString(spanId));
			when(adapter.getBaggageItem(Headers.SPAN_PARENT_ID)).thenReturn(ConversionUtil.toHexString(parentId));

			SpanIdent spanIdent = tracer.extractFromRequest(adapter);

			assertThat(spanIdent, is(nullValue()));
		}

	}
}