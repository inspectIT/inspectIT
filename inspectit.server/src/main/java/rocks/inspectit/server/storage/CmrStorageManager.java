package rocks.inspectit.server.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang.mutable.MutableObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.Input;

import rocks.inspectit.server.cache.IBuffer;
import rocks.inspectit.server.dao.StorageDataDao;
import rocks.inspectit.server.dao.impl.DefaultDataDaoImpl;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.version.VersionService;
import rocks.inspectit.shared.cs.cmr.service.IServerStatusService;
import rocks.inspectit.shared.cs.communication.data.cmr.WritingStatus;
import rocks.inspectit.shared.cs.storage.IStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageData.StorageState;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.shared.cs.storage.StorageWriter;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.TimeFrameDataProcessor;
import rocks.inspectit.shared.cs.storage.recording.RecordingProperties;
import rocks.inspectit.shared.cs.storage.recording.RecordingState;
import rocks.inspectit.shared.cs.storage.util.CopyMoveFileVisitor;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

/**
 * Storage manager for the CMR. Manages creation, opening and closing of storages, as well as
 * recording.
 *
 * @author Ivan Senic
 *
 */
@Component
public class CmrStorageManager extends StorageManager implements ApplicationListener<ContextClosedEvent> { // NOPMD

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * The fixed rate of the refresh rate for gathering the statistics.
	 */
	private static final int UPDATE_RATE = 30000;

	/**
	 * {@link DefaultDataDaoImpl}.
	 */
	@Autowired
	StorageDataDao storageDataDao;

	/**
	 * Buffer for dealing with copy from buffer action.
	 */
	@Autowired
	IBuffer<DefaultData> buffer;

	/**
	 * {@link StorageData} for currently active recorder.
	 */
	private volatile StorageData recorderStorageData = null;

	/**
	 * {@link StorageWriter} provider.
	 */
	@Autowired
	CmrStorageWriterProvider storageWriterProvider;

	/**
	 * Opened storages and their writers.
	 */
	private Map<StorageData, StorageWriter> openedStoragesMap = new ConcurrentHashMap<>(8, 0.75f, 1);

	/**
	 * Existing storages.
	 */
	private Set<StorageData> existingStoragesSet;

	/**
	 * {@link StorageRecorder} to deal with recording.
	 */
	@Autowired
	CmrStorageRecorder storageRecorder;

	/**
	 * {@link IServerStatusService}.
	 */
	@Autowired
	IServerStatusService serverStatusService;

	/**
	 * {@link VersionService}.
	 */
	@Autowired
	VersionService versionService;

	/**
	 * Stores the current cmr version read from the versionService.
	 */
	private String cmrVersion;

	/**
	 * Creates new storage.
	 *
	 * @param storageData
	 *            Storage.
	 * @throws IOException
	 *             if {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws BusinessException
	 *             If name for storage is not provided.
	 */
	public void createStorage(StorageData storageData) throws IOException, SerializationException, BusinessException {
		if (null == storageData.getName()) {
			throw new BusinessException("Create new storage.", StorageErrorCodeEnum.STORAGE_NAME_IS_NOT_PROVIDED);
		}
		storageData.setId(getRandomUUIDString());
		storageData.setCmrVersion(cmrVersion);
		writeStorageDataToDisk(storageData);
		existingStoragesSet.add(storageData);
	}

