package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Field;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
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

	/**
	 * Tests the
	 * {@link ExecutorIntercepterHook#beforeBody(long, Object, Object[], rocks.inspectit.agent.java.config.impl.SpecialSensorConfig)}
	 * method.
	 *
	 */
	public static class BeforeBody extends ExecutorIntercepterHookTest {

		private Runnable getRunnableFromSpanStore(SpanStoreRunnable store) throws Exception {
			Field field = SpanStoreRunnable.class.getDeclaredField("runnable");
			field.setAccessible(true);
			return (Runnable) field.get(store);
		}

		@Test
		public void successful() throws Exception {
			Object targetObject = mock(Object.class);
			SpecialSensorConfig ssc = mock(SpecialSensorConfig.class);
			Runnable runnable = mock(Runnable.class);
			Object[] parameters = new Object[] { runnable };

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(SpanStoreRunnable.class.isInstance(parameters[0]), is(true));
			assertThat(getRunnableFromSpanStore((SpanStoreRunnable) parameters[0]), is(equalTo(runnable)));
			assertThat(result, is(nullValue()));
			verifyZeroInteractions(targetObject, ssc);
		}

		@Test
		public void parameterIsNoRunnable() throws Exception {
			Object targetObject = mock(Object.class);
			SpecialSensorConfig ssc = mock(SpecialSensorConfig.class);
			Object object = mock(Object.class);
			Object[] parameters = new Object[] { object };

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(parameters[0], is(equalTo(object)));
			assertThat(result, is(nullValue()));
		}

		@Test
		public void parameterIsSpanStore() throws Exception {
			Object targetObject = mock(Object.class);
			SpecialSensorConfig ssc = mock(SpecialSensorConfig.class);
			Object spanStore = mock(SpanStoreRunnable.class);
			Object[] parameters = new Object[] { spanStore };

			Object result = hook.beforeBody(0, targetObject, parameters, ssc);

			assertThat(parameters[0], is(equalTo(spanStore)));
			assertThat(result, is(nullValue()));
		}
	}
}
