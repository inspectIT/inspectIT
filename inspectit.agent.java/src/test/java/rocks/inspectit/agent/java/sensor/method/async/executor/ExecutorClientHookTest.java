package rocks.inspectit.agent.java.sensor.method.async.executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
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
	Object result;

	@Mock
	TracerImpl tracer;

	@Mock
	IAsyncSpanContextListener asyncListener;

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
			when(tracer.buildSpan(null, References.FOLLOWS_FROM, true)).thenReturn(builder);
			when(builder.build()).thenReturn(span);
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);

			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, spanContext, false, rsc);

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

			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, null, false, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void noParameters() {
			Object[] parameters = new Object[] {};

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, null, false, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void multipleParameters() {
			Object[] parameters = new Object[] { mock(Object.class), mock(Object.class) };

			hook.beforeBody(0, 0, targetObject, parameters, rsc);

			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, null, false, rsc);

			verifyZeroInteractions(targetObject, rsc, asyncListener);
		}

		@Test
		public void nestedCalls() throws Exception {
			SpanStore spanStore = mock(SpanStore.class);
			Object[] parameters = new Object[] { spanStore };
			SpanBuilderImpl builder = mock(SpanBuilderImpl.class);
			SpanImpl span = mock(SpanImpl.class);
			SpanContextImpl spanContext = mock(SpanContextImpl.class);
			when(span.context()).thenReturn(spanContext);
			when(tracer.buildSpan(null, References.FOLLOWS_FROM, true)).thenReturn(builder);
			when(builder.build()).thenReturn(span);
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);
			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, spanContext, false, rsc);

			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, spanContext, false, rsc);

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
		public void consecutiveCalls() throws Exception {
			SpanStore spanStore = mock(SpanStore.class);
			Object[] parameters = new Object[] { spanStore };
			SpanBuilderImpl builder = mock(SpanBuilderImpl.class);
			SpanImpl span = mock(SpanImpl.class);
			SpanContextImpl spanContext = mock(SpanContextImpl.class);
			when(span.context()).thenReturn(spanContext);
			when(tracer.buildSpan(null, References.FOLLOWS_FROM, true)).thenReturn(builder);
			when(builder.build()).thenReturn(span);
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);
			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, spanContext, false, rsc);

			hook.beforeBody(1L, 2L, targetObject, parameters, rsc);
			hook.firstAfterBody(1L, 2L, targetObject, parameters, result, false, rsc);
			hook.secondAfterBody(null, 1L, 2L, targetObject, parameters, spanContext, false, rsc);

			verify(tracer, times(2)).buildSpan(null, References.FOLLOWS_FROM, true);
			verify(builder, times(2)).withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.PROCESS.toString());
			verify(builder, times(2)).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
			verify(builder, times(2)).withTag(ExtraTags.INSPECTT_METHOD_ID, 1L);
			verify(builder, times(2)).withTag(ExtraTags.INSPECTT_SENSOR_ID, 2L);
			verify(builder, times(2)).build();
			verify(spanStore, times(2)).storeSpan(span);
			verify(asyncListener, times(2)).asyncSpanContextCreated(spanContext);
			verifyNoMoreInteractions(tracer, builder, spanStore, asyncListener);
		}
	}
}
