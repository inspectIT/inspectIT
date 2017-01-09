package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.sdk.opentracing.ExtendedTracer;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.Timer;
import rocks.inspectit.agent.java.sdk.opentracing.TracerProvider;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.Propagator;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class TracerImplTest extends TestBase {

	TracerImpl tracer;

	@Mock
	Timer timer;

	@Mock
	Reporter reporter;

	@BeforeMethod
	public void init() {
		tracer = new TracerImpl(timer, reporter, false);
	}

	public static class Constructor extends TracerImplTest {

		@Test
		public void noArg() {
			tracer = new TracerImpl();

			assertThat(tracer.getTimer(), is(not(nullValue())));
			assertThat(TracerProvider.get(false), is(nullValue()));
		}

		@Test
		public void setToProvider() {
			tracer = new TracerImpl(timer, reporter, true);

			assertThat(tracer.getTimer(), is(not(nullValue())));
			assertThat(TracerProvider.get(), is((ExtendedTracer) tracer));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullTimer() {
			new TracerImpl(null, reporter, false);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullReporter() {
			new TracerImpl(timer, null, false);
		}

	}

	public static class BuildSpan extends TracerImplTest {

		@Test
		public void base() {
			long time = 122254L;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);

			SpanBuilderImpl builder = tracer.buildSpan();

			SpanImpl span = builder.start();
			assertThat(span.getOperationName(), is(nullValue()));
			assertThat(span.getStartTimeMicros(), is(time));
			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
		}

		@Test
		public void withOperationName() {
			long time = 122254L;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			String operationName = "op";

			SpanBuilderImpl builder = tracer.buildSpan(operationName);

			SpanImpl span = builder.start();
			assertThat(span.getOperationName(), is(operationName));
			assertThat(span.getStartTimeMicros(), is(time));
			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
		}

		@Test
		public void withThreadContext() {
			long time = 122254L;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			String operationName = "op";
			String referenceType = References.FOLLOWS_FROM;
			SpanImpl first = tracer.buildSpan().start();

			SpanBuilderImpl builder = tracer.buildSpan(operationName, referenceType, true);

			SpanImpl span = builder.start();
			assertThat(span.getOperationName(), is(operationName));
			assertThat(span.getStartTimeMicros(), is(time));
			assertThat(span.context().getReferenceType(), is(referenceType));
			assertThat(span.context().getTraceId(), is(first.context().getTraceId()));
			assertThat(span.context().getParentId(), is(first.context().getId()));
			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
		}

		@Test
		public void withoutThreadContext() {
			long time = 122254L;
			when(timer.getCurrentTimeMicroseconds()).thenReturn(time);
			String operationName = "op";
			String referenceType = References.FOLLOWS_FROM;
			tracer.buildSpan().start();

			SpanBuilderImpl builder = tracer.buildSpan(operationName, referenceType, false);

			SpanImpl span = builder.start();
			assertThat(span.getOperationName(), is(operationName));
			assertThat(span.getStartTimeMicros(), is(time));
			assertThat(span.context().getReferenceType(), is(nullValue()));
			assertThat(span.context().getParentId(), is(span.context().getId()));
			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
		}

	}

	public static class Inject extends TracerImplTest {

		@Mock
		Propagator<TextMap> propagator;

		@Mock
		SpanContextImpl context;

		@Mock
		TextMap carrier;

		@Test
		public void happyPath() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			tracer.inject(context, Format.Builtin.TEXT_MAP, carrier);

			verify(propagator).inject(context, carrier);
			verifyNoMoreInteractions(propagator);
		}

		@Test
		public void carrierNull() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			tracer.inject(context, Format.Builtin.TEXT_MAP, null);

			verifyZeroInteractions(propagator);
		}

		@Test
		public void formatNull() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			tracer.inject(context, null, carrier);

			verifyZeroInteractions(propagator);
		}

		@Test
		public void wrongContext() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			tracer.inject(mock(SpanContext.class), Format.Builtin.TEXT_MAP, carrier);

			verifyZeroInteractions(propagator);
		}

		@Test
		public void nonExistingPropagator() {
			Format<String> format = new Format<String>() {
			};
			String string = "test";

			tracer.inject(context, format, string);
		}

	}

	public static class Extract extends TracerImplTest {

		@Mock
		Propagator<TextMap> propagator;

		@Mock
		SpanContextImpl context;

		@Mock
		TextMap carrier;

		@Test
		public void happyPath() {
			when(propagator.extract(carrier)).thenReturn(context);
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			SpanContext extracted = tracer.extract(Format.Builtin.TEXT_MAP, carrier);

			assertThat(extracted, is((SpanContext) context));
			verify(propagator).extract(carrier);
			verifyNoMoreInteractions(propagator);
		}

		@Test
		public void carrierNull() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			SpanContext extracted = tracer.extract(Format.Builtin.TEXT_MAP, null);

			assertThat(extracted, is(nullValue()));
			verifyZeroInteractions(propagator);
		}

		@Test
		public void formatNull() {
			tracer.registerPropagator(Format.Builtin.TEXT_MAP, propagator);

			SpanContext extracted = tracer.extract(null, carrier);

			assertThat(extracted, is(nullValue()));
			verifyZeroInteractions(propagator);
		}

		@Test
		public void nonExistingPropagator() {
			Format<String> format = new Format<String>() {
			};
			String string = "test";

			SpanContext extracted = tracer.extract(format, string);

			assertThat(extracted, is(nullValue()));
		}

	}

	public static class SpanStarted extends TracerImplTest {

		@Test
		public void happyPath() {
			SpanImpl span = new SpanImpl(tracer);
			span.setSpanContext(SpanContextImpl.build());

			tracer.spanStarted(span);

			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
		}

		@Test
		public void spanNull() {
			tracer.spanStarted(null);

			assertThat(tracer.getCurrentContext(), is(nullValue()));
			assertThat(tracer.isCurrentContextExisting(), is(false));
		}
	}

	public static class SpanEnded extends TracerImplTest {

		@Test
		public void happyPath() {
			SpanImpl span = new SpanImpl(tracer);
			span.setSpanContext(SpanContextImpl.build());
			tracer.spanStarted(span);

			tracer.spanEnded(span);

			assertThat(tracer.getCurrentContext(), is(nullValue()));
			assertThat(tracer.isCurrentContextExisting(), is(false));
			verify(reporter).report(span);
			verifyNoMoreInteractions(reporter);
		}

		@Test
		public void spanNull() {
			SpanImpl span = new SpanImpl(tracer);
			span.setSpanContext(SpanContextImpl.build());
			tracer.spanStarted(span);

			tracer.spanEnded(null);

			assertThat(tracer.getCurrentContext(), is(span.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
			verifyZeroInteractions(reporter);
		}

		@Test
		public void noReporting() {
			SpanImpl span = new SpanImpl(tracer);
			span.setSpanContext(SpanContextImpl.build());
			span.setReport(false);
			tracer.spanStarted(span);

			tracer.spanEnded(span);

			assertThat(tracer.getCurrentContext(), is(nullValue()));
			assertThat(tracer.isCurrentContextExisting(), is(false));
			verifyZeroInteractions(reporter);
		}

		@Test
		public void twoSpans() {
			SpanImpl span1 = new SpanImpl(tracer);
			span1.setSpanContext(SpanContextImpl.build());
			SpanImpl span2 = new SpanImpl(tracer);
			span2.setSpanContext(SpanContextImpl.build());
			tracer.spanStarted(span1);
			tracer.spanStarted(span2);

			tracer.spanEnded(span2);

			assertThat(tracer.getCurrentContext(), is(span1.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
			verify(reporter).report(span2);
			verifyNoMoreInteractions(reporter);
		}

		@Test
		public void twoSpansFirstEndsBeforeSecond() {
			SpanImpl span1 = new SpanImpl(tracer);
			span1.setSpanContext(SpanContextImpl.build());
			SpanImpl span2 = new SpanImpl(tracer);
			span2.setSpanContext(SpanContextImpl.build());
			tracer.spanStarted(span1);
			tracer.spanStarted(span2);

			tracer.spanEnded(span1);

			assertThat(tracer.getCurrentContext(), is(nullValue()));
			assertThat(tracer.isCurrentContextExisting(), is(false));
			verify(reporter).report(span1);
			verifyNoMoreInteractions(reporter);
		}

		@Test
		public void twoSpansOneNotStarted() {
			SpanImpl span1 = new SpanImpl(tracer);
			span1.setSpanContext(SpanContextImpl.build());
			SpanImpl span2 = new SpanImpl(tracer);
			span2.setSpanContext(SpanContextImpl.build());
			tracer.spanStarted(span1);

			tracer.spanEnded(span2);

			assertThat(tracer.getCurrentContext(), is(span1.context()));
			assertThat(tracer.isCurrentContextExisting(), is(true));
			verify(reporter).report(span2);
			verifyNoMoreInteractions(reporter);
		}
	}

	public static class SetTimer extends TracerImplTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void timerNull() {
			tracer.setTimer(null);
		}

		@Test
		public void set() {
			Timer t = mock(Timer.class);

			tracer.setTimer(t);

			assertThat(tracer.getTimer(), is(t));
		}

	}

}
