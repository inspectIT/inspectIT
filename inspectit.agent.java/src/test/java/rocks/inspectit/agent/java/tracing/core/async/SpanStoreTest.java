package rocks.inspectit.agent.java.tracing.core.async;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.TagsProvidingAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
/**
 *
 * Tests for the {@link SpanStore} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class SpanStoreTest extends TestBase {

	@InjectMocks
	SpanStore spanStore;

	@Mock
	SpanImpl span;

	/**
	 * Tests the {@link SpanStore#startSpan()} method.
	 *
	 */
	public static class StartSpan extends SpanStoreTest {

		@Test
		public void successful() {
			when(span.isStarted()).thenReturn(false);

			boolean result = spanStore.startSpan();

			assertThat(result, is(true));
			verify(span).start();
			verify(span).isStarted();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void alreadyStarted() {
			when(span.isStarted()).thenReturn(true);

			boolean result = spanStore.startSpan();

			assertThat(result, is(false));
			verify(span).isStarted();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void spanIsNull() {
			spanStore.storeSpan(null);

			boolean result = spanStore.startSpan();

			assertThat(result, is(false));
			verifyZeroInteractions(span);
		}
	}

	/**
	 * Tests the {@link SpanStore#finishSpan()} method.
	 *
	 */
	public static class FinishSpan extends SpanStoreTest {

		@Mock
		TagsProvidingAdapter adapter;

		@Test
		public void successful() {
			when(span.isFinished()).thenReturn(false);

			boolean result = spanStore.finishSpan();

			assertThat(result, is(true));
			verify(span).finish();
			verify(span).isFinished();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void alreadyFinished() {
			when(span.isFinished()).thenReturn(true);

			boolean result = spanStore.finishSpan();

			assertThat(result, is(false));
			verify(span).isFinished();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void spanIsNull() {
			spanStore.storeSpan(null);

			boolean result = spanStore.finishSpan();

			assertThat(result, is(false));
			verifyZeroInteractions(span);
		}

		@Test
		public void tagsSuccessful() {
			when(adapter.getTags()).thenReturn(Collections.singletonMap("key", "value"));
			when(span.isFinished()).thenReturn(false);

			boolean result = spanStore.finishSpan(adapter);

			assertThat(result, is(true));
			verify(span).finish();
			verify(span).isFinished();
			verify(span).setTag("key", "value");
			verifyNoMoreInteractions(span);
		}

		@Test
		public void tagsNullSuccessful() {
			when(adapter.getTags()).thenReturn(null);
			when(span.isFinished()).thenReturn(false);

			boolean result = spanStore.finishSpan(adapter);

			assertThat(result, is(true));
			verify(span).finish();
			verify(span).isFinished();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void tagsAlreadyFinished() {
			when(adapter.getTags()).thenReturn(Collections.singletonMap("key", "value"));
			when(span.isFinished()).thenReturn(true);

			boolean result = spanStore.finishSpan(adapter);

			assertThat(result, is(false));
			verify(span).isFinished();
			verifyNoMoreInteractions(span);
		}

		@Test
		public void tagsSpanIsNull() {
			when(adapter.getTags()).thenReturn(Collections.singletonMap("key", "value"));
			spanStore.storeSpan(null);

			boolean result = spanStore.finishSpan(adapter);

			assertThat(result, is(false));
			verifyZeroInteractions(span);
		}
	}
}
