package rocks.inspectit.agent.java.sdk.opentracing.internal.propagation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.sdk.opentracing.internal.constants.PropagationConstants;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.util.ConversionUtils;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class TextMapPropagatorTest extends TestBase {

	@InjectMocks
	TextMapPropagator propagator;

	@Mock
	TextMap carrier;

	public static class Inject extends TextMapPropagatorTest {

		@Test
		public void ids() {
			SpanContextImpl context = SpanContextImpl.build();

			propagator.inject(context, carrier);

			verify(carrier).put(PropagationConstants.SPAN_ID, ConversionUtils.toHexString(context.getId()));
			verify(carrier).put(PropagationConstants.TRACE_ID, ConversionUtils.toHexString(context.getTraceId()));
			verifyNoMoreInteractions(carrier);
		}

		@Test
		public void baggage() {
			SpanContextImpl context = SpanContextImpl.build();
			context.setBaggageItem("key", "value");

			propagator.inject(context, carrier);

			verify(carrier).put(PropagationConstants.INSPECTIT_BAGGAGE_PREFIX + "key", "value");
		}

		@Test
		public void nullContext() {
			propagator.inject(null, carrier);

			verifyZeroInteractions(carrier);
		}
	}

	public static class Extract extends TextMapPropagatorTest {

		@Test
		public void ids() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.SPAN_ID, ConversionUtils.toHexString(1L));
			map.put(PropagationConstants.TRACE_ID, ConversionUtils.toHexString(2L));
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context.getId(), is(1L));
			assertThat(context.getTraceId(), is(2L));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
		}

		@Test
		public void idsWrong() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.SPAN_ID, "whatever");
			map.put(PropagationConstants.TRACE_ID, "whatever");
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context, is(nullValue()));
		}

		@Test
		public void onlySpanid() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.SPAN_ID, ConversionUtils.toHexString(1L));
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context, is(nullValue()));
		}

		@Test
		public void onlyTraceId() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.TRACE_ID, ConversionUtils.toHexString(2L));
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context, is(nullValue()));
		}

		@Test
		public void idsAndBaggage() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.SPAN_ID, ConversionUtils.toHexString(1L));
			map.put(PropagationConstants.TRACE_ID, ConversionUtils.toHexString(2L));
			map.put(PropagationConstants.INSPECTIT_BAGGAGE_PREFIX + "key", "value");
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context.getId(), is(1L));
			assertThat(context.getTraceId(), is(2L));
			Entry<String, String> entry = context.baggageItems().iterator().next();
			assertThat(entry.getKey(), is("key"));
			assertThat(entry.getValue(), is("value"));
		}

		@Test
		public void idsAndNotOurBaggage() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(PropagationConstants.SPAN_ID, ConversionUtils.toHexString(1L));
			map.put(PropagationConstants.TRACE_ID, ConversionUtils.toHexString(2L));
			map.put("somekey", "value");
			when(carrier.iterator()).thenReturn(map.entrySet().iterator());

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context.getId(), is(1L));
			assertThat(context.getTraceId(), is(2L));
			assertThat(context.baggageItems().iterator().hasNext(), is(false));
		}

		@Test
		public void carrierProvidesNullIterator() {
			when(carrier.iterator()).thenReturn(null);

			SpanContextImpl context = propagator.extract(carrier);

			assertThat(context, is(nullValue()));
		}
	}
}
