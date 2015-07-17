package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test of {@link TimerDataAggregator}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class TimerDataAggregatorTest extends AbstractTestNGLogSupport {

	/**
	 * {@link TimerDataAggregator} to test.
	 */
	private TimerDataAggregator aggregator;

	private StatelessSession session;

	/**
	 * Initialize.
	 */
	@BeforeMethod
	public void init() {
		SessionFactory factory = mock(SessionFactory.class);
		session = mock(StatelessSession.class);
		Transaction transaction = mock(Transaction.class);
		when(factory.openStatelessSession()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);

		aggregator = new TimerDataAggregator(factory);
		aggregator.aggregationPeriod = 5l;
		aggregator.cacheCleanSleepingPeriod = 10;
		aggregator.maxElements = 100;
	}

	/**
	 * Tests that after maximum amount of elements is reached we move them to persist list.
	 */
	@Test
	public void maxElementsReached() {
		aggregator.maxElements = 1;

		TimerData timerData1 = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		TimerData timerData2 = new TimerData(new Timestamp(System.currentTimeMillis()), 100L, 200L, 300L);

		aggregator.processTimerData(timerData1);
		aggregator.processTimerData(timerData2);

		assertThat(aggregator.getElementCount(), is(1));
		verifyZeroInteractions(session);
	}

	/**
	 * Tests that if we place many time same amount of elements, maximum will not be reached.
	 */
	@Test
	public void noMaxElementsReached() {
		aggregator.maxElements = 2;

		TimerData timerData1 = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		TimerData timerData2 = new TimerData(new Timestamp(System.currentTimeMillis()), 100L, 200L, 300L);

		for (int i = 0; i < 100; i++) {
			aggregator.processTimerData(timerData1);
			aggregator.processTimerData(timerData2);
		}

		assertThat(aggregator.getElementCount(), is(2));
		verifyZeroInteractions(session);
	}

	/**
	 * Tests that persist list saving includes correct elements being saved.
	 */
	@Test
	public void saveAllInPersistList() {
		aggregator.maxElements = 1;

		TimerData timerData1 = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		TimerData timerData2 = new TimerData(new Timestamp(System.currentTimeMillis()), 100L, 200L, 300L);

		aggregator.processTimerData(timerData1);
		aggregator.processTimerData(timerData2);

		aggregator.saveAllInPersistList();

		ArgumentCaptor<DatabaseAggregatedTimerData> argument = ArgumentCaptor.forClass(DatabaseAggregatedTimerData.class);
		verify(session, times(1)).insert(argument.capture());

		assertThat(argument.getValue(), is(instanceOf(DatabaseAggregatedTimerData.class)));
		assertThat(argument.getValue().getPlatformIdent(), is(timerData1.getPlatformIdent()));
		assertThat(argument.getValue().getSensorTypeIdent(), is(timerData1.getSensorTypeIdent()));
		assertThat(argument.getValue().getMethodIdent(), is(timerData1.getMethodIdent()));
	}

	/**
	 * Test for the validity of aggregation.
	 */
	@Test
	public void aggregation() {
		long timestampValue = new Date().getTime();
		long platformIdent = new Random().nextLong();

		final long count = 2;
		final double min = 1;
		final double max = 2;
		final double average = 1.5;
		final double duration = 3;

		TimerData timerData = new TimerData();
		timerData.setTimeStamp(new Timestamp(timestampValue));
		timerData.setPlatformIdent(platformIdent);
		timerData.setCount(count);
		timerData.setExclusiveCount(count);
		timerData.setDuration(duration);
		timerData.setCpuDuration(duration);
		timerData.setExclusiveDuration(duration);
		timerData.calculateMin(min);
		timerData.calculateCpuMin(min);
		timerData.calculateExclusiveMin(min);
		timerData.calculateMax(max);
		timerData.calculateCpuMax(max);
		timerData.calculateExclusiveMax(max);
		timerData.setMethodIdent(50L);

		TimerData timerData2 = new TimerData();
		timerData2.setTimeStamp(new Timestamp(timestampValue * 2));
		timerData2.setPlatformIdent(platformIdent);
		timerData2.setCount(count);
		timerData2.setExclusiveCount(count);
		timerData2.setDuration(duration);
		timerData2.setCpuDuration(duration);
		timerData2.setExclusiveDuration(duration);
		timerData2.calculateMin(min);
		timerData2.calculateCpuMin(min);
		timerData2.calculateExclusiveMin(min);
		timerData2.calculateMax(max);
		timerData2.calculateCpuMax(max);
		timerData2.calculateExclusiveMax(max);
		timerData2.setMethodIdent(100L);

		final int elements = 1000;

		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData);
		}

		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData2);
		}

		aggregator.removeAndPersistAll();

		verify(session, times(2)).insert(argThat(new ArgumentMatcher<TimerData>() {
			@Override
			public boolean matches(Object argument) {
				if (!DatabaseAggregatedTimerData.class.equals(argument.getClass())) {
					return false;
				}
				TimerData timerData = (TimerData) argument;

				assertThat(timerData.getCount() % count, is(equalTo(0L)));

				assertThat(timerData.getMin(), is(equalTo(min)));
				assertThat(timerData.getMax(), is(equalTo(max)));
				assertThat(timerData.getAverage(), is(equalTo(average)));
				assertThat(timerData.getDuration() / timerData.getCount(), is(equalTo(average)));

				assertThat(timerData.getCpuMin(), is(equalTo(min)));
				assertThat(timerData.getCpuMax(), is(equalTo(max)));
				assertThat(timerData.getCpuAverage(), is(equalTo(average)));
				assertThat(timerData.getCpuDuration() / timerData.getCount(), is(equalTo(average)));

				assertThat(timerData.getExclusiveMin(), is(equalTo(min)));
				assertThat(timerData.getExclusiveMax(), is(equalTo(max)));
				assertThat(timerData.getExclusiveAverage(), is(equalTo(average)));

				return true;
			}
		}));
	}

	/**
	 * Verify the zero interactions with setters of {@link TimerData} object passed to the
	 * aggregator.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void noSetterInteractions() {
		TimerData timerData = mock(TimerData.class);
		when(timerData.getTimeStamp()).thenReturn(new Timestamp(new Date().getTime()));
		when(timerData.getPlatformIdent()).thenReturn(10L);
		when(timerData.getMethodIdent()).thenReturn(20L);

		aggregator.processTimerData(timerData);

		verify(timerData, times(0)).setCount(anyLong());
		verify(timerData, times(0)).setCpuDuration(anyDouble());
		verify(timerData, times(0)).calculateCpuMax(anyDouble());
		verify(timerData, times(0)).calculateCpuMin(anyDouble());
		verify(timerData, times(0)).setDuration(anyDouble());
		verify(timerData, times(0)).setExclusiveCount(anyLong());
		verify(timerData, times(0)).setExclusiveDuration(anyDouble());
		verify(timerData, times(0)).calculateExclusiveMax(anyDouble());
		verify(timerData, times(0)).calculateExclusiveMin(anyDouble());
		verify(timerData, times(0)).setId(anyLong());
		verify(timerData, times(0)).calculateMax(anyDouble());
		verify(timerData, times(0)).setMethodIdent(anyLong());
		verify(timerData, times(0)).calculateMin(anyDouble());
		verify(timerData, times(0)).setParameterContentData((Set<ParameterContentData>) anyObject());
		verify(timerData, times(0)).setPlatformIdent(anyLong());
		verify(timerData, times(0)).setSensorTypeIdent(anyLong());
		verify(timerData, times(0)).setTimeStamp((Timestamp) anyObject());
		verify(timerData, times(0)).setVariance(anyDouble());
	}

}
