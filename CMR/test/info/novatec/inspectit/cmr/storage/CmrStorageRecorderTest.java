package info.novatec.inspectit.cmr.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for the {@link CmrStorageRecorder}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class CmrStorageRecorderTest extends AbstractTestNGLogSupport {

	/**
	 * Class under test.
	 */
	private CmrStorageRecorder cmrStorageRecorder;

	@Mock
	private CmrStorageManager cmrStorageManager;

	@Mock
	private StorageDataDao storageDataDao;

	@Mock
	private ScheduledExecutorService executorService;

	@Mock
	private AbstractDataProcessor dataProcessor;

	@Mock
	private StorageWriter storageWriter;

	@Mock
	private RecordingProperties recordingProperties;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		cmrStorageRecorder = new CmrStorageRecorder();
		cmrStorageRecorder.storageDataDao = storageDataDao;
		cmrStorageRecorder.cmrStorageManager = cmrStorageManager;
		cmrStorageRecorder.executorService = executorService;
		cmrStorageRecorder.log = LoggerFactory.getLogger(CmrStorageRecorder.class);
		when(storageWriter.isWritingOn()).thenReturn(true);
	}

	/**
	 * Test that start of recording and processing of data is correct.
	 */
	@Test
	public void processing() throws StorageException {
		when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		cmrStorageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);
		assertThat(cmrStorageRecorder.isRecordingOn(), is(true));

		DefaultData defaultData = mock(DefaultData.class);
		cmrStorageRecorder.record(defaultData);

		verify(dataProcessor, times(1)).setStorageWriter(storageWriter);
		verify(dataProcessor, times(1)).process(defaultData);
		verifyNoMoreInteractions(dataProcessor);
	}

	/**
	 * Tests that no data will be processed if recording is off and storage writer is turned off.
	 */
	@Test
	public void noProcessing() throws StorageException {
		cmrStorageRecorder = spy(cmrStorageRecorder);
		when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		cmrStorageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);

		DefaultData defaultData = mock(DefaultData.class);
		when(cmrStorageRecorder.isRecordingOn()).thenReturn(false);
		cmrStorageRecorder.record(defaultData);
		when(cmrStorageRecorder.isRecordingOn()).thenReturn(true);
		when(storageWriter.isWritingOn()).thenReturn(false);
		cmrStorageRecorder.record(defaultData);

		verify(dataProcessor, times(0)).process(defaultData);
	}

	/**
	 * Tests that the recording will be scheduled if start delay is specified.
	 */
	@Test
	public void scheduleRecording() throws StorageException {
		cmrStorageRecorder = spy(cmrStorageRecorder);
		long recordingDelay = 1000L;
		when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		when(recordingProperties.getStartDelay()).thenReturn(recordingDelay);
		cmrStorageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);

		assertThat(cmrStorageRecorder.isRecordingOn(), is(false));
		assertThat(cmrStorageRecorder.isRecordingScheduled(), is(true));

		ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService, times(1)).schedule(captor.capture(), eq(recordingDelay), eq(TimeUnit.MILLISECONDS));
	}

	/**
	 * Test that recording will be stopped if recording duration is set.
	 */
	@Test
	public void scheduleRecordingStop() throws StorageException, IOException, SerializationException {
		cmrStorageRecorder = spy(cmrStorageRecorder);
		long recordingDuration = 1000L;
		when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		when(recordingProperties.getRecordDuration()).thenReturn(recordingDuration);
		cmrStorageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);

		assertThat(cmrStorageRecorder.isRecordingOn(), is(true));
		assertThat(cmrStorageRecorder.isRecordingScheduled(), is(false));

		ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService, times(1)).schedule(captor.capture(), eq(recordingDuration), eq(TimeUnit.MILLISECONDS));

		captor.getValue().run();
		verify(cmrStorageManager, times(1)).stopRecording();
	}

	/**
	 * Tests that stop recording will correctly flush the data processor and record the system
	 * information data for all recorded platforms.
	 */
	@Test
	public void stopRecording() throws StorageException {
		cmrStorageRecorder = spy(cmrStorageRecorder);
		when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		cmrStorageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);

		long platformId = 10L;
		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getPlatformIdent()).thenReturn(platformId);
		cmrStorageRecorder.record(defaultData);

		SystemInformationData systemInformationData = mock(SystemInformationData.class);
		when(storageDataDao.getSystemInformationData(Collections.singleton(platformId))).thenReturn(Collections.singletonList(systemInformationData));

		cmrStorageRecorder.stopRecording();

		verify(dataProcessor, times(1)).flush();
		verify(cmrStorageRecorder, times(1)).record(systemInformationData);

		assertThat(cmrStorageRecorder.isRecordingOn(), is(false));
	}

}
