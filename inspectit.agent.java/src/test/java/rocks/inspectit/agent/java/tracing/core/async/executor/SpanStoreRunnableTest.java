package rocks.inspectit.agent.java.tracing.core.async.executor;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 * Tests for the {@link SpanStoreRunnable} class.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpanStoreRunnableTest extends TestBase {

	@InjectMocks
	SpanStoreRunnable spanStoreRunnable;

	@Mock
	Runnable runnable;

	@Mock
	SpanImpl spanImpl;

	public static class Run extends SpanStoreRunnableTest {

		@Test
		public void successful() {
			spanStoreRunnable.storeSpan(spanImpl);

			spanStoreRunnable.run();

			verify(runnable).run();
			verify(spanImpl).start();
			verify(spanImpl).finish();
			verify(spanImpl).setTag(ExtraTags.RUNNABLE_TYPE, runnable.getClass().getName());
			verify(spanImpl, atLeastOnce()).isStarted();
			verify(spanImpl, atLeastOnce()).isFinished();
			verifyNoMoreInteractions(spanImpl, runnable);
			SpanImpl span = spanStoreRunnable.getSpan();
			assertThat(span, is(spanImpl));
		}

	}

	public static class Equals extends SpanStoreRunnableTest {

		@Test
		public void successful() {
			Object o = new Object();

			boolean result = spanStoreRunnable.equals(o);

			assertThat(result, is(runnable.equals(o)));
		}

	}

	public static class HashCode extends SpanStoreRunnableTest {

		@Test
		public void successful() {
			int result = spanStoreRunnable.hashCode();

			assertThat(result, is(runnable.hashCode()));
		}

	}

}