	/**
	 * Opens existing storage if it is not already opened.
	 *
	 * @param storageData
	 *            Storage to open.
	 * @return {@link StorageWriter} created for this storage. Of <code>null</code> if no new writer
	 *         is created.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If exception occurs during update of storage data.
	 * @throws BusinessException
	 *             If provided storage data does not exist or if the storage is closed.
	 */
	public StorageWriter openStorage(StorageData storageData) throws IOException, SerializationException, BusinessException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			if (isStorageClosed(local)) {
				throw new BusinessException("Open the storage " + local + ".", StorageErrorCodeEnum.STORAGE_ALREADY_CLOSED);
			}
			if (!isStorageOpen(local)) {
				local.markOpened();
				StorageWriter writer = storageWriterProvider.getCmrStorageWriter();
				openedStoragesMap.put(local, writer);
				writer.prepareForWrite(local);
				writeStorageDataToDisk(local);
				return writer;
			}
		}
		return null;
	}

	/**
	 * Closes the storage if it is open.
	 *
	 * @param storageData
	 *            Storage.
	 * @throws BusinessException
	 *             When storage that should be closed is used for recording or it is already closed.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void closeStorage(StorageData storageData) throws BusinessException, IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			if ((storageRecorder.isRecordingOn() || storageRecorder.isRecordingScheduled()) && Objects.equals(local, recorderStorageData)) {
				throw new BusinessException("Close the storage " + local + ".", StorageErrorCodeEnum.STORAGE_CAN_NOT_BE_CLOSED);
			} else if (isStorageClosed(local)) {
				throw new BusinessException("Close the storage " + local + ".", StorageErrorCodeEnum.STORAGE_ALREADY_CLOSED);
			}

			StorageWriter writer = openedStoragesMap.get(local);
			if (writer != null) {
				writer.closeStorageWriter();
			}
			openedStoragesMap.remove(local);
			local.setDiskSize(getDiskSizeForStorage(local));
			local.markClosed();
			writeStorageDataToDisk(local);
		}
	}

	/**
	 * Deletes a storage information and files from disk.
	 *
	 * @param storageData
	 *            {@link StorageData} to delete.
	 * @throws BusinessException
	 *             If storage is not closed.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void deleteStorage(StorageData storageData) throws BusinessException, IOException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			if ((storageRecorder.isRecordingOn() || storageRecorder.isRecordingScheduled()) && Objects.equals(local, recorderStorageData)) {
				throw new BusinessException("Delete the storage " + local + ".", StorageErrorCodeEnum.STORAGE_ALREADY_CLOSED);
			}
			if (local.isStorageOpened()) {
				StorageWriter writer = openedStoragesMap.get(local);
				if (writer != null) {
					writer.cancel();
				}
				openedStoragesMap.remove(local);
			}
			deleteCompleteStorageDataFromDisk(local);
			existingStoragesSet.remove(local);
		}
	}

	/**
	 * If the recording is active, returns the storage that is used for storing recording data.
	 *
	 * @return Storage that is used for recording, or null if recording is not active.
	 */
	public StorageData getRecordingStorage() {
		return recorderStorageData;
	}

	/**
	 * Returns the properties used for the current recording on the CMR.
	 *
	 * @return {@link RecordingProperties} that are used for recording, or null if recording is not
	 *         active.
	 */
	public RecordingProperties getRecordingProperties() {
		return storageRecorder.getRecordingProperties();
	}

	/**
	 * Returns the recording state.
	 *
	 * @return Returns the recording state.
	 * @See {@link RecordingState}
	 */
	public RecordingState getRecordingState() {
		return storageRecorder.getRecordingState();
	}

	/**
	 * Returns the {@link WritingStatus} for the storage that is currently used as a recording one
	 * or <code>null</code> if the recording is not active.
	 *
	 * @return {@link WritingStatus} if recording is active. <code>Null</code> otherwise.
	 */
	public WritingStatus getRecordingStatus() {
		StorageWriter recordingStorageWriter = storageRecorder.getStorageWriter();
		if (null != recordingStorageWriter) {
			return recordingStorageWriter.getWritingStatus();
		} else {
			return null;
		}
	}

	/**
	 * Starts recording on the provided storage if recording is not active. If storage is not
	 * created it will be. If it is not open, it will be.
	 *
	 * @param storageData
	 *            Storage.
	 * @param recordingProperties
	 *            Recording properties. Must not be null.
	 * @throws IOException
	 *             If {@link IOException} occurs while creating and opening the storage.
	 * @throws SerializationException
	 *             If serialization fails when creating the storage.
	 * @throws BusinessException
	 *             If recording can not be started for some reason.
	 */
	public void startOrScheduleRecording(StorageData storageData, RecordingProperties recordingProperties) throws IOException, SerializationException, BusinessException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}
		synchronized (this) {
			if (!storageRecorder.isRecordingOn() && !storageRecorder.isRecordingScheduled()) {
				StorageWriter storageWriter = openedStoragesMap.remove(local);
				storageRecorder.startOrScheduleRecording(storageWriter, recordingProperties);
				recorderStorageData = local;
				recorderStorageData.markRecording();
				writeStorageDataToDisk(recorderStorageData);
			}
		}
	}

	/**
	 * Stops recording.
	 *
	 * @throws SerializationException
	 *             If serialization fails during write {@link StorageData} to disk.
	 * @throws IOException
	 *             If IOException occurs during write {@link StorageData} to disk.
	 * @throws BusinessException
	 *             If {@link BusinessException} is throw during auto-finalize process.
	 */
	public void stopRecording() throws IOException, SerializationException, BusinessException {
		synchronized (this) {
			if (storageRecorder.isRecordingOn() || storageRecorder.isRecordingScheduled()) {
				boolean autoFinalize = storageRecorder.getRecordingProperties().isAutoFinalize();
				StorageWriter storageWriter = storageRecorder.getStorageWriter();
				storageRecorder.stopRecording();
				recorderStorageData.markOpened();
				openedStoragesMap.put(recorderStorageData, storageWriter);
				if (autoFinalize) {
					this.closeStorage(recorderStorageData);
				}
				writeStorageDataToDisk(recorderStorageData);
				recorderStorageData = null; // NOPMD
			}
		}
	}

	/**
	 * Writes one data to the recording storage.
	 *
	 * @param dataToRecord
	 *            Data to write.
	 */
	public void record(DefaultData dataToRecord) {
		if (storageRecorder.isRecordingOn() && canWriteMore()) {
			storageRecorder.record(dataToRecord);
		} else if (storageRecorder.isRecordingOn() && !canWriteMore()) {
			try {
				stopRecording();
			} catch (Exception e) {
				log.warn("Exception occurred trying to automatically stop the recording due to the hard disk space limitation warning.", e);
			}
		}
	}

	/**
	 * Writes collection of {@link DefaultData} objects to the storage.
	 *
	 * @param storageData
	 *            Storage to write.
	 * @param dataToWrite
	 *            Data to write.
	 * @param dataProcessors
	 *            Processors that will be used for data writing. Can be null. In this case, the
	 *            direct write is done.
	 * @param synchronously
	 *            If write will be done synchronously or not.
	 * @throws BusinessException
	 *             If storage is used as a recording storage.
	 * @throws SerializationException
	 *             If serialization fails during auto-finalization.
	 * @throws IOException
	 *             If {@link IOException} occurs during auto-finalization.
	 */
	public void writeToStorage(StorageData storageData, Collection<? extends DefaultData> dataToWrite, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously)
			throws BusinessException, IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		StorageWriter writer = openedStoragesMap.get(local);
		if (writer != null) {
			if (synchronously) {
				writer.processSynchronously(dataToWrite, dataProcessors);
			} else {
				writer.process(dataToWrite, dataProcessors);
			}
		} else if (Objects.equals(local, recorderStorageData)) {
			throw new BusinessException("Write data to storage " + local + ".", StorageErrorCodeEnum.WRITE_FAILED);
		} else if (local.getState() == StorageState.CLOSED) {
			throw new BusinessException("Write data to storage " + local + ".", StorageErrorCodeEnum.STORAGE_ALREADY_CLOSED);
		} else {
			log.error("Writer for the not closed storage " + local + " is not available.");
			throw new RuntimeException("Writer for the not closed storage " + local + " is not available.");
		}
	}

	/**
	 * Copies the content of the current CMR buffer to the Storage.
	 *
	 * @param storageData
	 *            Storage to copy data to.
	 * @param platformIdents
	 *            List of agent IDs.
	 * @param dataProcessors
	 *            Processors that will be used for data writing.
	 * @param autoFinalize
	 *            If the storage where action is performed should be auto-finalized after the write.
	 * @throws BusinessException
	 *             If storage is used as a recording storage.
	 * @throws SerializationException
	 *             If storage needs to be created, and serialization fails.
	 * @throws IOException
	 *             If IO exception occurs.
	 */
	public void copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws BusinessException, IOException, SerializationException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}

		DefaultData oldestBufferElement = buffer.getOldestElement();
		// only copy if we have smth in the buffer
		if (null != oldestBufferElement) {

			// oldest date is the buffer oldest date, we don't include data from before
			Date fromDate = new Date(oldestBufferElement.getTimeStamp().getTime());
			Date toDate = null;

			// check if we have the time-frame limit
			for (AbstractDataProcessor dataProcessor : dataProcessors) {
				if (dataProcessor instanceof TimeFrameDataProcessor) {
					TimeFrameDataProcessor timeFrameDataProcessor = (TimeFrameDataProcessor) dataProcessor;
					// update dates
					if (timeFrameDataProcessor.getFromDate().after(fromDate)) {
						fromDate = timeFrameDataProcessor.getFromDate();
					}
					toDate = timeFrameDataProcessor.getToDate();
					break;
				}
			}

			for (Long platformId : platformIdents) {
				List<DefaultData> toWriteList = storageDataDao.getAllDefaultDataForAgent(platformId.longValue(), fromDate, toDate);
				this.writeToStorage(local, toWriteList, dataProcessors, true);
			}
		}

		if (autoFinalize) {
			this.closeStorage(local);
		}
		updateExistingStorageSize(local);
	}

	/**
	 * Copies set of template data to storage. The storage does not have to be opened before action
	 * can be executed (storage will be created/opened first in this case)
	 *
	 * @param storageData
	 *            {@link StorageData} to copy to.
	 * @param elementIds
	 *            IDs of the elements to be saved.
	 * @param platformIdent
	 *            Platform ident elements belong to.
	 * @param traceIds
	 *            Set of trace ids to include when saving. This includes all spans and invocations
	 *            related to traceId.
	 * @param dataProcessors
	 *            Processors to process the data. Can be null, then the data is only copied with no
	 *            processing.
	 * @param autoFinalize
	 *            If the storage where action is performed should be auto-finalized after the write.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails when storage needs to be created/opened.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 */
	public void copyDataToStorage(StorageData storageData, Collection<Long> elementIds, long platformIdent, Set<Long> traceIds, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws IOException, SerializationException, BusinessException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}

		// use set to avoid duplicated data
		Set<DefaultData> toWriteList = new HashSet<>();

		if (CollectionUtils.isNotEmpty(elementIds)) {
			List<DefaultData> data = storageDataDao.getDataFromIdList(elementIds, platformIdent);
			toWriteList.addAll(data);
		}

		if (CollectionUtils.isNotEmpty(traceIds)) {
			List<DefaultData> data = storageDataDao.getDataForTraceIdList(traceIds);
			toWriteList.addAll(data);
		}

		// write only one time
		this.writeToStorage(local, toWriteList, dataProcessors, true);

		if (autoFinalize) {
			this.closeStorage(local);
		}
		updateExistingStorageSize(local);
	}

	/**
	 * Closes all opened storages. This method should only be called when the CMR shutdown hook is
	 * activated to ensure that no data is lost.
	 *
	 * @throws SerializationException
	 * @throws IOException
	 */
	protected void closeAllStorages() {
		if (storageRecorder.isRecordingOn() || storageRecorder.isRecordingScheduled()) {
			try {
				stopRecording();
			} catch (Exception e) {
				log.warn("Recording storage could not be finalized during the CMR shut-down.", e);

			}
		}
		for (StorageData openedStorage : openedStoragesMap.keySet()) {
			try {
				this.closeStorage(openedStorage);
			} catch (Exception e) {
				log.warn("Storage " + openedStorage + " could not be finalized during the CMR shut-down.", e);
			}
		}
	}

	/**
	 * Returns the storage data based on the ID. This method can be helpful when the updated version
	 * of {@link StorageData} needs to be retrieved.
	 *
	 * @param id
	 *            ID of storage.
	 * @return {@link StorageData}
	 */
	public StorageData getStorageData(String id) {
		for (StorageData storageData : existingStoragesSet) {
			if (storageData.getId().equals(id)) {
				return storageData;
			}
		}
		return null;
	}

	/**
	 * Returns list of existing storages.
	 *
	 * @return Returns list of existing storages.
	 */
	public List<StorageData> getExistingStorages() {
		List<StorageData> list = new ArrayList<>();
		list.addAll(existingStoragesSet);
		return list;
	}

	/**
	 * Returns list of opened storages.
	 *
	 * @return Returns list of opened storages.
	 */
	public List<StorageData> getOpenedStorages() {
		List<StorageData> list = new ArrayList<>();
		list.addAll(openedStoragesMap.keySet());
		return list;
	}

	/**
	 * Returns list of readable storages.
	 *
	 * @return Returns list of readable storages.
	 */
	public List<StorageData> getReadableStorages() {
		List<StorageData> list = new ArrayList<>();
		for (StorageData storageData : existingStoragesSet) {
			if (storageData.isStorageClosed()) {
				list.add(storageData);
			}
		}
		return list;
	}

	/**
	 * Returns if the storage is opened, and thus if the write to the storage can be executed.
	 *
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage is opened, otherwise false.
	 */
	public boolean isStorageOpen(StorageData storageData) {
		for (StorageData existing : openedStoragesMap.keySet()) {
			if (existing.getId().equals(storageData.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the storage is existing.
	 *
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage exists, otherwise false.
	 */
	public boolean isStorageExisting(StorageData storageData) {
		for (StorageData existing : existingStoragesSet) {
			if (existing.getId().equals(storageData.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the storage is closed.
	 *
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage is closed, in any other situation false.
	 */
	public boolean isStorageClosed(StorageData storageData) {
		for (StorageData existing : existingStoragesSet) {
			if (existing.getId().equals(storageData.getId())) {
				return existing.isStorageClosed();
			}
		}
		return false;
	}

	/**
	 * Returns the amount of writing tasks storage still has to process. Note that this is an
	 * approximate number.
	 *
	 * @param storageData
	 *            Storage data to get information for.
	 * @return Returns number of queued tasks. Note that if the storage is not in writable mode
	 *         <code>0</code> will be returned.
	 */
	public long getStorageQueuedWriteTaskCount(StorageData storageData) {
		try {
			StorageData local = getLocalStorageDataObject(storageData);
			if (!isStorageOpen(local)) {
				return 0;
			}

			StorageWriter storageWriter = openedStoragesMap.get(local);
			if (null == storageWriter) {
				return 0;
			} else {
				return storageWriter.getQueuedTaskCount();
			}
		} catch (BusinessException e) {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getStoragePath(IStorageData storageData) {
		return getDefaultStorageDirPath().resolve(storageData.getStorageFolder());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Path getDefaultStorageDirPath() {
		return Paths.get(getStorageDefaultFolder()).toAbsolutePath();
	}

	/**
	 * Returns list of files paths with given extension for a storage in HTTP form.
	 *
	 * @param storageData
	 *            Storage.
	 * @param extension
	 *            Files extension.
	 * @return Returns the map containing pair with file name with given extension for a storage in
	 *         HTTP form and size of each file.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public Map<String, Long> getFilesHttpLocation(StorageData storageData, final String extension) throws IOException {
		Path storagePath = getStoragePath(storageData);
		if ((storagePath == null) || !Files.isDirectory(storagePath)) {
			return Collections.emptyMap();
		}

		final List<Path> filesPaths = new ArrayList<>();
		Files.walkFileTree(storagePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(extension)) {
					filesPaths.add(file);
				}
				return super.visitFile(file, attrs);
			}
		});

		Map<String, Long> result = new HashMap<>();
		for (Path path : filesPaths) {
			result.put(getPathAsHttp(path), Files.size(path));
		}

		return result;
	}

	/**
	 * Add a label to the storage and saves new state of the storage to the disk.
	 *
	 * @param storageData
	 *            {@link StorageData}.
	 * @param storageLabel
	 *            Label to add.
	 * @param doOverwrite
	 *            Overwrite if label type already exists and is only one per storage allowed.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 * @throws SerializationException
	 *             If {@link SerializationException} happens.
	 * @throws BusinessException
	 *             If provided storage data does not exist.
	 */
	public void addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws IOException, SerializationException, BusinessException {
		StorageData local = getLocalStorageDataObject(storageData);
		if (null != local) {
			local.addLabel(storageLabel, doOverwrite);
			writeStorageDataToDisk(local);
		}
	}

	/**
	 * Removes label from storage and saves new state of the storage data to the disk.
	 *
	 * @param storageData
	 *            {@link StorageData}.
	 * @param storageLabel
	 *            Label to remove.
	 * @return True if the label was removed, false otherwise.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 * @throws SerializationException
	 *             If {@link SerializationException} happens.
	 * @throws BusinessException
	 *             If provided storage data does not exist.
	 */
	public boolean removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws IOException, SerializationException, BusinessException {
		StorageData local = getLocalStorageDataObject(storageData);
		boolean removed = local.removeLabel(storageLabel);
		writeStorageDataToDisk(local);
		return removed;
	}

	/**
	 * Updates the storage data for already existing storage.
	 *
	 * @param storageData
	 *            Storage data containing update values.
	 * @throws BusinessException
	 *             If storage does not exists.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If IO operation fails.
	 */
	public void updateStorageData(StorageData storageData) throws BusinessException, IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			local.setName(storageData.getName());
			local.setDescription(storageData.getDescription());
			writeStorageDataToDisk(local);
		}
	}

	/**
	 * Returns the status of the active storage writers. This can be used for logging purposes.
	 *
	 * @return Returns the status of the active storage writers.
	 */
	public Map<StorageData, String> getWritersStatus() {
		Map<StorageData, String> map = new HashMap<>();
		for (Map.Entry<StorageData, StorageWriter> entry : openedStoragesMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue().getExecutorServiceStatus());
		}
		if (storageRecorder.isRecordingOn()) {
			StorageData storageData = recorderStorageData;
			StorageWriter storageWriter = storageRecorder.getStorageWriter();
			if ((null != storageData) && (null != storageWriter)) {
				map.put(storageData, storageWriter.getExecutorServiceStatus());
			}
		}
		return map;
	}

	/**
	 * Updates the size of each existing storage, if it changed.
	 * <p>
	 * This method is called from a Spring configured job.
	 *
	 * @throws IOException
	 *             If {@link IOException} happened during operation.
	 * @throws SerializationException
	 *             If serialization failed.
	 */
	@Scheduled(fixedRate = UPDATE_RATE)
	protected void updateExistingStoragesSize() throws IOException, SerializationException {
		for (StorageData storageData : existingStoragesSet) {
			updateExistingStorageSize(storageData);
		}
	}

	/**
	 * Updates size of the given storage and saves information to this.
	 *
	 * @param storageData
	 *            Storage data.
	 * @throws IOException
	 *             If {@link IOException} happened during operation.
	 * @throws SerializationException
	 *             If serialization failed.
	 */
	private void updateExistingStorageSize(StorageData storageData) throws IOException, SerializationException {
		if (null != storageData) {
			synchronized (storageData) {
				long newSize = getDiskSizeForStorage(storageData);
				if (newSize != storageData.getDiskSize()) {
					storageData.setDiskSize(newSize);
					writeStorageDataToDisk(storageData);
				}
			}
		}
	}

	/**
	 * Checks for the uploaded files in the storage uploads folder and tries to extract data to the
	 * default storage folder.
	 *
	 * @param packedStorageData
	 *            Storage data that is packed in the file that needs to be unpacked.
	 *
	 * @throws IOException
	 *             IF {@link IOException} occurs during the file tree walk.
	 * @throws BusinessException
	 *             If there is not enough space for the unpacking the storage.
	 */
	public void unpackUploadedStorage(final IStorageData packedStorageData) throws IOException, BusinessException {
		long storageBytesLeft = getBytesHardDriveOccupancyLeft();
		if (packedStorageData.getDiskSize() > storageBytesLeft) {
			throw new BusinessException("Unpack the uploaded storage " + packedStorageData + ".", StorageErrorCodeEnum.LOW_DISK_SPACE);
		}

		Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			throw new IOException("Can not perform storage unpacking. The main upload path " + uploadPath.toString() + " does not exist.");
		} else {
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						// skip all other files
						if (!file.toString().endsWith(StorageFileType.ZIP_STORAGE_FILE.getExtension())) {
							return FileVisitResult.CONTINUE;
						}

						IStorageData storageData = getStorageDataFromZip(file);
						if (!Objects.equals(packedStorageData, storageData)) {
							// go to next file if the file that we found does not hold the correct
							// storage to unpack
							return FileVisitResult.CONTINUE;
						}

						if (null != storageData) {
							StorageData importedStorageData = new StorageData(storageData);
							if (existingStoragesSet.add(importedStorageData)) {
								printStorageCmrVersionWarn(storageData);

								unzipStorageData(file, getStoragePath(importedStorageData));
								Path localInformation = getStoragePath(importedStorageData).resolve(importedStorageData.getId() + StorageFileType.LOCAL_STORAGE_FILE.getExtension());
								Files.deleteIfExists(localInformation);
								writeStorageDataToDisk(importedStorageData);
							} else {
								log.info("Uploaded storage file " + file.toString() + " contains the storage that is already available on the CMR. File will be deleted.");
							}
						}
						Files.deleteIfExists(file);
					} catch (Exception e) {
						log.warn("Uploaded storage file " + file.toString() + " is not of correct type and can not be extracted. File will be deleted.", e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Creates a storage form the uploaded local storage directory.
	 *
	 * @param localStorageData
	 *            Local storage information.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 *             If there is not enough space for the unpacking the storage.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	public void createStorageFromUploadedDir(final IStorageData localStorageData) throws IOException, BusinessException, SerializationException {
		long storageBytesLeft = getBytesHardDriveOccupancyLeft();
		if (localStorageData.getDiskSize() > storageBytesLeft) {
			throw new BusinessException("Create the uploaded storage " + localStorageData + ".", StorageErrorCodeEnum.LOW_DISK_SPACE);
		}

		Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			throw new IOException("Can not perform storage unpacking. The main upload path " + uploadPath.toString() + " does not exist.");
		} else {
			final MutableObject storageUploadPath = new MutableObject();
			final MutableObject uploadedStorageData = new MutableObject();
			final ISerializer serializer = getSerializationManagerProvider().createSerializer();
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					// skip all other files, search for the local data
					if (!file.toString().endsWith(localStorageData.getId() + StorageFileType.LOCAL_STORAGE_FILE.getExtension())) {
						return FileVisitResult.CONTINUE;
					}

					// when found confirm it is the one we wanted to upload
					InputStream inputStream = null;
					Input input = null;
					try {
						inputStream = Files.newInputStream(file, StandardOpenOption.READ);
						input = new Input(inputStream);
						Object deserialized = serializer.deserialize(input);
						if (Objects.equals(deserialized, localStorageData)) {
							uploadedStorageData.setValue(new StorageData(localStorageData));
							storageUploadPath.setValue(file.toAbsolutePath().getParent());
							return FileVisitResult.TERMINATE;
						}
					} catch (SerializationException e) {
						log.warn("Error de-serializing local storage file.", e);
					} finally {
						if (null != input) {
							input.close();
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});

			// do the rest out of the file walk
			Path parentDir = (Path) storageUploadPath.getValue();
			StorageData storageData = (StorageData) uploadedStorageData.getValue();
			if ((null != storageData) && (null != parentDir)) {
				Path storageDir = getStoragePath(storageData);
				if (existingStoragesSet.add(storageData)) {
					if (Files.notExists(storageDir)) {
						printStorageCmrVersionWarn(storageData);

						Files.walkFileTree(parentDir, new CopyMoveFileVisitor(parentDir, storageDir, true));
						Path localInformation = getStoragePath(storageData).resolve(storageData.getId() + StorageFileType.LOCAL_STORAGE_FILE.getExtension());
						Files.deleteIfExists(localInformation);
						writeStorageDataToDisk(storageData);
					} else {
						throw new IOException("Directory to place uploaded storage already exists.");
					}
				} else {
					log.info("Uploaded storage on path " + parentDir.toString() + " contains the storage that is already available on the CMR. Dir will be deleted.");
					Files.walkFileTree(parentDir, new DeleteFileVisitor());
				}
			}
		}
	}

	/**
	 * Returns location of the file where the cached data for given storage and hash is cached.
	 * Returns <code>null</code> if no data is cached for given storage and hash.
	 * <p>
	 * The path is in form "/directory/file.extension". The path can be used in combination to CMR's
	 * ip and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the ip localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 *
	 * @param storageData
	 *            Storage
	 * @param hash
	 *            Hash that was used for caching.
	 * @return Returns location of the file where the cached data for given storage and hash is
	 *         cached. Returns <code>null</code> if no data is cached for given storage and hash.
	 */
	public String getCachedStorageDataFileLocation(StorageData storageData, int hash) {
		Path path = super.getCachedDataPath(storageData, hash);
		if (Files.exists(path)) {
			return getPathAsHttp(path);
		} else {
			return null;
		}
	}

	/**
	 * Returns path in the storage folder that can be used in HTTP requests.
	 * <p>
	 * Note that for Jetty, root folder to deliver files is /storage/ thus path must be relative
	 * from it.
	 *
	 * @param path
	 *            Path to convert.
	 * @return String that attached to server ip and port dentes HTTP location of file.
	 */
	private String getPathAsHttp(Path path) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Path pathPart : getDefaultStorageDirPath().relativize(path)) {
			stringBuilder.append('/');
			stringBuilder.append(pathPart.toString());
		}
		return stringBuilder.toString();
	}

	/**
	 * Returns the size of the storage on disk.
	 *
	 * @param storageData
	 *            Storage.
	 * @return Size of storage on disk, or 0 if {@link IOException} occurs during calculations.
	 */
	private long getDiskSizeForStorage(StorageData storageData) {
		Path storageDir = getStoragePath(storageData);
		try {
			final MutableLong size = new MutableLong(0);
			Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					size.add(attrs.size());
					return super.visitFile(file, attrs);
				}
			});
			return size.longValue();
		} catch (IOException e) {
			return 0;
		}
	}

	/**
	 * Returns the local cached object that represent the {@link StorageData}.
	 *
	 * @param storageData
	 *            Template.
	 * @return Local object.
	 * @throws BusinessException
	 *             If local object can not be found.
	 */
	private StorageData getLocalStorageDataObject(StorageData storageData) throws BusinessException {
		for (StorageData existing : existingStoragesSet) {
			if (existing.getId().equals(storageData.getId())) {
				return existing;
			}
		}
		throw new BusinessException("Find storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
	}

	/**
	 * Loads all existing storages by walking through the default storage directory.
	 */
	private void loadAllExistingStorages() {
		existingStoragesSet = Collections.newSetFromMap(new ConcurrentHashMap<StorageData, Boolean>());

		Path defaultDirectory = Paths.get(getStorageDefaultFolder());
		if (!Files.isDirectory(defaultDirectory)) {
			return;
		}

		final ISerializer serializer = getSerializationManagerProvider().createSerializer();
		try {
			Files.walkFileTree(defaultDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(StorageFileType.STORAGE_FILE.getExtension())) {

						InputStream inputStream = null;
						Input input = null;
						try {
							inputStream = Files.newInputStream(file, StandardOpenOption.READ);
							input = new Input(inputStream);
							Object deserialized = serializer.deserialize(input);
							if (deserialized instanceof StorageData) {
								StorageData storageData = (StorageData) deserialized;
								// do not add any corrupted storages
								if (storageData.getState() == StorageState.CLOSED) {
									printStorageCmrVersionWarn(storageData);
									existingStoragesSet.add(storageData);
								}
							}
						} catch (IOException e) {
							log.error("Error reading existing storage data file. File path: " + file.toString() + ".", e);
						} catch (SerializationException e) {
							log.error("Error deserializing existing storage binary data in file:" + file.toString() + ".", e);
						} finally {
							if (null != input) {
								input.close();
							}
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring default storage directory. Directory path: " + defaultDirectory.toString() + ".", e);
		}
	}

	/**
	 * Clears the upload folder.
	 */
	private void clearUploadFolder() {
		final Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			return;
		}

		try {
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (dir.equals(uploadPath)) {
						return FileVisitResult.CONTINUE;
					}

					if (null == exc) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}

			});
		} catch (IOException e) {
			log.warn("Could not delete the storage upload folder on the start-up.", e);
		}
	}

	/**
	 * Returns the unique String that will be used as a StorageData ID. This ID needs to be unique
	 * not only for the current CMR, but we need to ensure that is unique for all CMRs, because the
	 * correlation between storage and CMR will be done by this ID.
	 *
	 * @return Returns unique string based on the {@link UUID}.
	 */
	private String getRandomUUIDString() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Prints the warnings if the CMR version saved in the storage does not exists or is different
	 * from the current CMR version.
	 *
	 * @param storageData
	 *            {@link StorageData}.
	 */
	private void printStorageCmrVersionWarn(IStorageData storageData) {
		// inform if the version of the CMR differs or is not available
		if (null == storageData.getCmrVersion()) {
			log.warn("The storage " + storageData + " does not define the CMR version. The storage might be unstable on the CMR version " + cmrVersion + ".");
		} else if (!Objects.equals(storageData.getCmrVersion(), cmrVersion)) {
			log.warn(
					"The storage " + storageData + " has different CMR version (" + storageData.getCmrVersion() + ") than the current CMR version(" + cmrVersion + "). The storage might be unstable.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		cmrVersion = versionService.getVersionAsString();
		loadAllExistingStorages();
		updatedStorageSpaceLeft();
		clearUploadFolder();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Close all storage on context closing.
	 */
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		closeAllStorages();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("existingStoragesSet", existingStoragesSet);
		toStringBuilder.append("openedStoragesMap", openedStoragesMap);
		toStringBuilder.append("storageRecorder", storageRecorder);
		return toStringBuilder.toString();
	}

}
