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
 * Tests the {@link NHttpClientConnectionManagerHook} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class NHttpClientConnectionManagerHookTest extends TestBase {

	@InjectMocks
	NHttpClientConnectionManagerHook hook;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	Object object;

	/**
	 * Tests the
	 * {@link NHttpClientConnectionManagerHook#beforeBody(long, long, Object, Object[], RegisteredSensorConfig)}
	 * method.
	 */
	public static class BeforeBody extends NHttpClientConnectionManagerHookTest {

		@Test
		public void successful() {
			SpanStore spanStore = mock(SpanStore.class);
			HttpContext context = mock(HttpContext.class);
			when(context.getAttribute(SpanStoreAdapter.Constants.ID)).thenReturn(spanStore);
			Object[] parameters = new Object[] { new Object(), new Object(), context };

			hook.beforeBody(5L, 10L, object, parameters, rsc);

			verify(context).getAttribute(SpanStoreAdapter.Constants.ID);
			verify(spanStore).startSpan();
			verifyNoMoreInteractions(spanStore, context);
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void noParameters() {
			Object[] parameters = new Object[0];

			hook.beforeBody(5L, 10L, object, parameters, rsc);

			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void invalidParameterLength() {
			Object[] parameters = new Object[4];

			hook.beforeBody(5L, 10L, object, parameters, rsc);

			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void noSpanStore() {
			HttpContext context = mock(HttpContext.class);
			Object[] parameters = new Object[] { new Object(), new Object(), context };

			hook.beforeBody(5L, 10L, object, parameters, rsc);

			verify(context).getAttribute(SpanStoreAdapter.Constants.ID);
			verifyNoMoreInteractions(context);
			verifyZeroInteractions(object, rsc);
		}
	}

	/**
	 * Tests the
	 * {@link NHttpClientConnectionManagerHook#firstAfterBody(long, long, Object, Object[], Object, boolean, rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig)}
	 * method.
	 */
	public static class FirstAfterBody extends NHttpClientConnectionManagerHookTest {

		@Test
		public void successful() {
			Object result = mock(Object.class);
			Object[] parameters = new Object[0];

			hook.firstAfterBody(5L, 10L, object, parameters, result, false, rsc);

			verifyZeroInteractions(object, rsc, result);
		}
	}

	/**
	 * Tests the
	 * {@link NHttpClientConnectionManagerHook#secondAfterBody(rocks.inspectit.agent.java.core.ICoreService, long, long, Object, Object[], Object, boolean, RegisteredSensorConfig)}
	 * method.
	 */
	public static class SecondAfterBody extends NHttpClientConnectionManagerHookTest {

		@Test
		public void successful() {
			ICoreService coreService = mock(ICoreService.class);
			Object result = mock(Object.class);
			Object[] parameters = new Object[0];

			hook.secondAfterBody(coreService, 5L, 10L, object, parameters, result, false, rsc);

			verifyZeroInteractions(coreService, object, rsc, result);
		}
	}

	/**
	 * Mock template for {@link org.apache.http.protocol.HttpContext}.
	 */
	interface HttpContext {
		Object getAttribute(String id);
	}
}
