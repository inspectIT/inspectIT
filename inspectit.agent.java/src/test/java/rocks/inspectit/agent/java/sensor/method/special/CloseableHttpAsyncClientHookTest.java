package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.theInstance;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.FutureCallbackProxy;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link CloseableHttpAsyncClientHook} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class CloseableHttpAsyncClientHookTest extends TestBase {

	@InjectMocks
	CloseableHttpAsyncClientHook hook;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	Object object;

	@Mock
	IRuntimeLinker runtimeLinker;

	/**
	 * Tests the
	 * {@link CloseableHttpAsyncClientHook#beforeBody(long, Object, Object[], SpecialSensorConfig)}
	 * method.
	 */
	public static class BeforeBody extends CloseableHttpAsyncClientHookTest {

		static final long METHOD_ID = 11L;

		@Mock
		HttpContext httpContext;

		@Mock
		FutureCallback futureCallback;

		@Test
		public void theReturnedObjectMustBeANullInstance() {
			Object[] parameters = new Object[] { new Object(), new Object(), new Object(), new Object() };

			Object beforeBodyObject = hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat("The returned object must be null", beforeBodyObject, is(nullValue()));
			verifyZeroInteractions(ssc, object, runtimeLinker, httpContext, futureCallback);
		}

		@Test
		public void mustNotHaveAProxyInThirdParametersIfThereIsNoParameters() {
			Object[] parameters = new Object[] {};

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyNoMoreInteractions(runtimeLinker);
			verifyZeroInteractions(ssc, object, httpContext, futureCallback);
		}

		@Test
		public void mustNotHaveAProxyInThirdParametersIfParametersIsNull() {
			Object[] parameters = null;

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyNoMoreInteractions(runtimeLinker);
			verifyZeroInteractions(ssc, object, httpContext, futureCallback);
		}

		@Test
		public void mustNotSetProxyIfThereIsTooManyParameters() {
			Object[] parameters = new String[] { null, null, null, null, null };

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			assertThat("Third parameter (proxy) must not be set.", parameters[3], is(nullValue()));
			verifyNoMoreInteractions(runtimeLinker);
			verifyZeroInteractions(ssc, object, httpContext, futureCallback);
		}

		@Test
		public void mustNotSetProxyIfThereIsTooFewParameters() {
			Object[] parameters = new String[] { null, null, null };

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verifyNoMoreInteractions(runtimeLinker);
			verifyZeroInteractions(ssc, object, httpContext, futureCallback);
		}

		@Test
		public void mustSetTheProperValueForTheProxy() {
			SpanStore spanStore = new SpanStore();
			Object[] parameters = new Object[] { new Object(), new Object(), httpContext, mock(FutureCallback.class) };
			when(runtimeLinker.createProxy(eq(FutureCallbackProxy.class), Mockito.<FutureCallbackProxy> any(), Mockito.<ClassLoader> any())).thenReturn(futureCallback);
			when(httpContext.getAttribute(SpanStoreAdapter.Constants.ID)).thenReturn(spanStore);
			ArgumentCaptor<FutureCallbackProxy> proxyCaptor = ArgumentCaptor.forClass(FutureCallbackProxy.class);

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verify(runtimeLinker).createProxy(eq(FutureCallbackProxy.class), proxyCaptor.capture(), Mockito.<ClassLoader> any());
			verify(httpContext).getAttribute(SpanStoreAdapter.Constants.ID);
			verifyNoMoreInteractions(runtimeLinker, httpContext);
			verifyZeroInteractions(ssc, object, futureCallback);
			assertThat(parameters[3], is(theInstance((Object) futureCallback)));
		}

		@Test
		public void mustNotSetTheProxyIfTheSpanStoreIsNotSet() {
			Object[] parameters = new Object[] { new Object(), new Object(), httpContext, new Object() };

			hook.beforeBody(METHOD_ID, object, parameters, ssc);

			verify(httpContext).getAttribute(SpanStoreAdapter.Constants.ID);
			verifyNoMoreInteractions(httpContext);
			verifyZeroInteractions(ssc, object, runtimeLinker, futureCallback);
		}

		interface FutureCallback {
			void completed(Object response);

			void failed(Object exception);

			void cancelled();
		}

		interface HttpContext {
			Object getAttribute(String id);

			void setAttribute(String id, Object obj);

			Object removeAttribute(String id);
		}
	}

	/**
	 * Tests the
	 * {@link CloseableHttpAsyncClientHook#afterBody(long, Object, Object[], Object, SpecialSensorConfig)}
	 * method.
	 */
	public static class AfterBody extends CloseableHttpAsyncClientHookTest {

		@Test
		public void returnsANullValueAndHasZeroInteractionsWithAnyOfTheParamters() {
			long methodId = 11L;
			Object[] parameters = new Object[0];
			Object result = mock(Object.class);

			Object afterBodyObject = hook.afterBody(methodId, object, parameters, result, ssc);

			assertThat("The returned object after body must be null.", afterBodyObject, is(nullValue()));
			verifyZeroInteractions(object, result, ssc, runtimeLinker);
		}
	}
}
