package rocks.inspectit.server.processor.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.persistence.EntityManager;

import org.apache.commons.lang.mutable.MutableInt;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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

	@Mock
	ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);

	@InjectMocks
	EUMCorrelationCmrProcessor processor;

	@BeforeMethod
	public void initMocks() {
		doAnswer(new Answer<ScheduledFuture<Void>>() {
			@Override
			public ScheduledFuture<Void> answer(InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			}
		}).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any());
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

			verify(backEndSpan, times(1)).setParentSpanId(eq(frontEndIdent.getId()));
		}

		@Test
		public void testEUMSpanFirst() throws InterruptedException {
			// span initially not available
			MutableInt counter = new MutableInt(0);
			doAnswer(new Answer<AbstractSpan>() {
				@Override
				public AbstractSpan answer(InvocationOnMock invocation) throws Throwable {
					counter.increment();
					if (counter.longValue() >= 5) { // make it available on the fifth attempt
						return backEndSpan;
					} else {
						return null;
					}
				}
			}).when(spanDao).get(Matchers.eq(backEndIdent));

			processor.process(Collections.singleton(frontEndSpan), mock(EntityManager.class));

			verify(backEndSpan, times(1)).setParentSpanId(eq(frontEndIdent.getId()));
		}

		@Test
		public void testNoAjaxCorrelation() throws InterruptedException {
			when(frontEndSpan.getDetails()).thenReturn(mock(AjaxRequest.class));

			processor.process(Collections.singleton(frontEndSpan), mock(EntityManager.class));

			verify(spanDao, never()).get(any());
		}
	}
}
