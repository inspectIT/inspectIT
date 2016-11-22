package rocks.inspectit.server.processor.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.persistence.EntityManager;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Jonas Kunz
 *
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class EUMCorrelationCmrProcessorTest extends TestBase {

	SpanIdent frontEndIdent = new SpanIdent(10, 11, 10);
	SpanIdent backEndUncorrelatedIdent = new SpanIdent(11, 11, 11);
	SpanIdent backEndCorrelatedIdent = new SpanIdent(11, 11, 10);

	@Mock
	BufferSpanDaoImpl spanDao;

	@Mock
	EUMSpan frontEndSpan;

	@Mock
	PageLoadRequest spanDetails;

	@Mock
	AbstractSpan backEndSpan;

	@InjectMocks
	EUMCorrelationCmrProcessor processor;

	@BeforeMethod
	public void initMocks() {
		when(frontEndSpan.getDetails()).thenReturn(spanDetails);
		when(frontEndSpan.getSpanIdent()).thenReturn(frontEndIdent);

		when(spanDao.get(Matchers.eq(backEndUncorrelatedIdent))).thenReturn(backEndSpan);

	}

	public static class ProcessData extends EUMCorrelationCmrProcessorTest {

		@Test
		public void testPageLoadCorrelation() throws InterruptedException {
			processor.process(Collections.singleton(frontEndSpan), mock(EntityManager.class));
			Thread.sleep(50);

			verify(backEndSpan, times(1)).setSpanIdent(eq(backEndCorrelatedIdent));
		}
	}
}
