package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.HttpRequestInterceptorProxy;
import rocks.inspectit.agent.java.util.ClassReference;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link HttpClientBuilderHook} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class HttpClientBuilderHookTest extends TestBase {

	@InjectMocks
	HttpClientBuilderHook hook;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	HttpClientBuilder object;

	@Mock
	Logger log;

	@Mock
	IRuntimeLinker runtimeLinker;

	/**
	 * Tests the
	 * {@link HttpClientBuilderHook#beforeBody(long, Object, Object[], SpecialSensorConfig)} method.
	 */
	public static class BeforeBody extends HttpClientBuilderHookTest {

		private static final long METHOD_ID = 11L;

		@Mock
		HttpRequestInterceptor newInterceptor;

		private void injectClassReference() throws Exception {
			Field fieldICR;
			fieldICR = HttpClientBuilderHook.class.getDeclaredField("interceptorClassReference");
			fieldICR.setAccessible(true);
			fieldICR.set(hook, new ClassReference(HttpRequestInterceptor.class.getName(), getClass().getClassLoader()));
		}

		@Test
		public void theReturnedObjectMustBeANullInstanceIfTheNumberOfParametersIsNotTheExpected() throws Exception {
			injectClassReference();
			Object[] parameters = new Object[] { "xyz" };

			Object beforeBodyObject = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat("The returned object must be null", beforeBodyObject, is(nullValue()));
			verifyZeroInteractions(object, runtimeLinker, ssc);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void mustCallToInvokeMethodOfCacheWithTheProperParameters() throws Exception {
			injectClassReference();
			Object[] parameters = new Object[] {};
			when(runtimeLinker.createProxy(eq(HttpRequestInterceptorProxy.class), Mockito.<HttpRequestInterceptorProxy> any(), Mockito.<ClassLoader> any())).thenReturn(newInterceptor);

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verify(object).addInterceptorFirst((HttpRequestInterceptor) any());
			verify(runtimeLinker).createProxy((Class) any(), (HttpRequestInterceptorProxy) any(), (ClassLoader) any());
			verifyNoMoreInteractions(object, runtimeLinker);
			verifyZeroInteractions(ssc);
		}

		@Test
		public void mustNotCallToInvokeMethodParametersIsNull() throws Exception {
			injectClassReference();
			Object[] parameters = null;

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyZeroInteractions(object, runtimeLinker, ssc);
		}

		@Test
		public void mustNotCallToInvokeMethodIfThereIsTooManyParameters() throws Exception {
			injectClassReference();
			Object[] parameters = new String[] { null };

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyZeroInteractions(object, runtimeLinker, ssc);
		}

		@Test
		public void interceptorClassCannotBeLoaded() {
			Object[] parameters = new Object[] {};

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyZeroInteractions(object, runtimeLinker, ssc);
		}
	}

	/**
	 * Tests the
	 * {@link HttpClientBuilderHook#afterBody(long, Object, Object[], Object, SpecialSensorConfig)}
	 * method.
	 */
	public static class AfterBody extends HttpClientBuilderHookTest {

		@Test
		public void returnsANullValueAndHasZeroInteractionsWithAnyOfTheParamters() {
			long methodId = 11L;
			Object[] parameters = new Object[] {};
			Object result = mock(Object.class);

			Object afterBodyObject = hook.afterBody(methodId, object, parameters, result, ssc);

			assertThat("The returned object after body must be null.", afterBodyObject, is(nullValue()));
			verifyZeroInteractions(object, result, ssc);
		}
	}

	interface HttpRequestInterceptor {
		Object addInterceptorFirst(HttpRequestInterceptor object);
	}

	interface HttpClientBuilder {
		HttpClientBuilder addInterceptorFirst(HttpRequestInterceptor object);
	}
}
