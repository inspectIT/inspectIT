package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Constructor;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")

public class ClassLoadingDelegationHookTest extends TestBase {

	@InjectMocks
	ClassLoadingDelegationHook hook;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	Object object;

	public static class BeforeBody extends ClassLoadingDelegationHookTest {

		static final long METHOD_ID = 11L;

		@Test
		public void happyPath() {
			Object[] parameters = new String[] { ClassLoadingDelegationHookTest.class.getName() };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is((Object) ClassLoadingDelegationHookTest.class));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void notOurClass() {
			Object[] parameters = new String[] { String.class.getName() };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void notExistingOurClass() {
			Object[] parameters = new String[] { "rocks.inspectit.agent.java.StupidClass" };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void emptyParameters() {
			Object[] parameters = new String[] {};

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void nullParameters() {
			Object[] parameters = null;

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void nullFirstParameter() {
			Object[] parameters = new String[] { null };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void tooMuchParameters() {
			Object[] parameters = new String[] { "bla", "nah" };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void wrongParameterType() {
			Object[] parameters = new Long[] { 1L };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(object, ssc);
		}

		@Test
		public void reflectAsmClassLoader() throws Exception {
			Class<?> classLoaderClass = Class.forName("com.esotericsoftware.reflectasm.AccessClassLoader");
			Constructor<?> constructor = classLoaderClass.getDeclaredConstructor(ClassLoader.class);
			constructor.setAccessible(true);
			Object classLoader = constructor.newInstance(this.getClass().getClassLoader());
			Object[] parameters = new String[] { ClassLoadingDelegationHookTest.class.getName() };

			Object result = hook.beforeBody(METHOD_ID, classLoader, parameters, ssc);

			assertThat(result, is(nullValue()));
			verifyZeroInteractions(ssc);
		}

	}

	public static class AfterBody extends ClassLoadingDelegationHookTest {

		@Test
		public void ignored() {
			long methodId = 11L;
			Object[] parameters = new Object[] {};
			Object result = mock(Object.class);

			Object resultAfter = hook.afterBody(methodId, object, parameters, result, ssc);

			assertThat(resultAfter, is(nullValue()));
			verifyZeroInteractions(object, result, ssc);
		}
	}
}
