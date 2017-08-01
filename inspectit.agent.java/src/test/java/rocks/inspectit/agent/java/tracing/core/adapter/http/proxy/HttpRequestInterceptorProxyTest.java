package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link HttpRequestInterceptorProxy} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class HttpRequestInterceptorProxyTest extends TestBase {

	@InjectMocks
	HttpRequestInterceptorProxy proxy;

	@Mock
	SpanStore spanStore;

	/**
	 * Tests the {@link HttpRequestInterceptorProxy#getProxyConstructorArguments()} method.
	 */
	public static class GetProxyConstructorArguments extends HttpRequestInterceptorProxyTest {

		@Test
		public void returnsAnEmptyCollection() {
			Object[] args = proxy.getProxyConstructorArguments();

			assertThat("Number of args returned by the constructor must be zero.", args, is(new Object[] {}));
		}
	}

	/**
	 * Tests the {@link HttpRequestInterceptorProxy#process(Object, Object)} method.
	 */
	public static class Process extends HttpRequestInterceptorProxyTest {

		@Mock
		HttpContext context;

		@Test
		public void finishSpanMustBeCalledWhenIsCalledAndTheInstanceOfTheProviderMustBeCorrect() {
			Object request = new Object();
			when(context.getAttribute(SpanStoreAdapter.Constants.ID)).thenReturn(spanStore);

			proxy.process(request, context);

			verify(spanStore).startSpan();
			verifyNoMoreInteractions(spanStore);
		}

		@Test
		public void withoutSpanStore() {
			Object request = new Object();

			proxy.process(request, context);

			verifyZeroInteractions(spanStore);
		}

		interface HttpContext {
			Object getAttribute(String id);
		};
	}
}
