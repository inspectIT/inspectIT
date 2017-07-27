package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

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
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JettyEventListenerProxyTest extends TestBase {

	JettyEventListenerProxy proxy;

	@Mock
	SpanStore spanStore;

	@BeforeMethod
	public void init() {
		// can not use @InjectMocks as span store gets injected to listener place
		proxy = new JettyEventListenerProxy(null, spanStore);
	}

	public static class GetProxyConstructorArguments extends JettyEventListenerProxyTest {

		@Test
		public void listenerNull() {
			Object[] args = proxy.getProxyConstructorArguments();

			assertThat(args, is(new Object[] { null, Boolean.FALSE }));
		}

		@Test
		public void listenerProvided() {
			Object originalListener = new Object();
			proxy = new JettyEventListenerProxy(originalListener, spanStore);

			Object[] args = proxy.getProxyConstructorArguments();

			assertThat(args, is(new Object[] { originalListener, Boolean.TRUE }));
		}
	}

	public static class OnRequestCommitted extends JettyEventListenerProxyTest {

		@Test
		public void happyPath() throws IOException {
			proxy.onRequestCommitted();

			verify(spanStore).startSpan();
		}
	}

	public static class OnResponseStatus extends JettyEventListenerProxyTest {

		@Test
		public void happyPath() throws IOException {
			int status = 200;

			proxy.onResponseStatus("version", status, "reason");

			verifyZeroInteractions(spanStore);
			assertThat(proxy.getStatus(), is(status));
		}
	}

	public static class OnResponseComplete extends JettyEventListenerProxyTest {

		@Test
		public void happyPath() throws IOException {
			int status = 200;
			proxy.onResponseStatus("version", status, "reason");

			// can not be called before status reporting
			proxy.onResponseComplete();

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStore).finishSpan(captor.capture());
			TagsProvidingAdapter adapter = captor.getValue();
			assertThat(adapter, is(instanceOf(HttpResponseAdapter.class)));
			assertThat(adapter.getTags(), hasEntry(Tags.HTTP_STATUS.getKey(), String.valueOf(status)));
		}
	}

	public static class OnConnectionFailed extends JettyEventListenerProxyTest {

		@Test
		public void happyPath() throws IOException {
			IOException ex = new IOException();

			proxy.onConnectionFailed(ex);

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStore).finishSpan(captor.capture());
			TagsProvidingAdapter adapter = captor.getValue();
			assertThat(adapter, is(instanceOf(ThrowableAwareResponseAdapter.class)));
			assertThat(adapter.getTags(), hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat(adapter.getTags(), hasEntry(ExtraTags.THROWABLE_TYPE, ex.getClass().getSimpleName()));
		}
	}

	public static class OnException extends JettyEventListenerProxyTest {

		@Test
		public void happyPath() throws IOException {
			IOException ex = new IOException();

			proxy.onConnectionFailed(ex);

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStore).finishSpan(captor.capture());
			TagsProvidingAdapter adapter = captor.getValue();
			assertThat(adapter, is(instanceOf(ThrowableAwareResponseAdapter.class)));
			assertThat(adapter.getTags(), hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat(adapter.getTags(), hasEntry(ExtraTags.THROWABLE_TYPE, ex.getClass().getSimpleName()));
		}
	}

}
