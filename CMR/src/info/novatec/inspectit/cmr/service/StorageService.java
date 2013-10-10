package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.StorageErrorCodeEnum;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageFileType;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.management.AbstractLabelManagementAction;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Storage service implementation.
 * 
 * @author Ivan Senic
 * 
 */
@Service
public class StorageService implements IStorageService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Storage manager.
	 */
	@Autowired
	private CmrStorageManager storageManager;

	/**
	 * Label data DAO.
	 */
	@Autowired
	private StorageDataDao storageLabelDataDao;

	/**
	 * Creates the new storage on the CMR with information given in {@link StorageData} object.
	 * 
	 * @param storageData
	 *            Information about new storage.
	 * @throws BusinessException
	 *             When storage creation fails.
	 */
	@MethodLog
	public void createStorage(StorageData storageData) throws BusinessException {
		try {
			storageManager.createStorage(storageData);
		} catch (SerializationException e) {
			throw new TechnicalException("Create the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Create the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * Opens an already existing storage in means that it prepares it for write.
	 * 
	 * @param storageData
	 *            Storage to open.
	 * @throws BusinessException
	 *             When storage with provided {@link StorageData} does not exists. When storage
	 *             opening fails.
	 */
	@MethodLog
	public void openStorage(StorageData storageData) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Open the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		try {
			storageManager.openStorage(storageData);
		} catch (SerializationException e) {
			throw new TechnicalException("Open the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Open the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData createAndOpenStorage(StorageData storageData) throws BusinessException {
		this.createStorage(storageData);
		this.openStorage(storageData);
		return storageData;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws BusinessException
	 */
	@MethodLog
	public void closeStorage(StorageData storageData) throws BusinessException {
		try {
			storageManager.closeStorage(storageData);
		} catch (SerializationException e) {
			throw new TechnicalException("Close the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Close the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteStorage(StorageData storageData) throws BusinessException {
		try {
			storageManager.deleteStorage(storageData);
		} catch (IOException e) {
			throw new TechnicalException("Delete the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public boolean isStorageOpen(StorageData storageData) {
		return storageManager.isStorageOpen(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<StorageData> getOpenedStorages() {
		return storageManager.getOpenedStorages();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<StorageData> getExistingStorages() {
		return storageManager.getExistingStorages();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<StorageData> getReadableStorages() {
		return storageManager.getReadableStorages();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public RecordingState getRecordingState() {
		return storageManager.getRecordingState();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData startOrScheduleRecording(StorageData storageData, RecordingProperties recordingProperties) throws BusinessException {
		if ((storageManager.getRecordingState() == RecordingState.ON || storageManager.getRecordingState() == RecordingState.SCHEDULED) && !storageData.equals(storageManager.getRecordingStorage())) {
			throw new BusinessException("Start or schedule recording on the storage " + storageData + ".", StorageErrorCodeEnum.CAN_NOT_START_RECORDING);
		} else if (storageManager.getRecordingState() == RecordingState.ON || storageManager.getRecordingState() == RecordingState.SCHEDULED) {
			throw new BusinessException("Start or schedule recording on the storage " + storageData + ".", StorageErrorCodeEnum.CAN_NOT_START_RECORDING);
		} else {
			try {
				storageManager.startOrScheduleRecording(storageData, recordingProperties);
				return storageManager.getRecordingStorage();
			} catch (SerializationException e) {
				throw new TechnicalException("Start or schedule recording on the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
			} catch (IOException e) {
				throw new TechnicalException("Start or schedule recording on the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void stopRecording() throws BusinessException {
		try {
			storageManager.stopRecording();
		} catch (SerializationException e) {
			throw new TechnicalException("Stop recording.", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Stop recording.", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	@Override
	public RecordingData getRecordingData() {
		if (storageManager.getRecordingState() == RecordingState.ON || storageManager.getRecordingState() == RecordingState.SCHEDULED) {
			RecordingData recordingData = new RecordingData();
			RecordingProperties recordingProperties = storageManager.getRecordingProperties();
			if (null != recordingProperties) {
				recordingData.setRecordStartDate(recordingProperties.getRecordStartDate());
				recordingData.setRecordEndDate(recordingProperties.getRecordEndDate());
			}
			recordingData.setRecordingStorage(storageManager.getRecordingStorage());
			recordingData.setRecordingWritingStatus(storageManager.getRecordingStatus());
			return recordingData;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void writeToStorage(StorageData storageData, Collection<DefaultData> defaultDataCollection, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously)
			throws BusinessException {
		if (!storageManager.isStorageOpen(storageData)) {
			throw new BusinessException("Write to the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_IS_NOT_OPENED);
		}
		try {
			storageManager.writeToStorage(storageData, defaultDataCollection, dataProcessors, synchronously);
		} catch (SerializationException e) {
			throw new TechnicalException("Write to the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Write to the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize) throws BusinessException {
		try {
			storageManager.copyBufferToStorage(storageData, platformIdents, dataProcessors, autoFinalize);
			return storageData;
		} catch (SerializationException e) {
			throw new TechnicalException("Copy buffer to the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Copy buffer to the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	@Override
	public StorageData copyDataToStorage(StorageData storageData, Collection<Long> elementIds, long platformIdent, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws BusinessException {
		try {
			storageManager.copyDataToStorage(storageData, elementIds, platformIdent, dataProcessors, autoFinalize);
			return storageData;
		} catch (SerializationException e) {
			throw new TechnicalException("Write to the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Write to the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getIndexFilesLocations(StorageData storageData) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Load index files locations for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.INDEX_FILE.getExtension());
		} catch (IOException e) {
			throw new TechnicalException("Load index files locations for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getDataFilesLocations(StorageData storageData) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Load data files locations for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.DATA_FILE.getExtension());
		} catch (IOException e) {
			throw new TechnicalException("Load data files locations for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getCachedDataFilesLocations(StorageData storageData) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Load cached files locations for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.CACHED_DATA_FILE.getExtension());
		} catch (IOException e) {
			throw new TechnicalException("Load cache files locations for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getAgentFilesLocations(StorageData storageData) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Load agent files locations for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.AGENT_FILE.getExtension());
		} catch (IOException e) {
			throw new TechnicalException("Load agent files locations for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public StorageData addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws BusinessException {
		try {
			storageManager.addLabelToStorage(storageData, storageLabel, doOverwrite);
			storageLabelDataDao.saveLabel(storageLabel);
			return storageManager.getStorageData(storageData.getId());
		} catch (SerializationException e) {
			throw new TechnicalException("Add a label to the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Add a label to the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public StorageData addLabelsToStorage(StorageData storageData, Collection<AbstractStorageLabel<?>> storageLabels, boolean doOverwrite) throws BusinessException {
		try {
			for (AbstractStorageLabel<?> storageLabel : storageLabels) {
				storageManager.addLabelToStorage(storageData, storageLabel, doOverwrite);
				storageLabelDataDao.saveLabel(storageLabel);
			}
			return storageManager.getStorageData(storageData.getId());
		} catch (SerializationException e) {
			throw new TechnicalException("Add labels to the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Add labels to the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public StorageData removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws BusinessException {
		try {
			storageManager.removeLabelFromStorage(storageData, storageLabel);
			return storageManager.getStorageData(storageData.getId());
		} catch (SerializationException e) {
			throw new TechnicalException("Remove a label from the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Remove a label from the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public StorageData removeLabelsFromStorage(StorageData storageData, List<AbstractStorageLabel<?>> storageLabelList) throws BusinessException {
		try {
			for (AbstractStorageLabel<?> label : storageLabelList) {
				storageManager.removeLabelFromStorage(storageData, label);
			}
			return storageManager.getStorageData(storageData.getId());
		} catch (SerializationException e) {
			throw new TechnicalException("Remove labels from the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Remove labels from the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void executeLabelManagementActions(Collection<AbstractLabelManagementAction> managementActions) throws BusinessException {
		for (AbstractLabelManagementAction managementAction : managementActions) {
			managementAction.execute(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Collection<AbstractStorageLabel<?>> getAllLabelsInStorages() {
		Set<AbstractStorageLabel<?>> labels = new HashSet<AbstractStorageLabel<?>>();
		for (StorageData storageData : getExistingStorages()) {
			labels.addAll(storageData.getLabelList());
		}
		return labels;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<AbstractStorageLabel<?>> getAllLabels() {
		return storageLabelDataDao.getAllLabels();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public <E> List<AbstractStorageLabel<E>> getLabelSuggestions(AbstractStorageLabelType<E> labeltype) {
		List<AbstractStorageLabel<E>> results = storageLabelDataDao.getAllLabelsForType(labeltype);
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void saveLabelToCmr(AbstractStorageLabel<?> storageLabel) {
		storageLabelDataDao.saveLabel(storageLabel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void saveLabelsToCmr(Collection<AbstractStorageLabel<?>> storageLabels) {
		for (AbstractStorageLabel<?> label : storageLabels) {
			storageLabelDataDao.saveLabel(label);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Transactional
	@MethodLog
	public void removeLabelFromCmr(AbstractStorageLabel<?> storageLabel, boolean removeFromStoragesAlso) throws BusinessException {
		storageLabelDataDao.removeLabel(storageLabel);
		if (removeFromStoragesAlso) {
			for (StorageData storageData : getExistingStorages()) {
				removeLabelFromStorage(storageData, storageLabel);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Transactional
	@MethodLog
	public void removeLabelsFromCmr(Collection<AbstractStorageLabel<?>> storageLabels, boolean removeFromStoragesAlso) throws BusinessException {
		storageLabelDataDao.removeLabels(storageLabels);
		if (removeFromStoragesAlso) {
			for (StorageData storageData : getExistingStorages()) {
				removeLabelsFromStorage(storageData, new ArrayList<AbstractStorageLabel<?>>(storageLabels));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void saveLabelType(AbstractStorageLabelType<?> labelType) {
		storageLabelDataDao.saveLabelType(labelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException {
		storageLabelDataDao.removeLabelType(labelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public <E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass) {
		return storageLabelDataDao.getLabelTypes(labelTypeClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<AbstractStorageLabelType<?>> getAllLabelTypes() {
		return storageLabelDataDao.getAllLabelTypes();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateStorageData(StorageData storageData) throws BusinessException {
		try {
			storageManager.updateStorageData(storageData);
		} catch (SerializationException e) {
			throw new TechnicalException("Update data for the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update data for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long getStorageQueuedWriteTaskCount(StorageData storageData) {
		return storageManager.getStorageQueuedWriteTaskCount(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void unpackUploadedStorage(IStorageData storageData) throws BusinessException {
		try {
			storageManager.unpackUploadedStorage(storageData);
		} catch (IOException e) {
			throw new TechnicalException("Un-pack uploaded data for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void createStorageFromUploadedDir(final IStorageData localStorageData) throws BusinessException {
		try {
			storageManager.createStorageFromUploadedDir(localStorageData);
		} catch (SerializationException e) {
			throw new TechnicalException("Create the storage " + localStorageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Create the storage " + localStorageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void cacheStorageData(StorageData storageData, Collection<? extends DefaultData> data, int hash) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Data caching for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		if (!storageManager.isStorageClosed(storageData)) {
			throw new BusinessException("Data caching for the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_IS_NOT_CLOSED);
		}
		try {
			storageManager.cacheStorageData(storageData, data, hash);
		} catch (SerializationException e) {
			throw new TechnicalException("Cache data for the storage " + storageData + ".", StorageErrorCodeEnum.SERIALIZATION_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Cache data for the storage " + storageData + ".", StorageErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public String getCachedStorageDataFileLocation(StorageData storageData, int hash) throws BusinessException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new BusinessException("Load cached storage data files locations for storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}
		return storageManager.getCachedStorageDataFileLocation(storageData, hash);
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Storage Service active...");
		}
	}

}
