package rocks.inspectit.agent.java.sdk.opentracing.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.Timer;

/**
 * @author Ivan Senic
 *
 */
public class SpanImplTest {

	SpanImpl span;

	@Mock
	Timer timer;

	@Mock
	TracerImpl tracer;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(tracer.getTimer()).thenReturn(timer);
		span = new SpanImpl(tracer);
	}

	public static class Start extends SpanImplTest {

		@Test
		public void happyPath() {
			long startTime = 1442l;

			span.start(startTime, 0);

			assertThat(span.getStartTimeMicros(), is(startTime));
			verify(tracer).spanStarted(span);
			verifyNoMoreInteractions(tracer);
			verifyZeroInteractions(timer);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void startTimeNotProvided() {
			span.start(0, 0);
		}

	}

	public static class Finish extends SpanImplTest {

		@Test
		public void noNanos() {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + 1223l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(endTime);
			span.start(startTime, 0);

			span.finish();

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((double) endTime - startTime));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verify(tracer, atLeastOnce()).getTimer();
			verify(timer).getCurrentTimeMicroseconds();
			verifyNoMoreInteractions(tracer, timer);
		}

		@Test
		public void noNanosExplicit() {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + 1223l;
			span.start(startTime, 0);

			span.finish(endTime);

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((double) endTime - startTime));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verifyNoMoreInteractions(tracer);
			verifyZeroInteractions(timer);
		}

		@Test
		public void nanos() {
			long startTime = System.currentTimeMillis();
			long startTimeNanos = System.nanoTime();
			long endTimeNanos = startTimeNanos + 22546l;
			when(timer.getCurrentNanoTime()).thenReturn(endTimeNanos);
			span.start(startTime, startTimeNanos);

			span.finish();

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((endTimeNanos - startTimeNanos) / 1000.d));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verify(tracer, atLeastOnce()).getTimer();
			verify(timer).getCurrentNanoTime();
			verifyNoMoreInteractions(tracer, timer);
		}

		@Test
		public void nanosExplicit() {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + 1223l;
			span.start(startTime, 5467);

			span.finish(endTime);

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((double) endTime - startTime));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verifyNoMoreInteractions(tracer);
			verifyZeroInteractions(timer);
		}

	}

	public static class Close extends SpanImplTest {

		@Test
		public void noNanos() {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + 1223l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(endTime);
			span.start(startTime, 0);

			span.close();

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((double) endTime - startTime));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verify(tracer, atLeastOnce()).getTimer();
			verify(timer).getCurrentTimeMicroseconds();
			verifyNoMoreInteractions(tracer, timer);
		}

		@Test
		public void nanos() {
			long startTime = System.currentTimeMillis();
			long startTimeNanos = System.nanoTime();
			long endTimeNanos = startTimeNanos + 22546l;
			when(timer.getCurrentNanoTime()).thenReturn(endTimeNanos);
			span.start(startTime, startTimeNanos);

			span.close();

			assertThat(span.getStartTimeMicros(), is(startTime));
			assertThat(span.getDuration(), is((endTimeNanos - startTimeNanos) / 1000.d));
			verify(tracer).spanStarted(span);
			verify(tracer).spanEnded(span);
			verify(tracer, atLeastOnce()).getTimer();
			verify(timer).getCurrentNanoTime();
			verifyNoMoreInteractions(tracer, timer);
		}
	}

	public static class SetTag extends SpanImplTest {

		@Test
		public void booleanTag() {
			span.setTag("key", false);

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", String.valueOf(false)));
		}

		@Test
		public void numberTag() {
			span.setTag("key", 5l);

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", String.valueOf(5L)));
		}

		@Test
		public void stringTag() {
			span.setTag("key", "value");

			assertThat(span.getTags().size(), is(1));
			assertThat(span.getTags(), hasEntry("key", "value"));
		}
	}

	public static class Context extends SpanImplTest {

		@Test
		public void setContext() {
			SpanContextImpl spanContext = SpanContextImpl.build();
			span.setSpanContext(spanContext);

			SpanContextImpl context = span.context();

			assertThat(context, is(spanContext));
		}
	}

	public static class SetBaggageItem extends SpanImplTest {

		@Test
		public void happyPath() {
			SpanContextImpl spanContext = SpanContextImpl.build();
			span.setSpanContext(spanContext);

			span.setBaggageItem("key", "value");

			assertThat(span.context().getBaggageItem("key"), is("value"));
		}

		@Test
		public void contextNull() {
			span.setBaggageItem("key", "value");
		}
	}

	public static class GetBaggageItem extends SpanImplTest {

		@Test
		public void contextNull() {
			String baggageItem = span.getBaggageItem("key");

			assertThat(baggageItem, is(nullValue()));
		}
	}

	public static class IsClient extends SpanImplTest {

		@Test
		public void specified() {
			Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);

			boolean client = span.isClient();

			assertThat(client, is(true));
		}

		@Test
		public void notSpecified() {
			boolean client = span.isClient();

			assertThat(client, is(false));
		}

		@Test
		public void notClient() {
			Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);

			boolean client = span.isClient();

			assertThat(client, is(false));
		}
	}

	public static class IsServer extends SpanImplTest {

		@Test
		public void specified() {
			Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);

			boolean server = span.isServer();

			assertThat(server, is(true));
		}

		@Test
		public void notSpecified() {
			boolean server = span.isServer();

			assertThat(server, is(true));
		}

		@Test
		public void notClient() {
			Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);

			boolean server = span.isServer();

			assertThat(server, is(false));
		}
	}

	public static class Log extends SpanImplTest {

		@Test
		public void onlyPayload() {
			long time = 3444l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			String payload = "sent";

			span.log(payload);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@Test
		public void payloadNull() {
			long time = 3444l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);

			span.log((String) null);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@SuppressWarnings("deprecation")
		@Test
		public void nameAndPayload() {
			long time = 3444l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			String payload = "sent";
			String eventName = "myEventName";

			span.log(eventName, payload);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@Test
		public void asMap() {
			long time = 3444l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("e1", "p1");
			map.put("e2", false);
			map.put("e3", 12d);

			span.log(map);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@Test
		public void asMapNull() {
			Map<String, String> m = null;

			span.log(1L, m);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@Test
		public void onlyPayloadWithTime() {
			long time = 3444l;
			String payload = "sent";

			span.log(time, payload);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);

		}

		@SuppressWarnings("deprecation")
		@Test
		public void nameAndPayloadWithTime() {
			long time = 3444l;
			String payload = "sent";
			String eventName = "myEventName";

			span.log(time, eventName, payload);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}

		@Test
		public void asMapWihtTime() {
			long time = 3444l;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("e1", "p1");
			map.put("e2", false);
			map.put("e3", 12d);

			span.log(time, map);

			// not impemneted, ignored
			verifyNoMoreInteractions(timer, tracer);
		}
	}
}
