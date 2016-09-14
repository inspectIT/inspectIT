package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.communication.data.cmr.RecordingData;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.SqlStatementDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataAggregatorProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataSaverProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationClonerDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationExtractorDataProcessor;
import rocks.inspectit.shared.cs.storage.recording.RecordingProperties;
import rocks.inspectit.shared.cs.storage.recording.RecordingState;

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
	 * <i> Example URL: /storage</i>
	 *
	 * @return List of all storages.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public List<StorageData> getAllStorages() {
		List<StorageData> storages = storageService.getExistingStorages();
		return storages;
	}

	/**
	 * Returns storage by ID.
	 * <p>
	 * <i> Example URL: /storage/{id}</i>
	 *
	 * @param id
	 *            ID bounded from path.
	 * @return One storage or <code>null</code> if the storage with given ID does not exists.
	 */
	@RequestMapping(method = GET, value = "{id}")
	@ResponseBody
	public StorageData getStorageById(@PathVariable String id) {
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
	 * <i> Example URL: /storage/{name}/create</i>
	 *
	 * @param name
	 *            Name of the storage.
	 * @return Map containing message and created storage.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 */
	@RequestMapping(method = GET, value = "{name}/create")
	@ResponseBody
	public Object createStorage(@PathVariable String name) throws BusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new BusinessException("Create a new storage via storage REST service.", StorageErrorCodeEnum.STORAGE_NAME_IS_NOT_PROVIDED);
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
	 * <i> Example URL: /storage/{id}/finalize</i>
	 *
	 * @param id
	 *            ID bounded from path.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = GET, value = "{id}/finalize")
	@ResponseBody
	public Object finalizeStorage(@PathVariable String id) throws BusinessException {
		StorageData storageData = new StorageData();
		storageData.setId(id);
		storageService.closeStorage(storageData);
		return Collections.singletonMap("message", "Storage id " + id + " successfully finalized.");
	}

	/**
	 * Deletes storage by ID.
	 * <p>
	 * <i> Example URL: /storage/{id}/delete</i>
	 *
	 * @param id
	 *            ID bounded from path.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = GET, value = "{id}/delete")
	@ResponseBody
	public Object deleteStorage(@PathVariable String id) throws BusinessException {
		StorageData storageData = new StorageData();
		storageData.setId(id);
		storageService.deleteStorage(storageData);
		return Collections.singletonMap("message", "Storage id " + id + " successfully deleted.");
	}

	/**
	 * Returns the current state of the recording.
	 * <p>
	 * <i> Example URL: /storage/state</i>
	 *
	 * @return {@link RecordingState}.
	 */
	@RequestMapping(method = GET, value = "state")
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
	 * <i> Example URL: /storage/stop</i>
	 *
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 * @return Message for the user.
	 */
	@RequestMapping(method = GET, value = "stop")
	@ResponseBody
	public Object stopRecording() throws BusinessException {
		storageService.stopRecording();
		return Collections.singletonMap("message", "Recording stopped.");
	}

	/**
	 * Starts recording on the storage with the given ID with some advanced settings. This method
	 * allows specification of the start delay and recording duration in milliseconds. Zero values
	 * for that parameters are omitting them.
	 * <p>
	 * <i> Example URL: /storage/start?id=1&startDelay=30000&recordingDuration=60000 (makes a 30s
	 * delay and records for 60s)</i>
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
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "start")
	@ResponseBody
	public Object startOrScheduleRecording(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "startDelay", required = false) Long startDelay,
			@RequestParam(value = "recordingDuration", required = false) Long recordingDuration,
			@RequestParam(value = "extractInvocations", required = false, defaultValue = "true") Boolean extractInvocations,
			@RequestParam(value = "autoFinalize", required = false, defaultValue = "true") Boolean autoFinalize) throws BusinessException {
		if (null == getStorageById(id)) {
			throw new BusinessException("Start or schedule recording on storage with ID=" + id + " via storage REST service.", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}

		StorageData storageData = new StorageData();
		storageData.setId(id);

		// extractInvocations and autoFinalize have default Values and should never be null
		RecordingProperties recordingProperties = getRecordingProperties(extractInvocations.booleanValue());
		recordingProperties.setAutoFinalize(autoFinalize.booleanValue());

		if ((null != startDelay) && (startDelay.longValue() > 0)) {
			recordingProperties.setStartDelay(startDelay.longValue());
		}

		if ((null != recordingDuration) && (recordingDuration.longValue() > 0)) {
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

		List<AbstractDataProcessor> normalProcessors = new ArrayList<>();

		// data saver
		List<Class<? extends DefaultData>> classesToSave = new ArrayList<>();
		Collections.addAll(classesToSave, InvocationSequenceData.class, HttpTimerData.class, ExceptionSensorData.class, MemoryInformationData.class, CpuInformationData.class,
				ClassLoadingInformationData.class, ThreadInformationData.class, SystemInformationData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(classesToSave, true);
		normalProcessors.add(dataSaverProcessor);

		// data aggregators
		normalProcessors.add(new DataAggregatorProcessor<>(TimerData.class, 5000, new TimerDataAggregator(), true));
		normalProcessors.add(new DataAggregatorProcessor<>(SqlStatementData.class, 5000, new SqlStatementDataAggregator(true), true));

		// invocations support
		if (extractInvocations) {
			List<AbstractDataProcessor> chainedProcessorsForExtractor = new ArrayList<>();
			chainedProcessorsForExtractor.addAll(normalProcessors);
			InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedProcessorsForExtractor);
			normalProcessors.add(invocationExtractorDataProcessor);
		}
		normalProcessors.add(new InvocationClonerDataProcessor());

		recordingProperties.setRecordingDataProcessors(normalProcessors);

		return recordingProperties;
	}

	/**
	 * Header information for swagger requests.
	 *
	 * @param response
	 *            Response information
	 */
	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
	}
}
