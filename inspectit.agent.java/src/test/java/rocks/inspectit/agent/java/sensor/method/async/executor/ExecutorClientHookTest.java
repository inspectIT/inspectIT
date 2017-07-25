package rocks.inspectit.agent.java.sensor.method.async.executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Tests the {@link ExecutorClientHook] class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "PMD" })
public class ExecutorClientHookTest extends TestBase {

	@InjectMocks
	ExecutorClientHook hook;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	Object targetObject;

	@Mock
	TracerImpl tracer;

	@Mock
	IAsyncSpanContextListener asyncListener;

	public StartEndMarker getRefMarker() throws Exception {
		Field field = ExecutorClientHook.class.getDeclaredField("REF_MARKER");
		field.setAccessible(true);
		return (StartEndMarker) field.get(hook);
	}

	@AfterMethod
	public void reset() throws Exception {
		getRefMarker().remove();
	}

	/**
	 * Tests the
	 * {@link ExecutorClientHook#beforeBody(long, long, Object, Object[], rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig)}
	 * method.
	 *
	 */
	public static class BeforeBody extends ExecutorClientHookTest {

		@Test
		public void successful() throws Exception {
			SpanStore spanStore = mock(SpanStore.class);
			Object[] parameters = new Object[] { spanStore };
			SpanBuilderImpl builder = mock(SpanBuilderImpl.class);
			SpanImpl span = mock(SpanImpl.class);
			SpanContextImpl spanContext = mock(SpanContextImpl.class);
			when(span.context()).thenReturn(spanContext);
			when(rsc.getFullTargetMethodName()).thenReturn("method()");
			when(tracer.buildSpan(null, References.FOLLOWS_FROM, true)).thenReturn(builder);
			when(builder.build()).thenReturn(span);
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);

			verify(tracer).isCurrentContextExisting();
			verify(tracer).buildSpan(null, References.FOLLOWS_FROM, true);
			verify(builder).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.PROCESS.toString());
			verify(builder).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(builder).withTag(ExtraTags.INSPECTT_METHOD_ID, 1L);
			verify(builder).withTag(ExtraTags.INSPECTT_SENSOR_ID, 2L);
			verify(builder).build();
			verify(spanStore).storeSpan(span);
			verify(asyncListener).asyncSpanContextCreated(spanContext);
			verifyNoMoreInteractions(tracer, builder, spanStore, asyncListener);
		}


		@Test
		public void parameterIsNoSpanStore() {
			Object[] parameters = new Object[] { mock(Object.class) };

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void noParameters() {
			Object[] parameters = new Object[] {};

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void multipleParameters() {
			Object[] parameters = new Object[] { mock(Object.class), mock(Object.class) };

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void refMarkIsSet() throws Exception {
			getRefMarker().markCall();
			Object[] parameters = new Object[] {};

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			verifyZeroInteractions(targetObject, rsc, tracer, asyncListener);
		}
	}

	/**
	 * Tests the
	 * {@link ExecutorClientHook#firstAfterBody(long, long, Object, Object[], Object, boolean, RegisteredSensorConfig)}
	 * method.
	 *
	 */
	public static class FirstAfterBody extends ExecutorClientHookTest {
		@Test
		public void verifyRefMarkerEndCall() throws Exception {
			getRefMarker().markCall();

			hook.firstAfterBody(0, 0, null, null, null, false, null);

			assertThat(getRefMarker().matchesFirst(), is(true));
		}
	}

	/**
	 * Tests the
	 * {@link ExecutorClientHook#secondAfterBody(rocks.inspectit.agent.java.core.ICoreService, long, long, Object, Object[], Object, boolean, RegisteredSensorConfig)}
	 * method.
	 *
	 */
	public static class SecondAfterBody extends ExecutorClientHookTest {

		@Test
		public void isFirst() throws Exception {
			getRefMarker().markCall();
			getRefMarker().markEndCall();

			hook.secondAfterBody(null, 0L, 0L, null, null, null, false, null);

			assertThat(getRefMarker().isMarkerSet(), is(false));
		}

		@Test
		public void isNotFirst() throws Exception {
			getRefMarker().markCall();
			getRefMarker().markCall();
			getRefMarker().markEndCall();

			hook.secondAfterBody(null, 0L, 0L, null, null, null, false, null);

			assertThat(getRefMarker().isMarkerSet(), is(true));
		}
	}
}
