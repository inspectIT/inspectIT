package info.novatec.inspectit.cmr.service.rest;

import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.cmr.service.rest.error.JsonError;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.processor.impl.DataAggregatorProcessor;
import info.novatec.inspectit.storage.processor.impl.DataSaverProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationClonerDataProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationExtractorDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Restful service provider for storages.
 * 
 * @author Ivan Senic
 * 
 */
@Controller
@RequestMapping(value = "/storage")
public class StorageRestfulService {

	/**
	 * Reference to the existing {@link IStorageService}.
	 */
	@Autowired
	IStorageService storageService;

	/**
	 * Handling of all the exceptions happening in this controller.
	 * 
	 * @param exception
	 *            Exception being thrown
	 * @return {@link ModelAndView}
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(Exception exception) {
		return new JsonError(exception).asModelAndView();
	}

	/**
	 * Returns all storages.
	 * <p>
	 * <i> Example URL: /storage/all</i>
	 * 
	 * @return List of all storages.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "all")
	@ResponseBody
	public List<StorageData> getAllStorages() {
		List<StorageData> storages = storageService.getExistingStorages();
		return storages;
	}

	/**
	 * Returns storage by ID.
	 * <p>
	 * <i> Example URL: /storage/get?id=1</i>
	 * 
	 * @param id
	 *            ID bounded from path.
	 * @return One storage or <code>null</code> if the storage with given ID does not exists.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "get")
	@ResponseBody
	public StorageData getStorageById(@RequestParam(value = "id", required = true) String id) {
		List<StorageData> storages = storageService.getExistingStorages();
		for (StorageData storageData : storages) {
			if (Objects.equals(id, storageData.getId())) {
				return storageData;
			}
		}
		return null;
	}

	/**
	 * Creates a new storage with given name.
	 * <p>
	 * <i> Example URL: /storage/create?name=ViaRest</i>
	 * 
	 * @param name
	 *            Name of the storage.
	 * @return Map containing message and created storage.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "create")
	@ResponseBody
	public Object createStorage(@RequestParam(value = "name", required = true) String name) throws StorageException {
		if (StringUtils.isEmpty(name)) {
			throw new StorageException("Can not create the storage when name is not provided.");
		}

		StorageData storageData = new StorageData();
		storageData.setName(name);
		storageData = storageService.createAndOpenStorage(storageData);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("message", "Storage successfully created.");
		resultMap.put("storage", storageData);
		return resultMap;
	}

	/**
	 * Finalize storage by ID.
	 * <p>
	 * <i> Example URL: /storage/finalize?id=1</i>
	 * 
	 * @param id
	 *            ID bounded from path.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "finalize")
	@ResponseBody
	public Object finalizeStorage(@RequestParam(value = "id", required = true) String id) throws StorageException {
		StorageData storageData = new StorageData();
		storageData.setId(id);
		storageService.closeStorage(storageData);
		return Collections.singletonMap("message", "Storage id " + id + " successfully finalized.");
	}

	/**
	 * Deletes storage by ID.
	 * <p>
	 * <i> Example URL: /storage/delete?id=1</i>
	 * 
	 * @param id
	 *            ID bounded from path.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "delete")
	@ResponseBody
	public Object deleteStorage(@RequestParam(value = "id", required = true) String id) throws StorageException {
		StorageData storageData = new StorageData();
		storageData.setId(id);
		storageService.deleteStorage(storageData);
		return Collections.singletonMap("message", "Storage id " + id + " successfully deleted.");
	}

	/**
	 * Returns the current state of the recording.
	 * <p>
	 * <i> Example URL: /storage/recording-state</i>
	 * 
	 * @return {@link RecordingState}.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "recording-state")
	@ResponseBody
	public Map<String, Object> getRecordingState() {
		RecordingState state = storageService.getRecordingState();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("recordingState", state);
		if (RecordingState.OFF != state) {
			// add recording storage
			RecordingData recordingData = storageService.getRecordingData();
			resultMap.put("recordingStorage", recordingData.getRecordingStorage());

			if (RecordingState.ON == state) {
				// add end date if we are having one
				Date recordingEndDate = recordingData.getRecordEndDate();
				if (null != recordingEndDate) {
					resultMap.put("recordingStopDate", DateFormat.getDateTimeInstance().format(recordingEndDate));
				}
			}

			// add start date if it's scheduled
			if (RecordingState.SCHEDULED == state) {
				resultMap.put("schduledStartDate", DateFormat.getDateTimeInstance().format(recordingData.getRecordStartDate()));
			}

		}
		return resultMap;
	}

	/**
	 * Stops recording.
	 * <p>
	 * <i> Example URL: /storage/stop-recording</i>
	 * 
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "stop-recording")
	@ResponseBody
	public Object stopRecording() throws StorageException {
		storageService.stopRecording();
		return Collections.singletonMap("message", "Recording stopped.");
	}

	/**
	 * Starts recording on the storage with the given ID with some advanced settings. This method
	 * allows specification of the start delay and recording duration in milliseconds. Zero values
	 * for that parameters are omitting them.
	 * <p>
	 * <i> Example URL: /storage/start-recording/?id=1&startDelay=30000&recordingDuration=60000
	 * (makes a 30s delay and records for 60s)</i>
	 * 
	 * @param id
	 *            Storage ID.
	 * @param startDelay
	 *            startDelay in milliseconds or <code>0</code> to ignore
	 * @param recordingDuration
	 *            recording duration in milliseconds or <code>0</code> to ignore
	 * @param extractInvocations
	 *            If invocations should be extracted.
	 * @param autoFinalize
	 *            If storage should be auto-finalized when recording is stopped.
	 * @return Map with informations for the user.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "start-recording")
	@ResponseBody
	public Object startOrScheduleRecording(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "startDelay", required = false) Long startDelay,
			@RequestParam(value = "recordingDuration", required = false) Long recordingDuration,
			@RequestParam(value = "extractInvocations", required = false, defaultValue = "true") Boolean extractInvocations,
			@RequestParam(value = "autoFinalize", required = false, defaultValue = "true") Boolean autoFinalize) throws StorageException {
		if (null == getStorageById(id)) {
			throw new StorageException("Storage with id " + id + " that is chosen for recording does not exists.");
		}

		StorageData storageData = new StorageData();
		storageData.setId(id);

		// extractInvocations and autoFinalize have default Values and should never be null
		RecordingProperties recordingProperties = getRecordingProperties(extractInvocations.booleanValue());
		recordingProperties.setAutoFinalize(autoFinalize.booleanValue());

		if (null != startDelay && startDelay.longValue() > 0) {
			recordingProperties.setStartDelay(startDelay.longValue());
		}

		if (null != recordingDuration && recordingDuration.longValue() > 0) {
			recordingProperties.setRecordDuration(recordingDuration.longValue());
		}

		StorageData recordingStorage = storageService.startOrScheduleRecording(storageData, recordingProperties);
		Map<String, Object> resultMap = new HashMap<>();
		if (recordingProperties.getStartDelay() > 0) {
			resultMap.put("message", "Recording scheduled.");
		} else {
			resultMap.put("message", "Recording started.");
		}
		resultMap.put("recordingStorage", recordingStorage);
		return resultMap;

	}

	/**
	 * Returns the recording properties with correctly set default set of
	 * {@link AbstractDataProcessor}s.
	 * 
	 * @param extractInvocations
	 *            If invocations should be extracted.
	 * @return {@link RecordingProperties}.
	 */
	private RecordingProperties getRecordingProperties(boolean extractInvocations) {
		RecordingProperties recordingProperties = new RecordingProperties();

		List<AbstractDataProcessor> normalProcessors = new ArrayList<AbstractDataProcessor>();

		// data saver
		List<Class<? extends DefaultData>> classesToSave = new ArrayList<Class<? extends DefaultData>>();
		Collections.addAll(classesToSave, InvocationSequenceData.class, HttpTimerData.class, ExceptionSensorData.class, MemoryInformationData.class, CpuInformationData.class,
				ClassLoadingInformationData.class, ThreadInformationData.class, SystemInformationData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(classesToSave, true);
		normalProcessors.add(dataSaverProcessor);

		// data aggregators
		normalProcessors.add(new DataAggregatorProcessor<TimerData>(TimerData.class, 5000, new TimerDataAggregator(), true));
		normalProcessors.add(new DataAggregatorProcessor<SqlStatementData>(SqlStatementData.class, 5000, new SqlStatementDataAggregator(true), true));

		// invocations support
		if (extractInvocations) {
			List<AbstractDataProcessor> chainedProcessorsForExtractor = new ArrayList<AbstractDataProcessor>();
			chainedProcessorsForExtractor.addAll(normalProcessors);
			InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedProcessorsForExtractor);
			normalProcessors.add(invocationExtractorDataProcessor);
		}
		normalProcessors.add(new InvocationClonerDataProcessor());

		recordingProperties.setRecordingDataProcessors(normalProcessors);

		return recordingProperties;
	}

}
