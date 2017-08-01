package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Isabel Vico Peinado
 *
 */
public class HttpRequestInterceptorProxyTest extends TestBase {

	@InjectMocks
	HttpRequestInterceptorProxy proxy;

	@Mock
	SpanStore spanStore;

	public static class GetProxyConstructorArguments extends HttpRequestInterceptorProxyTest {

		@Test
		public void returnsAnEmptyCollection() {
			Object[] args = proxy.getProxyConstructorArguments();

			assertThat("Number of args returned by the constructor must be zero.", args, is(new Object[] {}));
		}
	}

	public static class Process extends HttpRequestInterceptorProxyTest {
		@Mock
		HttpContext context;

		@Test
		public void finishSpanMustBeCalledWhenIsCalledAndTheInstanceOfTheProviderMustBeCorrect() {
			Object request = new Object();
			when(context.getAttribute("spanStore")).thenReturn(spanStore);

			proxy.process(request, context);

			verify(spanStore).startSpan();
		}

		interface HttpContext {
			Object getAttribute(String id);

			void setAttribute(String id, Object obj);

			Object removeAttribute(String id);
		};
	}
}
