package rocks.inspectit.agent.java.tracing.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class CoreServiceReporterTest extends TestBase {

	@InjectMocks
	CoreServiceReporter reporter;

	@Mock
	IPlatformManager platformManager;

	@Mock
	ICoreService coreService;

	public static class Report extends CoreServiceReporterTest {

		@Test
		public void happyPath() throws Exception {
			long spanId = 13L;
			long platformId = 17L;
			SpanImpl span = mock(SpanImpl.class);
			SpanContextImpl context = mock(SpanContextImpl.class);
			when(context.getId()).thenReturn(spanId);
			when(span.context()).thenReturn(context);
			when(platformManager.getPlatformId()).thenReturn(platformId);

			reporter.report(span);

			ArgumentCaptor<AbstractSpan> captor = ArgumentCaptor.forClass(AbstractSpan.class);
			verify(coreService).addDefaultData(captor.capture());
			assertThat(captor.getValue().getPlatformIdent(), is(platformId));
			assertThat(captor.getValue().getSensorTypeIdent(), is(0L));
			assertThat(captor.getValue().getMethodIdent(), is(0L));
		}
	}
}
