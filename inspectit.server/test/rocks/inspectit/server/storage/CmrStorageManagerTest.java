package info.novatec.inspectit.cmr.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.WritingStatus;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;
import info.novatec.inspectit.version.VersionService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the {@link CmrStorageManager} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class CmrStorageManagerTest extends AbstractTestNGLogSupport {

	private static final String CMR_VERSION = "v1";

	/**
	 * Class under test.
	 */
	private CmrStorageManager storageManager;

	@Mock
	private StorageDataDao storageDataDao;

	@Mock
	private CmrStorageWriterProvider storageWriterProvider;

	@Mock
	private CmrStorageRecorder storageRecorder;

	@Mock
	private CmrStorageWriter storageWriter;

	@Mock
	private SerializationManagerProvider serializationManagerProvider;

	@Mock
	private SerializationManager serializer;

	@Mock
	private IServerStatusService serverStatusService;

	@Mock
	private VersionService versionService;

	@Mock
	IBuffer<DefaultData> buffer;

	private StorageData storageData;

	/**
	 * Init method.
	 * 
	 * @throws Exception
	 */
	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		storageManager = new CmrStorageManager();
		storageManager.setStorageDefaultFolder("storageTest");
		storageManager.storageDataDao = storageDataDao;
		storageManager.storageWriterProvider = storageWriterProvider;
		storageManager.storageRecorder = storageRecorder;
		storageManager.buffer = buffer;
		storageManager.setSerializationManagerProvider(serializationManagerProvider);
		storageManager.serverStatusService = serverStatusService;
		storageManager.log = LoggerFactory.getLogger(CmrStorageManager.class);
		storageManager.versionService = versionService;
		when(storageWriterProvider.getCmrStorageWriter()).thenReturn(storageWriter);
		when(serializationManagerProvider.createSerializer()).thenReturn(serializer);
		when(versionService.getVersionAsString()).thenReturn(CMR_VERSION);

		Field field = StorageManager.class.getDeclaredField("log");
		field.setAccessible(true);
		field.set(storageManager, LoggerFactory.getLogger(CmrStorageManager.class));

		field = StorageManager.class.getDeclaredField("storageUploadsFolder");
		field.setAccessible(true);
		field.set(storageManager, "uploadTest");

		storageManager.postConstruct();
	}

	/**
	 * Test correct creation of storage.
	 */
	@Test
	public void createStorage() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		assertThat(storageData.getId(), is(notNullValue()));
		assertThat(storageData.getCmrVersion(), is(CMR_VERSION));
		assertThat(storageManager.isStorageExisting(storageData), is(true));
		assertThat(storageManager.isStorageOpen(storageData), is(false));
		assertThat(storageManager.isStorageClosed(storageData), is(false));
		assertThat(storageManager.getExistingStorages(), hasSize(1));
		assertThat(storageManager.getOpenedStorages(), is(empty()));
		assertThat(storageManager.getReadableStorages(), is(empty()));
	}

	/**
	 * Tests correct opening of storage.
	 */
	@Test
	public void openStorage() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);
		assertThat(storageManager.isStorageExisting(storageData), is(true));
		assertThat(storageManager.isStorageOpen(storageData), is(true));
		assertThat(storageManager.isStorageClosed(storageData), is(false));
		assertThat(storageManager.getExistingStorages(), hasSize(1));
		assertThat(storageManager.getOpenedStorages(), hasSize(1));
		assertThat(storageManager.getReadableStorages(), is(empty()));
		verify(storageWriterProvider, times(1)).getCmrStorageWriter();
		verify(storageWriter, times(1)).prepareForWrite(storageData);
	}

	/**
	 * Tests that already closed storage can not be opened.
	 */
	@Test(expectedExceptions = { BusinessException.class })
	public void canNotOpenAlreadyClosed() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);
		storageManager.closeStorage(storageData);
		storageManager.openStorage(storageData);
	}

	/**
	 * Tests closing of storage.
	 */
	@Test
	public void closeStorage() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);
		storageManager.closeStorage(storageData);
		assertThat(storageManager.isStorageExisting(storageData), is(true));
		assertThat(storageManager.isStorageOpen(storageData), is(false));
		assertThat(storageManager.isStorageClosed(storageData), is(true));
		assertThat(storageManager.getExistingStorages(), hasSize(1));
		assertThat(storageManager.getOpenedStorages(), is(empty()));
		assertThat(storageManager.getReadableStorages(), hasSize(1));
		// can not verify StorageWriter.closeStorageWriter cause it s final method
	}

	/**
	 * Tests closing of all storages.
	 */
	@Test
	public void closeAllStorages() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);
		storageManager.closeAllStorages();
		assertThat(storageManager.isStorageExisting(storageData), is(true));
		assertThat(storageManager.isStorageOpen(storageData), is(false));
		assertThat(storageManager.isStorageClosed(storageData), is(true));
		assertThat(storageManager.getExistingStorages(), hasSize(1));
		assertThat(storageManager.getOpenedStorages(), is(empty()));
		assertThat(storageManager.getReadableStorages(), hasSize(1));
		// can not verify StorageWriter.closeStorageWriter cause it s final method
	}

	/**
	 * Tests that storage used for recording can not be closed.
	 */
	@Test(expectedExceptions = { BusinessException.class })
	public void canNotCloseWhileRecording() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		RecordingProperties recordingProperties = mock(RecordingProperties.class);
		// AbstractDataProcessor dataProcessor = mock(AbstractDataProcessor.class);
		// when(recordingProperties.getRecordingDataProcessors()).thenReturn(Collections.singleton(dataProcessor));
		storageManager.startOrScheduleRecording(storageData, recordingProperties);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		when(storageRecorder.getRecordingState()).thenReturn(RecordingState.ON);
		when(storageRecorder.getRecordingProperties()).thenReturn(recordingProperties);
		when(storageRecorder.getStorageWriter()).thenReturn(storageWriter);
		storageManager.closeStorage(storageData);
	}

	/**
	 * Tests start of the recording.
	 */
	@Test
	public void startOrScheduleRecording() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		RecordingProperties recordingProperties = mock(RecordingProperties.class);
		WritingStatus writingStatus = WritingStatus.GOOD;
		storageManager.startOrScheduleRecording(storageData, recordingProperties);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		when(storageRecorder.getRecordingState()).thenReturn(RecordingState.ON);
		when(storageRecorder.getRecordingProperties()).thenReturn(recordingProperties);
		when(storageRecorder.getStorageWriter()).thenReturn(storageWriter);
		when(storageWriter.getWritingStatus()).thenReturn(writingStatus);
		verify(storageRecorder, times(1)).startOrScheduleRecording(storageWriter, recordingProperties);
		assertThat(storageManager.getRecordingState(), is(RecordingState.ON));
		assertThat(storageManager.getRecordingProperties(), is(recordingProperties));
		assertThat(storageManager.getRecordingStorage(), is(storageData));
		assertThat(storageManager.getRecordingStatus(), is(writingStatus));
	}

	/**
	 * Tests that recording can not be started if it s already running.
	 */
	@Test
	public void canNotStartRecordingWhenAlreadyRunning() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		RecordingProperties recordingProperties = mock(RecordingProperties.class);
		storageManager.startOrScheduleRecording(storageData, recordingProperties);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		when(storageRecorder.getRecordingState()).thenReturn(RecordingState.ON);
		when(storageRecorder.getRecordingProperties()).thenReturn(recordingProperties);
		when(storageRecorder.getStorageWriter()).thenReturn(storageWriter);
		verify(storageRecorder, times(1)).startOrScheduleRecording(storageWriter, recordingProperties);
		storageManager.startOrScheduleRecording(storageData, recordingProperties);
		verify(storageRecorder, times(1)).startOrScheduleRecording(storageWriter, recordingProperties);
	}

	/**
	 * Tests stop recording.
	 */
	@Test
	public void stopRecording() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");
		RecordingProperties recordingProperties = mock(RecordingProperties.class);
		when(recordingProperties.isAutoFinalize()).thenReturn(true);
		storageManager.startOrScheduleRecording(storageData, recordingProperties);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		when(storageRecorder.getRecordingState()).thenReturn(RecordingState.ON);
		when(storageRecorder.getRecordingProperties()).thenReturn(recordingProperties);
		when(storageRecorder.getStorageWriter()).thenReturn(storageWriter);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				when(storageRecorder.isRecordingOn()).thenReturn(false);
				when(storageRecorder.getRecordingState()).thenReturn(RecordingState.OFF);
				return null;
			}
		}).when(storageRecorder).stopRecording();
		storageManager.stopRecording();

		verify(storageRecorder, times(1)).stopRecording();
		assertThat(storageManager.isStorageClosed(storageData), is(true)); // due to auto-finalize
		assertThat(storageManager.getRecordingState(), is(RecordingState.OFF));
	}

	/**
	 * Tests data to be recorded.
	 */
	@Test
	public void record() {
		storageManager = spy(storageManager);
		DefaultData defaultData = mock(DefaultData.class);
		when(storageManager.canWriteMore()).thenReturn(true);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		storageManager.record(defaultData);

		verify(storageRecorder, times(1)).record(defaultData);
		when(storageRecorder.isRecordingOn()).thenReturn(false);
	}

	/**
	 * Tests that stop recording will be executed if manager reports that can not write more on
	 * disk.
	 */
	@Test
	public void stopRecordingWhenCanNotWriteMore() throws IOException, SerializationException, BusinessException {
		storageManager = spy(storageManager);
		DefaultData defaultData = mock(DefaultData.class);
		when(storageManager.canWriteMore()).thenReturn(false);
		when(storageRecorder.isRecordingOn()).thenReturn(true);
		Mockito.doNothing().when(storageManager).stopRecording();
		storageManager.record(defaultData);
		verify(storageManager, times(1)).stopRecording();
	}

	/**
	 * Tests writing of data to disk.
	 */
	@Test
	public void writeDataToStorage() throws BusinessException, IOException, SerializationException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);

		Collection<? extends DefaultData> data = Collections.singleton(mock(DefaultData.class));
		Collection<AbstractDataProcessor> processors = Collections.singleton(mock(AbstractDataProcessor.class));

		// first synchronously
		storageManager.writeToStorage(storageData, data, processors, true);
		verify(storageWriter, times(1)).processSynchronously(data, processors);

		// then asynchronously
		storageManager.writeToStorage(storageData, data, processors, false);
		verify(storageWriter, times(1)).process(data, processors);
	}

	/**
	 * Proves that no writing can be done to a closed storage.
	 */
	@Test(expectedExceptions = { BusinessException.class })
	public void canNotWriteToClosedStorage() throws BusinessException, IOException, SerializationException {
		storageData = new StorageData();
		storageData.setName("Test");
		storageManager.createStorage(storageData);
		storageManager.openStorage(storageData);
		storageManager.closeStorage(storageData);

		Collection<? extends DefaultData> data = Collections.singleton(mock(DefaultData.class));
		Collection<AbstractDataProcessor> processors = Collections.singleton(mock(AbstractDataProcessor.class));

		// first synchronously
		storageManager.writeToStorage(storageData, data, processors, true);
	}

	/**
	 * Tests copy buffer action.
	 */
	@Test
	public void copyBufferToStorage() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");

		DefaultData defaultData = mock(DefaultData.class);
		Timestamp timestamp = mock(Timestamp.class);
		when(defaultData.getTimeStamp()).thenReturn(timestamp);
		when(buffer.getOldestElement()).thenReturn(defaultData);

		List<DefaultData> data = Collections.singletonList(mock(DefaultData.class));
		Collection<AbstractDataProcessor> processors = Collections.singleton(mock(AbstractDataProcessor.class));
		Long platformId = 10L;
		List<Long> platformIdents = Collections.singletonList(platformId);
		storageManager = spy(storageManager);
		when(storageDataDao.getAllDefaultDataForAgent(eq(platformId), Mockito.<Date> any(), Mockito.<Date> any())).thenReturn(data);

		// first with no auto-finalize
		storageManager.copyBufferToStorage(storageData, platformIdents, processors, false);
		verify(storageDataDao, times(1)).getAllDefaultDataForAgent(eq(platformId), Mockito.<Date> any(), Mockito.<Date> any());
		verify(storageManager, times(1)).writeToStorage(storageData, data, processors, true);

		// first with auto-finalize
		storageManager.copyBufferToStorage(storageData, platformIdents, processors, true);
		verify(storageDataDao, times(2)).getAllDefaultDataForAgent(eq(platformId), Mockito.<Date> any(), Mockito.<Date> any());
		verify(storageManager, times(2)).writeToStorage(storageData, data, processors, true);
		assertThat(storageManager.isStorageClosed(storageData), is(true));
	}

	/**
	 * Tests copy data to storage action.
	 */
	@SuppressWarnings("unchecked")
	public void copyDataToStorage() throws IOException, SerializationException, BusinessException {
		storageData = new StorageData();
		storageData.setName("Test");

		List<DefaultData> data = Collections.singletonList(mock(DefaultData.class));
		Collection<AbstractDataProcessor> processors = Collections.singleton(mock(AbstractDataProcessor.class));
		storageManager = spy(storageManager);
		long platformIdent = 10L;
		Collection<Long> elementIds = mock(Collection.class);
		when(storageDataDao.getDataFromIdList(elementIds, platformIdent)).thenReturn(data);

		// first with no auto-finalize
		storageManager.copyDataToStorage(storageData, elementIds, platformIdent, processors, false);
		verify(storageDataDao, times(1)).getDataFromIdList(elementIds, platformIdent);
		verify(storageManager, times(1)).writeToStorage(storageData, data, processors, true);

		// first with auto-finalize
		storageManager.copyDataToStorage(storageData, elementIds, platformIdent, processors, false);
		verify(storageDataDao, times(2)).getDataFromIdList(elementIds, platformIdent);
		verify(storageManager, times(2)).writeToStorage(storageData, data, processors, true);
		assertThat(storageManager.isStorageClosed(storageData), is(true));
	}

	/**
	 * After processing to delete storage that might be created in the test.
	 */
	@AfterMethod
	public void deleteStorage() throws BusinessException, IOException, SerializationException {
		if (null != storageData) {
			if (storageManager.getRecordingState() == RecordingState.ON) {
				storageManager.stopRecording();
			}
			if (!storageManager.isStorageClosed(storageData)) {
				storageManager.closeStorage(storageData);
			}
			storageManager.deleteStorage(storageData);
			storageData = null;
		}
		assertThat(storageManager.getExistingStorages(), is(empty()));
	}

	/**
	 * After tests delete created folders.
	 */
	@AfterTest
	public void deleteFolders() throws IOException {
		Files.deleteIfExists(Paths.get(storageManager.getStorageDefaultFolder()));
		Files.deleteIfExists(Paths.get(storageManager.getStorageUploadsFolder()));
	}

}
