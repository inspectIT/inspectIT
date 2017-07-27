package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import rocks.inspectit.agent.java.sdk.opentracing.Timer;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpanBuilderImplTest extends TestBase {

	@Mock
	Timer timer;

	@Mock
	TracerImpl tracer;

	@BeforeMethod
	public void init() {
		when(tracer.getTimer()).thenReturn(timer);
	}

	public static class Start extends SpanBuilderImplTest {

		Random random = new Random();

		@Test
		public void base() {
			String op = "operation";
			long currentTime = System.currentTimeMillis();
			long nano1 = random.nextLong();
			long nano2 = nano1 + random.nextInt(5000);
			when(timer.getCurrentTimeMicroseconds()).thenReturn(currentTime);
			when(timer.getCurrentNanoTime()).thenReturn(nano1).thenReturn(nano2);
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, op);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.getOperationName(), is(op));
			assertThat(span.getStartTimeMicros(), is(currentTime));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
			// for duration finish
			span.finish();
			assertThat(span.getDuration(), is((nano2 - nano1) / 1000.0d));
			// verify tracer passed
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verify(tracer, atLeastOnce()).getTimer();
			verify(timer).getCurrentTimeMicroseconds();
			verify(timer, times(2)).getCurrentNanoTime();
			verifyNoMoreInteractions(tracer, timer);
		}

		@Test
		public void operationNull() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null);

			SpanImpl span = builder.start();

			assertThat(span.getOperationName(), is(nullValue()));
		}

		@Test
		public void parent() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContextImpl parent = new SpanContextImpl(1, 2, 3, null, Collections.<String, String> singletonMap("key", "value"));
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).addReference(References.FOLLOWS_FROM, parent);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.context().getParentId(), is(parent.getId()));
			assertThat(span.context().getTraceId(), is(parent.getTraceId()));
			assertThat(span.context().getReferenceType(), is(References.FOLLOWS_FROM));
			Map<String, String> contextBaggage = mapFromEntryIterator(span.context().baggageItems());
			assertThat(contextBaggage.size(), is(1));
			assertThat(contextBaggage, hasEntry("key", "value"));
			// assert baggage of the builder as well
			Map<String, String> builderBaggage = mapFromEntryIterator(builder.baggageItems());
			assertThat(builderBaggage.size(), is(1));
			assertThat(builderBaggage, hasEntry("key", "value"));
		}

		@Test
		public void twoParents() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContextImpl parent1 = new SpanContextImpl(1, 2, 3, null, Collections.<String, String> singletonMap("key1", "value1"));
			SpanContextImpl parent2 = new SpanContextImpl(4, 5, 6, null, Collections.<String, String> singletonMap("key2", "value2"));
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).asChildOf(parent1).asChildOf(parent2);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			// first parent is trace responsible
			assertThat(span.context().getParentId(), is(parent1.getId()));
			assertThat(span.context().getTraceId(), is(parent1.getTraceId()));
			assertThat(span.context().getReferenceType(), is(References.CHILD_OF));
			// baggage is taken from both parents
			Map<String, String> contextBaggage = mapFromEntryIterator(span.context().baggageItems());
			assertThat(contextBaggage.size(), is(2));
			assertThat(contextBaggage, hasEntry("key1", "value1"));
			assertThat(contextBaggage, hasEntry("key2", "value2"));
			// assert baggage of the builder as well
			Map<String, String> builderBaggage = mapFromEntryIterator(builder.baggageItems());
			assertThat(builderBaggage.size(), is(2));
			assertThat(builderBaggage, hasEntry("key1", "value1"));
			assertThat(builderBaggage, hasEntry("key2", "value2"));
		}

		@Test
		public void twoParentsOneNotOurImpl() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContext parent1 = mock(SpanContext.class);
			SpanContextImpl parent2 = new SpanContextImpl(4, 5, 6, null, Collections.<String, String> singletonMap("key2", "value2"));
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).asChildOf(parent1).asChildOf(parent2);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			// first parent is trace responsible
			assertThat(span.context().getParentId(), is(parent2.getId()));
			assertThat(span.context().getTraceId(), is(parent2.getTraceId()));
			assertThat(span.context().getReferenceType(), is(References.CHILD_OF));
			// baggage is taken from both parents
			Map<String, String> contextBaggage = mapFromEntryIterator(span.context().baggageItems());
			assertThat(contextBaggage.size(), is(1));
			assertThat(contextBaggage, hasEntry("key2", "value2"));
			// assert baggage of the builder as well
			Map<String, String> builderBaggage = mapFromEntryIterator(builder.baggageItems());
			assertThat(builderBaggage.size(), is(1));
			assertThat(builderBaggage, hasEntry("key2", "value2"));
		}

		@Test
		public void parentNull() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			Span parent = null;
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).asChildOf(parent);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
		}

		@Test
		public void parentContextNull() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContextImpl parent = null;
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).asChildOf(parent);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
		}

		@Test
		public void parentWrongReference() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContextImpl parent = SpanContextImpl.build();
			parent.setBaggageItem("key", "value");
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).addReference("some_reference", parent);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
			assertThat(span.context().getBaggageItem("key"), is("value"));
		}

		@Test
		public void referenceNull() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanContextImpl parent = null;
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).addReference(References.CHILD_OF, parent);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
		}

		@Test
		public void noReport() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).doNotReport();

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.isReport(), is(false));
		}

		@Test
		public void selfStartTime() {
			long micros = 1422l;
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).withStartTimestamp(micros);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.getStartTimeMicros(), is(micros));
			verifyZeroInteractions(timer);
		}

		@Test
		public void booleanTag() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).withTag("key", false);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", String.valueOf(false)));
		}

		@Test
		public void numberTag() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).withTag("key", 5l);

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", String.valueOf(5L)));
		}

		@Test
		public void stringTag() {
			when(timer.getCurrentTimeMicroseconds()).thenReturn(System.currentTimeMillis());
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).withTag("key", "value");

			SpanImpl span = builder.start();

			assertThat(span.isStarted(), is(true));
			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", "value"));
		}
	}

	public static class Build extends SpanBuilderImplTest {

		@Test
		public void base() {
			String op = "operation";
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, op);

			SpanImpl span = builder.build();

			assertThat(span.isStarted(), is(false));
			assertThat(span.getOperationName(), is(op));
			assertThat(span.context().getId(), is(span.context().getParentId()));
			assertThat(span.context().getReferenceType(), is(nullValue()));
			// verify tracer passed
			verify(tracer, atLeastOnce()).getTimer();
			verifyNoMoreInteractions(tracer);
			verifyZeroInteractions(tracer);
		}

		@Test
		public void parent() {
			SpanContextImpl parent = new SpanContextImpl(1, 2, 3, null, Collections.<String, String> singletonMap("key", "value"));
			SpanBuilderImpl builder = new SpanBuilderImpl(tracer, null).addReference(References.FOLLOWS_FROM, parent);

			SpanImpl span = builder.build();

			assertThat(span.isStarted(), is(false));
			assertThat(span.context().getParentId(), is(parent.getId()));
			assertThat(span.context().getTraceId(), is(parent.getTraceId()));
			assertThat(span.context().getReferenceType(), is(References.FOLLOWS_FROM));
			Map<String, String> contextBaggage = mapFromEntryIterator(span.context().baggageItems());
			assertThat(contextBaggage.size(), is(1));
			assertThat(contextBaggage, hasEntry("key", "value"));
			// assert baggage of the builder as well
			Map<String, String> builderBaggage = mapFromEntryIterator(builder.baggageItems());
			assertThat(builderBaggage.size(), is(1));
			assertThat(builderBaggage, hasEntry("key", "value"));
		}
	}

	private static <K, V> Map<K, V> mapFromEntryIterator(Iterable<Entry<K, V>> i) {
		Map<K, V> map = new HashMap<K, V>();
		for (Entry<K, V> e : i) {
			map.put(e.getKey(), e.getValue());
		}
		return map;

	}
}
