package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
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
	 * @throws StorageException
	 *             When storage creation fails.
	 */
	@MethodLog
	public void createStorage(StorageData storageData) throws StorageException {
		try {
			storageManager.createStorage(storageData);
		} catch (Exception e) {
			throw new StorageException("Exception occurred trying to create storage" + storageData + ".", e);
		}
	}

	/**
	 * Opens an already existing storage in means that it prepares it for write.
	 * 
	 * @param storageData
	 *            Storage to open.
	 * @throws StorageException
	 *             When storage with provided {@link StorageData} does not exists. When storage
	 *             opening fails.
	 */
	@MethodLog
	public void openStorage(StorageData storageData) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exsist on the CMR.");
		}
		try {
			storageManager.openStorage(storageData);
		} catch (Exception e) {
			throw new StorageException("Exception occurred trying to open storage" + storageData + ".", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData createAndOpenStorage(StorageData storageData) throws StorageException {
		this.createStorage(storageData);
		this.openStorage(storageData);
		return storageData;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws StorageException
	 */
	@MethodLog
	public void closeStorage(StorageData storageData) throws StorageException {
		try {
			storageManager.closeStorage(storageData);
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to close storage" + storageData + ".", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteStorage(StorageData storageData) throws StorageException {
		try {
			storageManager.deleteStorage(storageData);
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to delete storage" + storageData + ".", e);
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
	public StorageData startOrScheduleRecording(StorageData storageData, RecordingProperties recordingProperties) throws StorageException {
		if ((storageManager.getRecordingState() == RecordingState.ON || storageManager.getRecordingState() == RecordingState.SCHEDULED) && !storageData.equals(storageManager.getRecordingStorage())) {
			throw new StorageException("Recording is already active/scheduled with different storage. Only one storage at time can be chosen as recording destination.");
		} else if (storageManager.getRecordingState() == RecordingState.ON || storageManager.getRecordingState() == RecordingState.SCHEDULED) {
			throw new StorageException("Recording is already active/scheduled with selected storage.");
		} else {
			try {
				storageManager.startOrScheduleRecording(storageData, recordingProperties);
				return storageManager.getRecordingStorage();
			} catch (IOException | SerializationException e) {
				throw new StorageException("Exception occurred trying to start recording on storage " + storageData + ".", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void stopRecording() throws StorageException {
		try {
			storageManager.stopRecording();
		} catch (Exception e) {
			throw new StorageException("Exception occurred trying to stop recording.", e);
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
	public void writeToStorage(StorageData storageData, Collection<DefaultData> defaultDataCollection, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously) throws StorageException {
		if (!storageManager.isStorageOpen(storageData)) {
			throw new StorageException("Writing to storage tried to be performed on the storage that is not opened. Please open the storage first.");
		}
		try {
			storageManager.writeToStorage(storageData, defaultDataCollection, dataProcessors, synchronously);
		} catch (IOException | SerializationException e) {
			throw new StorageException("Copy Buffer to Storage action encountered an error.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize) throws StorageException {
		try {
			storageManager.copyBufferToStorage(storageData, platformIdents, dataProcessors, autoFinalize);
			return storageData;
		} catch (IOException | SerializationException e) {
			throw new StorageException("Copy Buffer to Storage action encountered an error.", e);
		}
	}

	@Override
	public StorageData copyDataToStorage(StorageData storageData, Collection<Long> elementIds, long platformIdent, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws StorageException {
		try {
			storageManager.copyDataToStorage(storageData, elementIds, platformIdent, dataProcessors, autoFinalize);
			return storageData;
		} catch (IOException | SerializationException e) {
			throw new StorageException("Copy Data to Storage action encountered an error.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getIndexFilesLocations(StorageData storageData) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR.");
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.INDEX_FILE.getExtension());
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to load storage index files locations.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getDataFilesLocations(StorageData storageData) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR.");
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.DATA_FILE.getExtension());
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to load storage data files locations.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getCachedDataFilesLocations(StorageData storageData) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR.");
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.CACHED_DATA_FILE.getExtension());
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to load storage cached data files locations.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<String, Long> getAgentFilesLocations(StorageData storageData) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR.");
		}
		try {
			return storageManager.getFilesHttpLocation(storageData, StorageFileType.AGENT_FILE.getExtension());
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to load storage agent files locations.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws StorageException {
		try {
			storageManager.addLabelToStorage(storageData, storageLabel, doOverwrite);
			storageLabelDataDao.saveLabel(storageLabel);
			return storageManager.getStorageData(storageData.getId());
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to save storage data changes to disk.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData addLabelsToStorage(StorageData storageData, Collection<AbstractStorageLabel<?>> storageLabels, boolean doOverwrite) throws StorageException {
		try {
			for (AbstractStorageLabel<?> storageLabel : storageLabels) {
				storageManager.addLabelToStorage(storageData, storageLabel, doOverwrite);
				storageLabelDataDao.saveLabel(storageLabel);
			}
			return storageManager.getStorageData(storageData.getId());
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to save storage data changes to disk.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws StorageException {
		try {
			storageManager.removeLabelFromStorage(storageData, storageLabel);
			return storageManager.getStorageData(storageData.getId());
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to save storage data changes to disk.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public StorageData removeLabelsFromStorage(StorageData storageData, List<AbstractStorageLabel<?>> storageLabelList) throws StorageException {
		try {
			for (AbstractStorageLabel<?> label : storageLabelList) {
				storageManager.removeLabelFromStorage(storageData, label);
			}
			return storageManager.getStorageData(storageData.getId());
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to save storage data changes to disk.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void executeLabelManagementActions(Collection<AbstractLabelManagementAction> managementActions) throws StorageException {
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
	@MethodLog
	public void saveLabelToCmr(AbstractStorageLabel<?> storageLabel) {
		storageLabelDataDao.saveLabel(storageLabel);
	}

	/**
	 * {@inheritDoc}
	 */
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
	@MethodLog
	public void removeLabelFromCmr(AbstractStorageLabel<?> storageLabel, boolean removeFromStoragesAlso) throws StorageException {
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
	@MethodLog
	public void removeLabelsFromCmr(Collection<AbstractStorageLabel<?>> storageLabels, boolean removeFromStoragesAlso) throws StorageException {
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
	@MethodLog
	public void saveLabelType(AbstractStorageLabelType<?> labelType) {
		storageLabelDataDao.saveLabelType(labelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void removeLabelType(AbstractStorageLabelType<?> labelType) throws StorageException {
		try {
			storageLabelDataDao.removeLabelType(labelType);
		} catch (Exception e) {
			throw new StorageException("Label type was not removed", e);
		}
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
	public void updateStorageData(StorageData storageData) throws StorageException {
		try {
			storageManager.updateStorageData(storageData);
		} catch (IOException | SerializationException e) {
			throw new StorageException(e);
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
	public void unpackUploadedStorage(IStorageData storageData) throws StorageException {
		try {
			storageManager.unpackUploadedStorage(storageData);
		} catch (IOException e) {
			throw new StorageException("Exception occurred trying to check for imported storage.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void createStorageFromUploadedDir(final IStorageData localStorageData) throws StorageException {
		try {
			storageManager.createStorageFromUploadedDir(localStorageData);
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to create storage from uploaded local storage.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void cacheStorageData(StorageData storageData, Collection<? extends DefaultData> data, int hash) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR. Data caching is not possible.");
		}
		if (!storageManager.isStorageClosed(storageData)) {
			throw new StorageException("The storage " + storageData + " is still not finalized.  Data caching is not possible.");
		}
		try {
			storageManager.cacheStorageData(storageData, data, hash);
		} catch (IOException | SerializationException e) {
			throw new StorageException("Exception occurred trying to cache data fpr storage.", e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public String getCachedStorageDataFileLocation(StorageData storageData, int hash) throws StorageException {
		if (!storageManager.isStorageExisting(storageData)) {
			throw new StorageException("The storage " + storageData + " does not exist on the CMR. Cached data file can not be resolved.");
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
