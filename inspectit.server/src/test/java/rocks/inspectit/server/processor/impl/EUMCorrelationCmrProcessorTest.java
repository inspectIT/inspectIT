package rocks.inspectit.server.processor.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Jonas Kunz
 *
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class EUMCorrelationCmrProcessorTest extends TestBase {

	SpanIdent frontEndIdent = new SpanIdent(10, 11);
	SpanIdent backEndIdent = new SpanIdent(11, 11);

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

		when(backEndSpan.getPropagationType()).thenReturn(PropagationType.HTTP);
		when(backEndSpan.isRoot()).thenReturn(true);
		when(backEndSpan.getSpanIdent()).thenReturn(backEndIdent);

		when(frontEndSpan.getPropagationType()).thenReturn(PropagationType.HTTP);
		when(frontEndSpan.isRoot()).thenReturn(true);
		PageLoadRequest details = mock(PageLoadRequest.class);
		when(frontEndSpan.getDetails()).thenReturn(details);

		when(frontEndSpan.getDetails()).thenReturn(spanDetails);
		when(frontEndSpan.getSpanIdent()).thenReturn(frontEndIdent);


	}

	public static class Process extends EUMCorrelationCmrProcessorTest {

		@Test
		public void testBackEndSpanFirst() throws InterruptedException {
			when(spanDao.get(Matchers.eq(backEndIdent))).thenReturn(backEndSpan);

			processor.process(Collections.singleton(frontEndSpan), mock(EntityManager.class));
			Thread.sleep(50);

			verify(backEndSpan, times(1)).setParentSpanId(eq(frontEndIdent.getId()));
		}

		@Test
		public void testEUMSpanFirst() throws InterruptedException {
			when(spanDao.getSpans(Matchers.eq(frontEndIdent.getTraceId()))).thenReturn(Collections.singleton(frontEndSpan));

			processor.process(Collections.singleton(backEndSpan), mock(EntityManager.class));
			Thread.sleep(50);

			verify(backEndSpan, times(1)).setParentSpanId(eq(frontEndIdent.getId()));
		}

		@Test
		public void testNoAjaxCorrelation() throws InterruptedException {
			when(frontEndSpan.getDetails()).thenReturn(mock(AjaxRequest.class));

			processor.process(Collections.singleton(frontEndSpan), mock(EntityManager.class));
			Thread.sleep(50);

			verify(spanDao, never()).get(any());
		}
	}
}
