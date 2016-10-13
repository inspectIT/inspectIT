package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.management.MBeanServer;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.sensor.jmx.IMBeanServerListener;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MBeanServerInterceptorHookTest extends TestBase {

	MBeanServerInterceptorHook hook;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	Object object;

	@Mock
	IMBeanServerListener listener;

	@Mock
	MBeanServer server;

	@BeforeMethod
	public void init() {
		hook = new MBeanServerInterceptorHook(Collections.singletonList(listener));
	}

	public static class BeforeBody extends MBeanServerInterceptorHookTest {

		static final long METHOD_ID = 11L;

		@Test
		public void happyPathAdd() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = new Object[] { server };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verify(listener).mbeanServerAdded(server);
			verifyNoMoreInteractions(ssc, listener);
			verifyZeroInteractions(object);
		}

		@Test
		public void happyPathRemove() {
			when(ssc.getTargetMethodName()).thenReturn("removeMBeanServer");
			Object[] parameters = new Object[] { server };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verify(listener).mbeanServerRemoved(server);
			verifyNoMoreInteractions(ssc, listener);
			verifyZeroInteractions(object);
		}

		@Test
		public void notCorrectMethod() {
			when(ssc.getTargetMethodName()).thenReturn("somethingelse");
			Object[] parameters = new Object[] { server };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}


		@Test
		public void emptyParameters() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = new Object[] {};

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}

		@Test
		public void nullParameters() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = null;

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}

		@Test
		public void nullFirstParameter() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = new String[] { null };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}

		@Test
		public void tooMuchParameters() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = new Object[] { server, server };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}

		@Test
		public void wrongParameterType() {
			when(ssc.getTargetMethodName()).thenReturn("addMBeanServer");
			Object[] parameters = new Object[] { 1L };

			Object result = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat(result, is(nullValue()));
			verify(ssc).getTargetMethodName();
			verifyNoMoreInteractions(ssc);
			verifyZeroInteractions(object, listener);
		}
	}

	public static class AfterBody extends MBeanServerInterceptorHookTest {

		@Test
		public void ignored() {
			long methodId = 11L;
			Object[] parameters = new Object[] {};
			Object result = mock(Object.class);

			Object resultAfter = hook.afterBody(methodId, object, parameters, result, ssc);

			assertThat(resultAfter, is(nullValue()));
			verifyZeroInteractions(object, result, ssc, listener);
		}
	}
}
