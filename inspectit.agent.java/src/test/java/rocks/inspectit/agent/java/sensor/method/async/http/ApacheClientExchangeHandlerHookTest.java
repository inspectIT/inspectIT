package rocks.inspectit.agent.java.sensor.method.async.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ApacheClientExchangeHandlerHook} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class ApacheClientExchangeHandlerHookTest extends TestBase {

	@InjectMocks
	ApacheClientExchangeHandlerHook hook;

	@Mock
	RegisteredSensorConfig rsc;

	AbstractClientExchangeHandler handler = new AbstractClientExchangeHandler();

	/**
	 * Tests the
	 * {@link ApacheClientExchangeHandlerHook#beforeBody(long, long, Object, Object[], RegisteredSensorConfig)}
	 * method.
	 */
	public static class BeforeBody extends ApacheClientExchangeHandlerHookTest {

		@Test
		public void successful() {
			SpanStore spanStore = mock(SpanStore.class);
			HttpContext context = mock(HttpContext.class);
			when(context.getAttribute(SpanStoreAdapter.Constants.ID)).thenReturn(spanStore);
			handler.localContext = context;

			hook.beforeBody(5L, 10L, handler, null, rsc);

			verify(context).getAttribute(SpanStoreAdapter.Constants.ID);
			verify(spanStore).startSpan();
			verifyNoMoreInteractions(spanStore, context);
			verifyZeroInteractions(rsc);
		}

		@Test
		public void noSpanStore() {
			HttpContext context = mock(HttpContext.class);
			handler.localContext = context;

			hook.beforeBody(5L, 10L, handler, null, rsc);

			verify(context).getAttribute(SpanStoreAdapter.Constants.ID);
			verifyNoMoreInteractions(context);
			verifyZeroInteractions(rsc);
		}

		@Test
		public void missingHttpContext() {
			hook.beforeBody(5L, 10L, handler, null, rsc);

			verifyZeroInteractions(rsc);
		}
	}

	/**
	 * Tests the
	 * {@link ApacheClientExchangeHandlerHook#firstAfterBody(long, long, Object, Object[], Object, boolean, rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig)}
	 * method.
	 */
	public static class FirstAfterBody extends ApacheClientExchangeHandlerHookTest {

		@Test
		public void successful() {
			Object result = mock(Object.class);

			hook.firstAfterBody(5L, 10L, handler, null, result, false, rsc);

			verifyZeroInteractions(rsc, result);
		}
	}

	/**
	 * Tests the
	 * {@link ApacheClientExchangeHandlerHook#secondAfterBody(rocks.inspectit.agent.java.core.ICoreService, long, long, Object, Object[], Object, boolean, RegisteredSensorConfig)}
	 * method.
	 */
	public static class SecondAfterBody extends ApacheClientExchangeHandlerHookTest {

		@Test
		public void successful() {
			ICoreService coreService = mock(ICoreService.class);
			Object result = mock(Object.class);

			hook.secondAfterBody(coreService, 5L, 10L, handler, null, result, false, rsc);

			verifyZeroInteractions(coreService, rsc, result);
		}
	}

	/**
	 * Mock template for {@link org.apache.http.protocol.HttpContext}.
	 */
	interface HttpContext {
		Object getAttribute(String id);
	}

	/**
	 * Dummy class representing
	 * {@link org.apache.http.impl.nio.client.AbstractClientExchangeHandler}.
	 */
	@SuppressWarnings("unused")
	class AbstractClientExchangeHandler {
		private HttpContext localContext;
	}
}
