package info.novatec.inspectit.storage.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;
import info.novatec.inspectit.storage.IWriter;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.serializer.util.KryoSerializationPreferences;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests all {@link AbstractDataProcessor}s for the correct functionality.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class StorageDataProcessorsTest {

	/**
	 * Storage writer.
	 */
	@Mock
	private IWriter storageWriter;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		Answer<Future<Void>> answer = new Answer<Future<Void>>() {
			@SuppressWarnings("unchecked")
			public Future<Void> answer(InvocationOnMock invocation) throws Throwable {
				return mock(Future.class);
			}
		};

		when(storageWriter.write(Mockito.<DefaultData> anyObject())).thenAnswer(answer);
		when(storageWriter.write(Mockito.<DefaultData> anyObject(), Mockito.<Map<?, ?>> anyObject())).thenAnswer(answer);
	}

	/**
	 * Tests that the {@link DataSaverProcessor} will not save data type if type is not registered
	 * with a processor.
	 */
	@Test
	public void testSimpleSaverExclusive() {
		List<Class<? extends DefaultData>> includedClasses = new ArrayList<Class<? extends DefaultData>>();
		includedClasses.add(InvocationSequenceData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(includedClasses, true);
		dataSaverProcessor.setStorageWriter(storageWriter);

		TimerData timerData = new TimerData();
		assertThat(dataSaverProcessor.canBeProcessed(timerData), is(false));

		Collection<Future<Void>> futures = dataSaverProcessor.process(timerData);
		assertThat(futures, is(empty()));
		verifyZeroInteractions(storageWriter);
	}

	/**
	 * Tests that the {@link DataSaverProcessor} will save data type if type is not registered with
	 * a processor.
	 */
	@Test
	public void testSimpleSaverInclusive() {
		List<Class<? extends DefaultData>> includedClasses = new ArrayList<Class<? extends DefaultData>>();
		includedClasses.add(InvocationSequenceData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(includedClasses, true);
		dataSaverProcessor.setStorageWriter(storageWriter);

		InvocationSequenceData invocation = new InvocationSequenceData();
		assertThat(dataSaverProcessor.canBeProcessed(invocation), is(true));

		Collection<Future<Void>> futures = dataSaverProcessor.process(invocation);
		assertThat(futures, hasSize(1));
		verify(storageWriter, times(1)).write(invocation);
	}

	/**
	 * Test that the agent filtering is correct.
	 */
	@Test
	public void agentFilterDataProcessor() {
		AbstractDataProcessor abstractDataProcessor = mock(AbstractDataProcessor.class);
		AgentFilterDataProcessor dataProcessor = new AgentFilterDataProcessor(Collections.singletonList(abstractDataProcessor), Collections.singleton(10L));
		DefaultData data1 = mock(DefaultData.class);
		DefaultData data2 = mock(DefaultData.class);
		when(data1.getPlatformIdent()).thenReturn(10L);
		when(data2.getPlatformIdent()).thenReturn(20L);

		dataProcessor.process(data1);
		dataProcessor.process(data2);

		assertThat(dataProcessor.canBeProcessed(data1), is(true));
		assertThat(dataProcessor.canBeProcessed(data2), is(true));

		verify(abstractDataProcessor, times(1)).process(data1);
		verify(abstractDataProcessor, times(0)).process(data2);
	}

	/**
	 * Test the {@link InvocationClonerDataProcessor}.
	 */
	@Test
	public void invocationClonerDataProcessor() {
		InvocationClonerDataProcessor dataProcessor = new InvocationClonerDataProcessor();

		InvocationSequenceData invocationSequenceData = mock(InvocationSequenceData.class);
		DefaultData defaultData = mock(DefaultData.class);
		IWriter writer = mock(IWriter.class);
		dataProcessor.setStorageWriter(writer);

		assertThat(dataProcessor.canBeProcessed(invocationSequenceData), is(true));
		assertThat(dataProcessor.canBeProcessed(defaultData), is(false));

		dataProcessor.process(invocationSequenceData);
		dataProcessor.process(defaultData);

		verify(invocationSequenceData, times(1)).getClonedInvocationSequence();
		verify(writer, times(1)).write(invocationSequenceData.getClonedInvocationSequence());
		verify(writer, times(0)).write(invocationSequenceData);
	}

	/**
	 * Test that {@link InvocationExtractorDataProcessor} will extract all children of an
	 * invocation.
	 */
	@Test
	public void testInvocationExtractor() {
		AbstractDataProcessor chainedProcessor = mock(AbstractDataProcessor.class);
		List<AbstractDataProcessor> chainedList = new ArrayList<AbstractDataProcessor>();
		chainedList.add(chainedProcessor);

		InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedList);
		invocationExtractorDataProcessor.setStorageWriter(storageWriter);

		InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
		List<InvocationSequenceData> children = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData child1 = new InvocationSequenceData(new Timestamp(new Date().getTime()), 10, 10, 10);
		TimerData timerData = new TimerData();
		child1.setTimerData(timerData);

		InvocationSequenceData child2 = new InvocationSequenceData(new Timestamp(new Date().getTime()), 20, 20, 20);
		SqlStatementData sqlStatementData = new SqlStatementData();
		child2.setSqlStatementData(sqlStatementData);

		children.add(child1);
		children.add(child2);
		invocationSequenceData.setNestedSequences(children);

		assertThat(invocationExtractorDataProcessor.canBeProcessed(invocationSequenceData), is(true));
		invocationExtractorDataProcessor.process(invocationSequenceData);
		verify(chainedProcessor, times(1)).process(timerData);
		verify(chainedProcessor, times(1)).process(timerData);
		verify(chainedProcessor, times(0)).process(child1);
		verify(chainedProcessor, times(0)).process(child2);
	}

	/**
	 * Test that {@link TimeFrameDataProcessor} only passed the data that belongs to the given time
	 * frame.
	 */
	@Test
	public void testTimeframeProcessor() {
		DefaultData defaultData = mock(DefaultData.class);
		long time = 10000000;
		long past = time - 1000;
		long future = time + 1000;

		AbstractDataProcessor dataProcessor = mock(AbstractDataProcessor.class);
		List<AbstractDataProcessor> chainedProcessors = new ArrayList<AbstractDataProcessor>();
		chainedProcessors.add(dataProcessor);

		TimeFrameDataProcessor timeFrameDataProcessor = new TimeFrameDataProcessor(new Date(past), new Date(future), chainedProcessors);
		assertThat(timeFrameDataProcessor.canBeProcessed(defaultData), is(true));

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(time));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(past));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(future));
		timeFrameDataProcessor.process(defaultData);

		verify(dataProcessor, times(3)).process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(past - 1000));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(future + 1000));
		timeFrameDataProcessor.process(defaultData);

		verify(dataProcessor, times(3)).process(defaultData);

	}

	/**
	 * Test the {@link DataAggregatorProcessor} for a correct aggregation of data.
	 */
	@Test
	public void dataAggregatorProcessorAggregation() {
		int aggregationPeriod = 100;
		DataAggregatorProcessor<TimerData> dataAggregatorProcessor = new DataAggregatorProcessor<TimerData>(TimerData.class, aggregationPeriod, new TimerDataAggregator(), true);
		dataAggregatorProcessor.setStorageWriter(storageWriter);

		long timestampValue = new Date().getTime();
		Random random = new Random();
		long platformIdent = random.nextLong();
		long sensorTypeIdent = random.nextLong();
		long methodIdent = random.nextLong();
		TimerData timerData = new TimerData(new Timestamp(timestampValue), platformIdent, sensorTypeIdent, methodIdent);

		assertThat(dataAggregatorProcessor.canBeProcessed(timerData), is(true));

		final int elements = 1000;
		for (int i = 0; i < elements / 2; i++) {
			dataAggregatorProcessor.process(timerData);
		}

		Collection<Future<Void>> futures = dataAggregatorProcessor.flush();
		assertThat(futures, hasSize(1));
		verify(storageWriter, times(1)).write(Mockito.<TimerData> anyObject());
	}

	/**
	 * Test that aggregation processor will write elements when needed.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void dataAggregationProcessorWritting() {
		IAggregator<TimerData> aggregator = mock(IAggregator.class);
		when(aggregator.getAggregationKey(Mockito.<TimerData> anyObject())).thenReturn(1L);
		// setting max elements to 1, aggreation to 1000
		DataAggregatorProcessor<TimerData> dataProcessor = new DataAggregatorProcessor<TimerData>(TimerData.class, 1000, 1, aggregator, false);
		IWriter writer = mock(IWriter.class);
		dataProcessor.setStorageWriter(writer);

		TimerData timerData1 = new TimerData();
		timerData1.setId(1L);
		timerData1.setTimeStamp(new Timestamp(new Date().getTime()));
		when(aggregator.getClone(Mockito.<TimerData> anyObject())).thenReturn(new AggregatedTimerData());

		dataProcessor.process(timerData1);
		dataProcessor.process(timerData1);

		// no interaction with the writer at this point
		verify(aggregator, times(2)).aggregate(Mockito.<IAggregatedData<TimerData>> anyObject(), eq(timerData1));
		verifyNoMoreInteractions(writer);

		// now process same element with the too diff time stamp
		Timestamp newTimestamp = new Timestamp(timerData1.getTimeStamp().getTime() + 2000L);
		timerData1.setTimeStamp(newTimestamp);

		dataProcessor.process(timerData1);

		ArgumentCaptor<DefaultData> writtenObject = ArgumentCaptor.forClass(DefaultData.class);
		ArgumentCaptor<Map> kryoMap = ArgumentCaptor.forClass(Map.class);
		verify(writer, times(1)).write(writtenObject.capture(), kryoMap.capture());

		assertThat(writtenObject.getValue(), is(instanceOf(AggregatedTimerData.class)));
		assertThat(((AggregatedTimerData) writtenObject.getValue()).getId(), is(1L));
		assertThat(((Map<String, Boolean>) kryoMap.getValue()), hasEntry(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA, Boolean.FALSE));
	}
}
