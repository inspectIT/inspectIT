package info.novatec.inspectit.cmr.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.dao.impl.TimerDataAggregator;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.cmr.util.CacheIdGenerator;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.util.Collections;

import org.hibernate.StatelessSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the all cmr data processors we have.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class CmrDataProcessorsTest {

	@Mock
	Logger log;

	@Mock
	private StatelessSession session;

	@Mock
	private IBuffer<MethodSensorData> buffer;

	@Mock
	private CacheIdGenerator cacheIdGenerator;

	@Mock
	private IBufferTreeComponent<DefaultData> indexingTree;

	@Mock
	private CmrStorageManager storageManager;

	@Mock
	private TimerDataAggregator timerDataAggregator;

	@Mock
	private AbstractCmrDataProcessor chainedProcessor;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the {@link BufferInserterCmrProcessor}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void bufferInserter() {
		BufferInserterCmrProcessor processor = new BufferInserterCmrProcessor();
		processor.buffer = buffer;

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(buffer, session);

		// we don't allow system sensor data
		processor.process(new CpuInformationData(), session);
		verifyZeroInteractions(buffer, session);

		// we only allow invocation that is a root
		InvocationSequenceData invocationSequenceData = mock(InvocationSequenceData.class, RETURNS_SMART_NULLS);
		processor.process(invocationSequenceData, session);
		verifyZeroInteractions(buffer, session);

		// we don't insert data that's part of invocation
		InvocationAwareData invocationAwareData = mock(InvocationAwareData.class);
		when(invocationAwareData.isOnlyFoundInInvocations()).thenReturn(false);
		when(invocationAwareData.isOnlyFoundOutsideInvocations()).thenReturn(false);
		processor.process(invocationAwareData, session);
		verifyZeroInteractions(buffer, session);

		// allow other data
		invocationAwareData = mock(InvocationAwareData.class);
		when(invocationAwareData.isOnlyFoundInInvocations()).thenReturn(false);
		when(invocationAwareData.isOnlyFoundOutsideInvocations()).thenReturn(true);
		processor.process(invocationAwareData, session);
		ArgumentCaptor<IBufferElement> captor = ArgumentCaptor.forClass(IBufferElement.class);
		verify(buffer, times(1)).put(captor.capture());
		verifyZeroInteractions(session);
		assertThat(captor.getValue().getObject(), is(equalTo(((Object) invocationAwareData))));
	}

	/**
	 * Tests the {@link CacheIdGeneratorCmrProcessor}.
	 */
	@Test
	public void cacheIdProcessor() {
		CacheIdGeneratorCmrProcessor processor = new CacheIdGeneratorCmrProcessor();
		processor.cacheIdGenerator = cacheIdGenerator;

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(cacheIdGenerator, session);

		// assign Id otherwise
		DefaultData defaultData = mock(DefaultData.class);
		processor.process(defaultData, session);
		verify(cacheIdGenerator, times(1)).assignObjectAnId(defaultData);
		verifyZeroInteractions(session);
	}

	/**
	 * Tests the {@link ExceptionMessageCmrProcessor}.
	 */
	@Test
	public void exceptionMessageProcessor() {
		ExceptionMessageCmrProcessor processor = new ExceptionMessageCmrProcessor();

		// only exceptions
		assertThat(processor.canBeProcessed(new TimerData()), is(false));

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(session);

		ExceptionSensorData parent = new ExceptionSensorData();
		parent.setErrorMessage("parentMsg");
		ExceptionSensorData child = new ExceptionSensorData();
		child.setErrorMessage("childMsg");
		parent.setChild(child);

		// prove message changing
		processor.process(parent, session);
		assertThat(parent.getErrorMessage(), is("parentMsg"));
		assertThat(child.getErrorMessage(), is("parentMsg"));
	}

	/**
	 * Tests the {@link IndexerCmrProcessor}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void indexerProcessor() throws IndexingException {
		IndexerCmrProcessor processor = new IndexerCmrProcessor();
		processor.log = log;
		processor.indexingTree = indexingTree;

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(log, indexingTree, session);

		// don't allow system sensor data
		processor.process(new CpuInformationData(), session);
		verifyZeroInteractions(log, indexingTree, session);

		// don't allow invocations
		processor.process(new InvocationSequenceData(), session);
		verifyZeroInteractions(log, indexingTree, session);

		// don't allow invocation aware data that is not part of invocation
		InvocationAwareData invocationAwareData = mock(InvocationAwareData.class);
		when(invocationAwareData.isOnlyFoundInInvocations()).thenReturn(false);
		when(invocationAwareData.isOnlyFoundOutsideInvocations()).thenReturn(false);
		processor.process(invocationAwareData, session);
		when(invocationAwareData.isOnlyFoundOutsideInvocations()).thenReturn(true);
		processor.process(invocationAwareData, session);
		verifyZeroInteractions(log, indexingTree, session);

		// index other data
		invocationAwareData = mock(InvocationAwareData.class);
		when(invocationAwareData.isOnlyFoundInInvocations()).thenReturn(true);
		when(invocationAwareData.isOnlyFoundOutsideInvocations()).thenReturn(false);
		processor.process(invocationAwareData, session);
		verify(indexingTree, times(1)).put(invocationAwareData);

		// survive indexing exception
		when(indexingTree.put(indexingTree.put(invocationAwareData))).thenThrow(IndexingException.class);
		processor.process(invocationAwareData, session);
		verifyZeroInteractions(session);
	}

	/**
	 * Tests the {@link RecorderCmrProcessor}.
	 */
	@Test
	public void recordProcessor() {
		RecorderCmrProcessor processor = new RecorderCmrProcessor();
		processor.storageManager = storageManager;

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(storageManager, session);

		DefaultData defaultData = mock(DefaultData.class);

		// don't call record if it's not on
		when(storageManager.getRecordingState()).thenReturn(RecordingState.OFF);
		processor.process(defaultData, session);
		verify(storageManager, times(0)).record(defaultData);

		// call record if it's on
		when(storageManager.getRecordingState()).thenReturn(RecordingState.ON);
		processor.process(defaultData, session);
		verify(storageManager, times(1)).record(defaultData);

		verifyZeroInteractions(session);
	}

	/**
	 * Tests the {@link SessionInserterCmrProcessor}.
	 */
	@Test
	public void sessionInserterProcessor() {
		// only Timer Data
		SessionInserterCmrProcessor processor = new SessionInserterCmrProcessor(Collections.<Class<? extends DefaultData>> singletonList(TimerData.class));

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(session);

		// don't process wrong classes
		processor.process(new SqlStatementData(), session);
		processor.process(new HttpTimerData(), session);
		verifyZeroInteractions(session);

		// yes for correct class
		TimerData timerData = new TimerData();
		processor.process(timerData, session);
		verify(session, times(1)).insert(timerData);
	}

	/**
	 * Tests the {@link SqlExclusiveTimeCmrProcessor}.
	 */
	@Test
	public void sqlExclusiveTimeProcessor() {
		SqlExclusiveTimeCmrProcessor processor = new SqlExclusiveTimeCmrProcessor();

		// don't fail on null
		processor.process((DefaultData) null, session);

		// only sqls
		assertThat(processor.canBeProcessed(new TimerData()), is(false));

		// make sure exclusive data is set
		SqlStatementData sqlStatementData = new SqlStatementData();
		sqlStatementData.setDuration(5d);
		processor.process(sqlStatementData, session);

		assertThat(sqlStatementData.getExclusiveCount(), is(1l));
		assertThat(sqlStatementData.getExclusiveDuration(), is(5d));
		assertThat(sqlStatementData.getExclusiveMin(), is(5d));
		assertThat(sqlStatementData.getExclusiveMax(), is(5d));

		verifyZeroInteractions(session);
	}

	/**
	 * Tests the {@link SystemInformationDataCmrProcessor}.
	 */
	@Test
	public void systemInformationProcessor() {
		SystemInformationDataCmrProcessor processor = new SystemInformationDataCmrProcessor();

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(session);

		// false data will not be processed
		processor.process(new TimerData(), session);
		processor.process(new CpuInformationData(), session);
		verifyZeroInteractions(session);

		// system information
		SystemInformationData systemInformationData = new SystemInformationData();
		systemInformationData.addVMArguments("vmArgumentName", "vmArgumentValue");

		when(session.insert(systemInformationData)).thenReturn(Long.valueOf(3l));
		processor.process(systemInformationData, session);
		verify(session, times(1)).insert(systemInformationData);

		VmArgumentData vmArgumentData = systemInformationData.getVmSet().iterator().next();
		verify(session, times(1)).insert(vmArgumentData);
		assertThat(vmArgumentData.getSystemInformationId(), is(3l));
	}

	/**
	 * Tests the {@link TimerDataChartingCmrProcessor}.
	 */
	@Test
	public void chartingProcessor() {
		TimerDataChartingCmrProcessor processor = new TimerDataChartingCmrProcessor();
		processor.timerDataAggregator = timerDataAggregator;

		// don't fail on null
		processor.process((DefaultData) null, session);
		verifyZeroInteractions(timerDataAggregator, session);

		TimerData timerData = mock(TimerData.class);
		HttpTimerData httpTimerData = mock(HttpTimerData.class);

		// first with no charting skip
		when(timerData.isCharting()).thenReturn(false);
		when(httpTimerData.isCharting()).thenReturn(false);
		processor.process(timerData, session);
		processor.process(httpTimerData, session);
		verifyZeroInteractions(timerDataAggregator, session);

		// then with charting process
		when(timerData.isCharting()).thenReturn(true);
		when(httpTimerData.isCharting()).thenReturn(true);
		processor.process(timerData, session);
		processor.process(httpTimerData, session);
		// timer to aggregator
		verify(timerDataAggregator, times(1)).processTimerData(timerData);
		// http to session
		verify(session, times(1)).insert(httpTimerData);
		verifyNoMoreInteractions(timerDataAggregator, session);
	}

	/**
	 * Timer data processing with {@link InvocationModifierCmrProcessor}.
	 */
	@Test
	public void invocationProcessorTimerData() {
		InvocationModifierCmrProcessor processor = new InvocationModifierCmrProcessor(Collections.singletonList(chainedProcessor));

		InvocationSequenceData parent = new InvocationSequenceData();
		parent.setId(10L);
		TimerData parentTimer = new TimerData();
		parentTimer.setCount(1L);
		parentTimer.setDuration(2L);
		parent.setTimerData(parentTimer);

		InvocationSequenceData child = new InvocationSequenceData();
		child.setId(20L);
		TimerData childTimer = new TimerData();
		childTimer.setCount(1L);
		childTimer.setDuration(1L);
		child.setTimerData(childTimer);
		child.setParentSequence(parent);

		parent.setNestedSequences(Collections.singletonList(child));

		processor.process(parent, session);

		// correctly passed to the chained
		verify(chainedProcessor, times(1)).process(parentTimer, session);
		verify(chainedProcessor, times(1)).process(child, session);
		verify(chainedProcessor, times(1)).process(childTimer, session);
		verifyNoMoreInteractions(chainedProcessor);
		verifyZeroInteractions(session);

		// exclusive times in timers are set
		assertThat(parentTimer.getExclusiveDuration(), is(1d));
		assertThat(childTimer.getExclusiveDuration(), is(1d));

		// invocation parent is set correctly
		assertThat(parentTimer.isOnlyFoundInInvocations(), is(true));
		assertThat(parentTimer.getInvocationParentsIdSet(), hasSize(1));
		assertThat(parentTimer.getInvocationParentsIdSet(), hasItem(10L));

		// not that every timer must point to the root invocation
		assertThat(childTimer.isOnlyFoundInInvocations(), is(true));
		assertThat(childTimer.getInvocationParentsIdSet(), hasSize(1));
		assertThat(childTimer.getInvocationParentsIdSet(), hasItem(10L));
	}

	/**
	 * Sql data processing with {@link InvocationModifierCmrProcessor}.
	 */
	@Test
	public void invocationProcessorSqlData() {
		InvocationModifierCmrProcessor processor = new InvocationModifierCmrProcessor(Collections.singletonList(chainedProcessor));

		InvocationSequenceData parent = new InvocationSequenceData();
		parent.setId(10L);
		TimerData parentTimer = new TimerData();
		parentTimer.setCount(1L);
		parentTimer.setDuration(2L);
		parent.setTimerData(parentTimer);

		InvocationSequenceData child = new InvocationSequenceData();
		child.setId(20L);
		SqlStatementData sql = new SqlStatementData();
		sql.setCount(1L);
		sql.setDuration(1L);
		child.setSqlStatementData(sql);
		child.setParentSequence(parent);

		parent.setNestedSequences(Collections.singletonList(child));

		processor.process(parent, session);

		// correctly passed to the chained
		verify(chainedProcessor, times(1)).process(parentTimer, session);
		verify(chainedProcessor, times(1)).process(child, session);
		verify(chainedProcessor, times(1)).process(sql, session);
		verifyNoMoreInteractions(chainedProcessor);
		verifyZeroInteractions(session);

		// root has info about sql
		assertThat(parent.isNestedSqlStatements(), is(true));

		// exclusive times in parent timer is set
		assertThat(parentTimer.getExclusiveDuration(), is(1d));

		// sql has correct invocation affiliation
		assertThat(sql.isOnlyFoundInInvocations(), is(true));
		assertThat(sql.getInvocationParentsIdSet(), hasSize(1));
		assertThat(sql.getInvocationParentsIdSet(), hasItem(10L));
	}

	/**
	 * Simple exception processing with {@link InvocationModifierCmrProcessor}.
	 */
	@Test
	public void invocationProcessorOneExceptionData() {
		InvocationModifierCmrProcessor processor = new InvocationModifierCmrProcessor(Collections.singletonList(chainedProcessor));
		ExceptionMessageCmrProcessor exceptionMessageCmrProcessor = mock(ExceptionMessageCmrProcessor.class);
		processor.exceptionMessageCmrProcessor = exceptionMessageCmrProcessor;

		InvocationSequenceData parent = new InvocationSequenceData();
		parent.setId(10L);

		InvocationSequenceData child = new InvocationSequenceData();
		child.setId(20L);
		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);
		exceptionSensorData.setThrowableIdentityHashCode(1L);
		child.setExceptionSensorDataObjects(Collections.singletonList(exceptionSensorData));
		child.setParentSequence(parent);

		parent.setNestedSequences(Collections.singletonList(child));

		processor.process(parent, session);

		// correctly passed to the chained
		verify(chainedProcessor, times(1)).process(child, session);
		verify(chainedProcessor, times(1)).process(exceptionSensorData, session);
		verify(exceptionMessageCmrProcessor, times(1)).process(exceptionSensorData, session);
		verifyNoMoreInteractions(chainedProcessor, exceptionMessageCmrProcessor);
		verifyZeroInteractions(session);

		// root has info about sql
		assertThat(parent.isNestedExceptions(), is(true));

		// sql has correct invocation affiliation
		assertThat(exceptionSensorData.isOnlyFoundInInvocations(), is(true));
		assertThat(exceptionSensorData.getInvocationParentsIdSet(), hasSize(1));
		assertThat(exceptionSensorData.getInvocationParentsIdSet(), hasItem(10L));
	}

	/**
	 * When we have not created exception event in exception object.
	 */
	@Test
	public void invocationProcessorNotCreatedExceptionData() {
		InvocationModifierCmrProcessor processor = new InvocationModifierCmrProcessor(Collections.singletonList(chainedProcessor));
		ExceptionMessageCmrProcessor exceptionMessageCmrProcessor = mock(ExceptionMessageCmrProcessor.class);
		processor.exceptionMessageCmrProcessor = exceptionMessageCmrProcessor;

		InvocationSequenceData parent = new InvocationSequenceData();
		parent.setId(10L);

		InvocationSequenceData child = new InvocationSequenceData();
		child.setId(20L);
		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setExceptionEvent(ExceptionEvent.PASSED);
		exceptionSensorData.setThrowableIdentityHashCode(1L);
		child.setExceptionSensorDataObjects(Collections.singletonList(exceptionSensorData));
		child.setParentSequence(parent);

		parent.setNestedSequences(Collections.singletonList(child));

		processor.process(parent, session);

		// correctly passed to the chained
		verify(chainedProcessor, times(1)).process(child, session);
		verify(chainedProcessor, times(0)).process(exceptionSensorData, session);
		verify(exceptionMessageCmrProcessor, times(0)).process(exceptionSensorData, session);
		verifyNoMoreInteractions(chainedProcessor, exceptionMessageCmrProcessor);
		verifyZeroInteractions(session);

		// root has info about sql
		assertThat(parent.isNestedExceptions(), is(nullValue()));

		// sql has correct invocation affiliation
		assertThat(exceptionSensorData.isOnlyFoundInInvocations(), is(false));
		assertThat(exceptionSensorData.getInvocationParentsIdSet(), is(empty()));
	}

}
