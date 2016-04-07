package info.novatec.inspectit.cmr.service.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.cmr.service.rest.unsafe.IUnsafeStorageService;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.processor.impl.InvocationExtractorDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests the {@link StorageRestfulService}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class StorageRestfulServiceTest {

	/**
	 * Class under test.
	 */
	private StorageRestfulService restfulService;

	/**
	 * Mocked {@link IStorageService}.
	 */
	@Mock
	private IUnsafeStorageService storageService;

	/**
	 * {@link StorageData}.
	 */
	@Mock
	private StorageData storageData;

	/**
	 * Init.
	 */
	@BeforeTest
	public void init() {
		MockitoAnnotations.initMocks(this);
		restfulService = new StorageRestfulService();
		restfulService.storageService = storageService;
	}

	@Test
	public void getStorageById() {
		String id = "id";
		when(storageData.getId()).thenReturn(id);
		when(storageService.getExistingStorages()).thenReturn(Collections.singletonList(storageData));

		assertThat(restfulService.getStorageById(id), is(storageData));

		when(storageData.getId()).thenReturn("");
		assertThat(restfulService.getStorageById(id), is(nullValue()));
	}

	@Test
	public void createStorage() throws BusinessException {
		String name = "name";
		restfulService.createStorage(name);

		ArgumentCaptor<StorageData> captor = ArgumentCaptor.forClass(StorageData.class);
		verify(storageService, times(1)).createAndOpenStorage(captor.capture());
		assertThat(captor.getValue().getName(), is(name));
	}

	@Test(expectedExceptions = { BusinessException.class })
	public void createStorageEmptyName() throws BusinessException {
		restfulService.createStorage("");
	}

	@Test
	public void deleteStorage() throws BusinessException {
		String id = "id";
		restfulService.deleteStorage(id);

		ArgumentCaptor<StorageData> captor = ArgumentCaptor.forClass(StorageData.class);
		verify(storageService, times(1)).deleteStorage(captor.capture());
		assertThat(captor.getValue().getId(), is(id));
	}

	@Test
	public void finalizeStorage() throws BusinessException {
		String id = "id";
		restfulService.finalizeStorage(id);

		ArgumentCaptor<StorageData> captor = ArgumentCaptor.forClass(StorageData.class);
		verify(storageService, times(1)).closeStorage(captor.capture());
		assertThat(captor.getValue().getId(), is(id));
	}

	@Test
	public void recordingStatus() {
		RecordingData recordingData = mock(RecordingData.class, Mockito.RETURNS_SMART_NULLS);
		when(storageService.getRecordingData()).thenReturn(recordingData);

		when(storageService.getRecordingState()).thenReturn(RecordingState.OFF);
		assertThat(restfulService.getRecordingState(), hasEntry("recordingState", (Object) RecordingState.OFF));
		verifyZeroInteractions(recordingData);

		when(storageService.getRecordingState()).thenReturn(RecordingState.ON);
		when(recordingData.getRecordEndDate()).thenReturn(new Date());
		Map<String, Object> result = restfulService.getRecordingState();
		assertThat(result, hasEntry("recordingState", (Object) RecordingState.ON));
		assertThat(result, hasKey("recordingStopDate"));

		when(storageService.getRecordingState()).thenReturn(RecordingState.SCHEDULED);
		when(recordingData.getRecordStartDate()).thenReturn(new Date());
		result = restfulService.getRecordingState();
		assertThat(result, hasEntry("recordingState", (Object) RecordingState.SCHEDULED));
		assertThat(result, hasKey("schduledStartDate"));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void startRecording() throws BusinessException {
		String id = "id";
		StorageData storageData = new StorageData();
		storageData.setId(id);
		when(storageService.getExistingStorages()).thenReturn(Collections.singletonList(storageData));
		restfulService.startOrScheduleRecording(id, 10L, 20L, true, true);

		ArgumentCaptor<RecordingProperties> propertiesCaptor = ArgumentCaptor.forClass(RecordingProperties.class);
		verify(storageService, times(1)).startOrScheduleRecording(eq(storageData), propertiesCaptor.capture());

		assertThat(propertiesCaptor.getValue().getStartDelay(), is(10L));
		assertThat(propertiesCaptor.getValue().getRecordDuration(), is(20L));
		assertThat(propertiesCaptor.getValue().isAutoFinalize(), is(true));
		assertThat(propertiesCaptor.getValue().getRecordingDataProcessors(), hasItem(is(InvocationExtractorDataProcessor.class)));

		restfulService.startOrScheduleRecording(id, 10L, 20L, false, false);
		verify(storageService, times(2)).startOrScheduleRecording(eq(storageData), propertiesCaptor.capture());

		assertThat(propertiesCaptor.getValue().isAutoFinalize(), is(false));
		assertThat(propertiesCaptor.getValue().getRecordingDataProcessors(), not(hasItem(is(InvocationExtractorDataProcessor.class))));
	}
}
