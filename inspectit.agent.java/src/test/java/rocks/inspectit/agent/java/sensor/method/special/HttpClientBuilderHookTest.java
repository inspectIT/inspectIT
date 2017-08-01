package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.HttpRequestInterceptorProxy;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Isabel Vico Peinado
 *
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class HttpClientBuilderHookTest extends TestBase {
	@InjectMocks
	HttpClientBuilderHook hook;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	ReflectionCache cache;

	@Mock
	Object object;

	@Mock
	Logger log;

	public static class BeforeBody extends HttpClientBuilderHookTest {

		private static final long METHOD_ID = 11L;

		@Mock
		IRuntimeLinker runtimeLinker;

		@Mock
		HttpRequestInterceptor newInterceptor;

		@BeforeMethod
		private void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException {
			Field fieldRuntimeLinker;
			fieldRuntimeLinker = HttpClientBuilderHook.class.getDeclaredField("runtimeLinker");
			fieldRuntimeLinker.setAccessible(true);
			fieldRuntimeLinker.set(hook, runtimeLinker);

			Field fieldCache;
			fieldCache = HttpClientBuilderHook.class.getDeclaredField("cache");
			fieldCache.setAccessible(true);
			fieldCache.set(hook, cache);
		}

		@Test
		public void theReturnedObjectMustBeANullInstanceIfTheNumberOfParametersIsNotTheExpected() {
			Object[] parameters = new Object[] { "blah" };

			Object beforeBodyObject = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat("The returned object must be null", beforeBodyObject, is(nullValue()));
		}

		@Test
		public void mustCallToInvokeMethodOfCacheWithTheProperParameters() throws ClassNotFoundException {
			Object[] parameters = new Object[] {};
			when(runtimeLinker.createProxy(eq(HttpRequestInterceptorProxy.class), Mockito.<HttpRequestInterceptorProxy> any(), Mockito.<ClassLoader> any())).thenReturn(newInterceptor);
			hook = spy(hook);
			doReturn(Class.class).when(hook).getClass(object);

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verify(cache, times(1)).invokeMethod((Class<?>) Mockito.<Object> any(), Mockito.<String> any(), new Class<?>[] { (Class<?>) Mockito.<Object> any() }, Mockito.<Object> any(),
					new Object[] { Mockito.<Object> any() },
					Mockito.<Object> any());
		}

		@Test
		public void mustThrowAnExceptionWhenGetClassThrowsIt() throws ClassNotFoundException {
			Object[] parameters = new Object[] {};
			when(runtimeLinker.createProxy(eq(HttpRequestInterceptorProxy.class), Mockito.<HttpRequestInterceptorProxy> any(), Mockito.<ClassLoader> any())).thenReturn(newInterceptor);
			hook = spy(hook);
			doThrow(new ClassNotFoundException()).when(hook).getClass(object);

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verify(log, times(1)).error(Mockito.anyString(), Mockito.<ClassNotFoundException> any());
		}

		@Test
		public void mustNotCallToInvokeMethodParametersIsNull() {
			Object[] parameters = null;

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyZeroInteractions(cache);
		}

		@Test
		public void mustNotCallToInvokeMethodIfThereIsTooManyParameters() {
			Object[] parameters = new String[] { null };

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyZeroInteractions(cache);
		}

		interface HttpContext {
			Object getAttribute(String id);

			void setAttribute(String id, Object obj);

			Object removeAttribute(String id);
		};

		interface HttpRequestInterceptor {
			void process(HttpRequest request, HttpContext context);
		};

		interface HttpRequest {
			RequestLine getRequestLine();

			void setHeader(String key, String value);

			boolean containsHeader(String key);
		};

		interface RequestLine {
			String getUri();

			String getMethod();
		};
	}

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

}
