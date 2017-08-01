package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.eum.reflection.CachedMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.TagsProvidingAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.FutureCallbackProxy.WFutureCallback;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link FutureCallbackProxy} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class FutureCallbackProxyTest extends TestBase {

	@InjectMocks
	FutureCallbackProxy proxy;

	@Mock
	SpanStoreAdapter spanStoreAdapter;

	@Mock
	SpanStore spanStore;

	@Mock
	FutureCallback originalCallback;

	@BeforeMethod
	public void init() throws Exception {
		proxy = new FutureCallbackProxy(originalCallback, spanStoreAdapter);

		Field fieldMCompleted = WFutureCallback.class.getDeclaredField("completed");
		fieldMCompleted.setAccessible(true);
		fieldMCompleted.set(proxy, new CachedMethod<Void>(FutureCallback.class.getName(), "completed", Object.class));

		Field fieldMFailed = WFutureCallback.class.getDeclaredField("failed");
		fieldMFailed.setAccessible(true);
		fieldMFailed.set(proxy, new CachedMethod<Void>(FutureCallback.class.getName(), "failed", Exception.class));

		Field fieldMCancelled = WFutureCallback.class.getDeclaredField("cancelled");
		fieldMCancelled.setAccessible(true);
		fieldMCancelled.set(proxy, new CachedMethod<Void>(FutureCallback.class.getName(), "cancelled"));
	}

	public void removeOriginalCallback() throws Exception {
		Field fieldCallback = FutureCallbackProxy.class.getDeclaredField("originalCallback");
		fieldCallback.setAccessible(true);
		fieldCallback.set(proxy, null);
	}

	/**
	 * Tests the {@link FutureCallbackProxy#getProxyConstructorArguments()} method.
	 */
	public static class GetProxyConstructorArguments extends FutureCallbackProxyTest {

		@Test
		public void returnsAnEmptyCollection() {
			Object[] args = proxy.getProxyConstructorArguments();

			assertThat("Number of args returned by the constructor must be zero.", args, is(new Object[] {}));
		}
	}

	/**
	 * Tests the {@link FutureCallbackProxy#completed(Object)} method.
	 */
	public static class Completed extends FutureCallbackProxyTest {

		@Test
		public void successful() {
			Object response = new Object();
			when(spanStoreAdapter.getSpanStore()).thenReturn(spanStore);

			proxy.completed(response);

			verify(spanStoreAdapter).getSpanStore();
			verify(spanStore).finishSpan((HttpResponseAdapter) any());
			verify(originalCallback).completed(response);
			verifyNoMoreInteractions(spanStore, spanStoreAdapter, originalCallback);
		}

		@Test
		public void withoutSpanStore() {
			Object response = new Object();

			proxy.completed(response);

			verify(spanStoreAdapter).getSpanStore();
			verify(originalCallback).completed(response);
			verifyNoMoreInteractions(spanStoreAdapter, originalCallback);
			verifyZeroInteractions(spanStore);
		}

		@Test
		public void withoutSpanStoreAndCallback() throws Exception {
			Object response = new Object();
			removeOriginalCallback();

			proxy.completed(response);

			verify(spanStoreAdapter).getSpanStore();
			verifyNoMoreInteractions(spanStoreAdapter);
			verifyZeroInteractions(spanStore, originalCallback);
		}
	}

	/**
	 * Tests the {@link FutureCallbackProxy#failed(Object)} method.
	 */
	public static class Failed extends FutureCallbackProxyTest {

		@Test
		public void successful() throws Exception {
			Exception exception = new Exception();
			when(spanStoreAdapter.getSpanStore()).thenReturn(spanStore);

			proxy.failed(exception);

			verify(spanStoreAdapter).getSpanStore();
			verify(spanStore).finishSpan((TagsProvidingAdapter) any());
			verify(originalCallback).failed(exception);
			verifyNoMoreInteractions(spanStore, spanStoreAdapter, originalCallback);
		}

		@Test
		public void withoutSpanStore() throws Exception {
			Exception exception = new Exception();

			proxy.failed(exception);

			verify(spanStoreAdapter).getSpanStore();
			verify(originalCallback).failed(exception);
			verifyNoMoreInteractions(spanStoreAdapter, originalCallback);
			verifyZeroInteractions(spanStore);
		}

		@Test
		public void withoutSpanStoreAndCallback() throws Exception {
			Exception exception = new Exception();
			removeOriginalCallback();

			proxy.failed(exception);

			verify(spanStoreAdapter).getSpanStore();
			verifyNoMoreInteractions(spanStoreAdapter);
			verifyZeroInteractions(spanStore, originalCallback);
		}
	}

	/**
	 * Tests the {@link FutureCallbackProxy#cancelled()} method.
	 */
	public static class Cancelled extends FutureCallbackProxyTest {

		@Test
		public void successful() throws Exception {
			when(spanStoreAdapter.getSpanStore()).thenReturn(spanStore);

			proxy.cancelled();

			ArgumentCaptor<TagsProvidingAdapter> captor = ArgumentCaptor.forClass(TagsProvidingAdapter.class);
			verify(spanStoreAdapter).getSpanStore();
			verify(spanStore).finishSpan(captor.capture());
			verify(originalCallback).cancelled();
			verifyNoMoreInteractions(spanStore, spanStoreAdapter, originalCallback);
			assertThat(captor.getValue().getTags(), hasKey(SpanStoreAdapter.Constants.CANCEL));
		}

		@Test
		public void withoutSpanStore() throws Exception {
			proxy.cancelled();

			verify(spanStoreAdapter).getSpanStore();
			verify(originalCallback).cancelled();
			verifyNoMoreInteractions(spanStoreAdapter, originalCallback);
			verifyZeroInteractions(spanStore);
		}

		@Test
		public void withoutSpanStoreAndCallback() throws Exception {
			removeOriginalCallback();

			proxy.cancelled();

			verify(spanStoreAdapter).getSpanStore();
			verifyNoMoreInteractions(spanStoreAdapter);
			verifyZeroInteractions(spanStore, originalCallback);
		}
	}

	public interface FutureCallback {
		void completed(Object object);

		void failed(Exception object);

		void cancelled();
	}
}
