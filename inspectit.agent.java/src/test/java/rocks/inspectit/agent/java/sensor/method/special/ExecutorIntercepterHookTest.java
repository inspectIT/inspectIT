package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.async.executor.SpanStoreRunnable;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ExecutorIntercepterHook} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class ExecutorIntercepterHookTest extends TestBase {

	@InjectMocks
	ExecutorIntercepterHook hook;

	@Mock
	TracerImpl tracer;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	Object targetObject;

	/**
	 * Tests the
	 * {@link ExecutorIntercepterHook#beforeBody(long, Object, Object[], rocks.inspectit.agent.java.config.impl.SpecialSensorConfig)}
	 * method.
	 *
	 */
	public static class BeforeBody extends ExecutorIntercepterHookTest {

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void successful() throws Exception {
			Runnable runnable = mock(Runnable.class);
			Object[] parameters = new Object[] { runnable };
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(SpanStoreRunnable.class.isInstance(parameters[0]), is(true));
			assertThat(parameters[0], isA((Class) SpanStoreRunnable.class));
			assertThat(result, is(nullValue()));
			verify(tracer).isCurrentContextExisting();
			verifyNoMoreInteractions(tracer);
			verifyZeroInteractions(targetObject, ssc);
		}

		@Test
		public void tracerContextNotExisting() throws Exception {
			Object object = mock(Object.class);
			Object[] parameters = new Object[] { object };
			when(tracer.isCurrentContextExisting()).thenReturn(false);

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(parameters[0], is(equalTo(object)));
			assertThat(result, is(nullValue()));
		}

		@Test
		public void parameterIsNoRunnable() throws Exception {
			Object object = mock(Object.class);
			Object[] parameters = new Object[] { object };
			when(tracer.isCurrentContextExisting()).thenReturn(true);

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(parameters[0], is(equalTo(object)));
			assertThat(result, is(nullValue()));
		}

		@Test
		public void parameterIsSpanStore() throws Exception {
			Object spanStore = mock(SpanStoreRunnable.class);
			Object[] parameters = new Object[] { spanStore };

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(parameters[0], is(equalTo(spanStore)));
			assertThat(result, is(nullValue()));
		}
	}
}
