package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.tracing.core.adapter.TagsProvidingAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * @author Isabel Vico Peinado
 *
 */
public class FutureCallbackProxyTest extends TestBase {

	FutureCallbackProxy proxy;

	@Mock
	SpanStore spanStore;

	@BeforeMethod
	public void init() {
		proxy = new FutureCallbackProxy(spanStore);
	}

	public static class GetProxyConstructorArguments extends FutureCallbackProxyTest {

		@Test
		public void returnsAnEmptyCollection() {
			Object[] args = proxy.getProxyConstructorArguments();

			assertThat("Number of args returned by the constructor must be zero.", args, is(new Object[] {}));
		}
	}

	public static class Completed extends FutureCallbackProxyTest {
		@Test
		public void finishSpanMustBeCalledWhenIsCalledAndTheInstanceOfTheProviderMustBeCorrect() {
			Object response = new Object();

			proxy.completed(response);

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStore).finishSpan(captor.capture());
			TagsProvidingAdapter adapter = captor.getValue();
			assertThat("Adapter must be an instance of HttpResponseAdapter.", adapter, is(instanceOf(HttpResponseAdapter.class)));
		}
	}

	public static class Failed extends FutureCallbackProxyTest {
		@Test
		public void finishSpanMustBeCalledWhenIsCalledAndTheInstanceOfTheProviderMustBeCorrect() throws Exception {
			Exception ex = new Exception();

			proxy.failed(ex);

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStore).finishSpan(captor.capture());
			TagsProvidingAdapter adapter = captor.getValue();
			assertThat("Adapter must be an instance of ThrowabelAwareResponseAdapter", adapter, is(instanceOf(ThrowableAwareResponseAdapter.class)));
			assertThat("Adapter must set a tag with the error value as true.", adapter.getTags(), hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat("Adapter must set a tag with the throwable as an exception.", adapter.getTags(), hasEntry(ExtraTags.THROWABLE_TYPE, ex.getClass().getSimpleName()));
		}
	}
}
