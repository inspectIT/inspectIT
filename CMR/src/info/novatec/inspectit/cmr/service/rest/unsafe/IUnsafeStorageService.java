package info.novatec.inspectit.cmr.service.rest.unsafe;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.management.AbstractLabelManagementAction;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Part of the special unsafe access to the CMR services built for the REST services.
 */
public interface IUnsafeStorageService {
	/**
	 * Creates the new storage on the CMR with information given in {@link StorageData} object and
	 * opens it immediately, so that writing can start.
	 * 
	 * @param storageData
	 *            Information about new storage.
	 * @throws BusinessException
	 *             When {@link StorageData} is insufficient for storage creation. When storage
	 *             creation fails. When storage opening fails.
	 * @return The newly created storage with proper ID and status information.
	 */
	StorageData createAndOpenStorage(StorageData storageData) throws BusinessException;

	/**
	 * Closes an already open storage. Writing after calling this method will not be possible on the
	 * storage. When provided storage is not open, this method will just return, without throwing an
	 * exception.
	 * 
	 * @param storageData
	 *            Storage to close.
	 * @throws BusinessException
	 *             When storage that should be closed is used for recording.
	 */
	void closeStorage(StorageData storageData) throws BusinessException;

	/**
	 * Deletes a storage. Storage can be deleted only if it is not used for recording.
	 * 
	 * @param storageData
	 *            Storage to delete.
	 * @throws BusinessException
	 *             When storage is does not exists or is used for recording.
	 */
	void deleteStorage(StorageData storageData) throws BusinessException;

	/**
	 * Returns if the storage is opened, and thus if the write to the storage can be executed.
	 * 
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage is opened, otherwise false.
	 */
	boolean isStorageOpen(StorageData storageData);

	/**
	 * Returns the list of all opened storages.
	 * 
	 * @return Returns the list of all opened storages. If no storage is opened, the empty list will
	 *         be returned.
	 */
	List<StorageData> getOpenedStorages();

	/**
	 * Returns the list of all existing storages.
	 * 
	 * @return Returns the list of all existing storages. If no storage is existing, the empty list
	 *         will be returned.
	 */
	List<StorageData> getExistingStorages();

	/**
	 * Returns the list of storages that can be read from.
	 * 
	 * @return Returns the list of storages that can be read from.
	 */
	List<StorageData> getReadableStorages();

	/**
	 * Returns the recording state.
	 * 
	 * @return Returns the recording state.
	 * @see RecordingState
	 */
	RecordingState getRecordingState();

	/**
	 * Starts or schedules recording on the provided storage. If the recording properties define the
	 * start date that is after current time, recording will be scheduled. Otherwise it is started
	 * right away.
	 * <p>
	 * When recording is started all data coming to the CMR from this moment to the moment of
	 * calling {@link #stopRecording()} will be written to the given storage.
	 * <p>
	 * If provided storage is not opened, it will be. If the provided storage does not exists, it
	 * will be created and opened.
	 * <p>
	 * Note that exception is thrown if the recording is currently active when calling this method.
	 * 
	 * @param storageData
	 *            Storage to start recording on.
	 * @param recordingProperties
	 *            Properties to start recording with. Must not be null, otherwise the recording
	 *            won't start.
	 * @return The recording storage with proper ID and status information.
	 * @throws BusinessException
	 *             If recording is already active or scheduled. If storage has to be created, then
	 *             when {@link StorageData} is insufficient for storage creation or when storage
	 *             creation fails. If storage has to be opened, then when storage opening fails.
	 */
	StorageData startOrScheduleRecording(StorageData storageData, RecordingProperties recordingProperties) throws BusinessException;

	/**
	 * Stops recording. The storage that is currently used for recording will be closed.
	 * 
	 * @throws BusinessException
	 *             If stopping the recording fails.
	 */
	void stopRecording() throws BusinessException;

	/**
	 * Returns the {@link RecordingData} if the recording is on. Otherwise <code>null</code>.
	 * 
	 * @return Returns the {@link RecordingData} if the recording is on. Otherwise <code>null</code>
	 *         .
	 */
	RecordingData getRecordingData();

	/**
	 * Writes collections of {@link DefaultData} objects to the given storage. The storage has to be
	 * opened before write can be executed. If the provided storage is currently used for recording,
	 * the exception will be thrown.
	 * 
	 * @param storageData
	 *            Storage to write to.
	 * @param defaultDataCollection
	 *            Data to write.
	 * @param dataProcessors
	 *            List of processor to work on data.
	 * @param synchronously
	 *            Should write be synchronous or not.
	 * @throws BusinessException
	 *             If storage is not opened, or the storage is currently used for recording. If
	 *             write fails.
	 */
	void writeToStorage(StorageData storageData, Collection<DefaultData> defaultDataCollection, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously) throws BusinessException;

	/**
	 * Copies the complete content of the buffer to the provided storage. The storage does not have
	 * to be opened before action can be executed (storage will be created/opened first in this
	 * case). If the provided storage is currently used for recording, the exception will be thrown.
	 * 
	 * @param storageData
	 *            Storage to copy data to.
	 * @param platformIdents
	 *            List of agent IDs to include in the copy process.
	 * @param dataProcessors
	 *            List of processor to work on data. Can be null, then the data is only copied with
	 *            no processing.
	 * @param autoFinalize
	 *            If the storage where action is performed should be auto-finalized after the write.
	 * @return The recording storage with proper ID and status information.
	 * @throws BusinessException
	 *             If the storage is currently used for recording. If write fails.
	 */
	StorageData copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize) throws BusinessException;

	/**
	 * Copies the data with provided IDs in the storage. The buffer will be queried for the data
	 * first. The storage does not have to be opened before action can be executed (storage will be
	 * created/opened first in this case). If the provided storage is currently used for recording,
	 * the exception will be thrown.
	 * 
	 * @param storageData
	 *            {@link StorageData} to copy to.
	 * @param elementIds
	 *            Collection of IDs. These IDs represent the objects to be saved.
	 * @param platformIdent
	 *            PLatform ident that element belong to. If zero is passed it will be ignored and
	 *            complete buffer will be searched for the IDs. Providing the platform ident will
	 *            considerably speed-up the query that needs to be performed.
	 * @param dataProcessors
	 *            Processors to process the data. Can be null, then the data is only copied with no
	 *            processing.
	 * @param autoFinalize
	 *            If the storage where action is performed should be auto-finalized after the write.
	 * @return The recording storage with proper ID and status information.
	 * @throws BusinessException
	 *             If the storage is currently used for recording. If write fails.
	 */
	StorageData copyDataToStorage(StorageData storageData, Collection<Long> elementIds, long platformIdent, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws BusinessException;

	/**
	 * Returns the map of the string/long pairs that represent the path to the index files for one
	 * storage and their size in bytes. The paths are in form "/directory/file.extension". These
	 * paths can be used in combination to CMR's ip and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the ip localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the map of the string/long pairs that represent the path to the index files
	 *         and their size.
	 * @throws BusinessException
	 *             When provided storage does not exist.
	 */
	Map<String, Long> getIndexFilesLocations(StorageData storageData) throws BusinessException;

	/**
	 * Returns the map of the string/long pairs that represent the path to the data files for one
	 * storage and their size in bytes. The paths are in form "/directory/file.extension". These
	 * paths can be used in combination to CMR's ip and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the ip localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the map of the string/long pairs that represent the path to the data files
	 *         and their size.
	 * @throws BusinessException
	 *             When provided storage does not exist.
	 */
	Map<String, Long> getDataFilesLocations(StorageData storageData) throws BusinessException;

	/**
	 * Returns the map of the string/long pairs that represent the path to the cached data files for
	 * one storage and their size in bytes. The paths are in form "/directory/file.extension". These
	 * paths can be used in combination to CMR's ip and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the ip localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the map of the string/long pairs that represent the path to the cached data
	 *         files and their size.
	 * @throws BusinessException
	 *             When provided storage does not exist.
	 */
	Map<String, Long> getCachedDataFilesLocations(StorageData storageData) throws BusinessException;

	/**
	 * Returns the map of the string/long pairs that represent the path to the agent files for one
	 * storage and their size in bytes. The paths are in form "/directory/file.extension". These
	 * paths can be used in combination to CMR's ip and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the ip localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the map of the string/long pairs that represent the path to the agent files
	 *         and their size.
	 * @throws BusinessException
	 *             If gathering the file names fails.
	 */
	Map<String, Long> getAgentFilesLocations(StorageData storageData) throws BusinessException;

	/**
	 * Adds one label to the {@link StorageData}. Note that if overwrite is true, the label of the
	 * same type will be overwritten if the type is only one per storage data.
	 * 
	 * @param storageData
	 *            {@link StorageData} object.
	 * @param storageLabel
	 *            Label.
	 * @param doOverwrite
	 *            Should be overwritten if it is one per {@link StorageData}.
	 * @return Updated storage data.
	 * @throws BusinessException
	 *             If gathering the file names fails.
	 */
	StorageData addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws BusinessException;

	/**
	 * Adds collection of labels to the {@link StorageData}. Note that if overwrite is true, the
	 * label of the same type will be overwritten if the type is only one per storage data.
	 * 
	 * @param storageData
	 *            {@link StorageData} object.
	 * @param storageLabels
	 *            Labels.
	 * @param doOverwrite
	 *            Should be overwritten if it is one per {@link StorageData}.
	 * @return Updated storage data.
	 * @throws BusinessException
	 *             If gathering the file names fails.
	 */
	StorageData addLabelsToStorage(StorageData storageData, Collection<AbstractStorageLabel<?>> storageLabels, boolean doOverwrite) throws BusinessException;

	/**
	 * Removes the label from the {@link StorageData}.
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param storageLabel
	 *            Label.
	 * @return Updated storage data.
	 * @throws BusinessException
	 *             If {@link StorageData} information can not be updated on the disk.
	 */
	StorageData removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws BusinessException;

	/**
	 * Removes the label list from the {@link StorageData}.
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param storageLabelList
	 *            List of labels to remove.
	 * @return Updated storage data.
	 * @throws BusinessException
	 *             If {@link StorageData} information can not be updated on the disk.
	 */
	StorageData removeLabelsFromStorage(StorageData storageData, List<AbstractStorageLabel<?>> storageLabelList) throws BusinessException;

	/**
	 * Returns all labels that are at the moment existing in all storages. Note that if the same
	 * label exists in two or more storages, only one label will be included in the returned
	 * collection.
	 * <p>
	 * <i>Note that the return collection is different from the {@link #getAllLabels()} method
	 * result. Some of the labels existing in the storages might not be available in the CMR, and
	 * vice versa.</i>
	 * 
	 * @return Returns all labels that are at the moment existing in all storages.
	 */
	Collection<AbstractStorageLabel<?>> getAllLabelsInStorages();

	/**
	 * Returns all labels registered on the CMR. The labels returned are only the one that are saved
	 * on the CMR database for purpose of label suggestions.
	 * 
	 * @return Returns all labels registered on the CMR.
	 */
	List<AbstractStorageLabel<?>> getAllLabels();

	/**
	 * Gives the label suggestions for a specified label type.
	 * 
	 * @param <E>
	 *            Value type.
	 * @param labelType
	 *            Label type.
	 * @return List of suggestions.
	 */
	<E> List<AbstractStorageLabel<E>> getLabelSuggestions(AbstractStorageLabelType<E> labelType);

	/**
	 * Saves a persistent {@link AbstractStorageLabel} to a CMR database.
	 * 
	 * @param storageLabel
	 *            Label to save.
	 */
	void saveLabelToCmr(AbstractStorageLabel<?> storageLabel);

	/**
	 * Saves a collection of persistent {@link AbstractStorageLabel}s to a CMR database.
	 * 
	 * @param storageLabels
	 *            Collection of labels to save.
	 */
	void saveLabelsToCmr(Collection<AbstractStorageLabel<?>> storageLabels);

	/**
	 * Removes a persisted label from a CMR database.
	 * 
	 * @param storageLabel
	 *            Label to remove.
	 * @param removeFromStoragesAlso
	 *            Should the label also be removed from all storages where it exists.
	 * @throws BusinessException
	 *             If removeFromStoragesAlso is <code>true</code> and {@link StorageData}
	 *             information can not be updated on the disk.
	 */
	void removeLabelFromCmr(AbstractStorageLabel<?> storageLabel, boolean removeFromStoragesAlso) throws BusinessException;;

	/**
	 * Removes a collection of persisted label from a CMR database.
	 * 
	 * @param storageLabels
	 *            Label to remove.
	 * @param removeFromStoragesAlso
	 *            Should the label also be removed from all storages where it exists.
	 * @throws BusinessException
	 *             If removeFromStoragesAlso is <code>true</code> and {@link StorageData}
	 *             information can not be updated on the disk.
	 */
	void removeLabelsFromCmr(Collection<AbstractStorageLabel<?>> storageLabels, boolean removeFromStoragesAlso) throws BusinessException;;

	/**
	 * Saves the {@link AbstractStorageLabelType} to the database. The label will be saved only if
	 * the {@link AbstractStorageLabelType#isMultiType()} is true or no instances of the label type
	 * are already saved.
	 * 
	 * @param labelType
	 *            Label type to save.
	 */
	void saveLabelType(AbstractStorageLabelType<?> labelType);

	/**
	 * Removes label type from database.
	 * 
	 * @param labelType
	 *            Label type to remove.
	 * @throws BusinessException
	 *             If there are still labels of this type existing in the database.
	 */
	void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException;

	/**
	 * Returns all instances of desired label type.
	 * 
	 * @param <E>
	 *            Label value type.
	 * @param labelTypeClass
	 *            Label type class.
	 * @return List of all instances.
	 */
	<E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass);

	/**
	 * Returns all label types.
	 * 
	 * @return Returns all label types.
	 */
	List<AbstractStorageLabelType<?>> getAllLabelTypes();

	/**
	 * Executes the collection of {@link AbstractLabelManagementAction} in order they are given.
	 * 
	 * @param managementActions
	 *            Collection of management actions that can remove different label/label type
	 *            add/removal.
	 * @throws BusinessException
	 *             If exception occurs.
	 */
	void executeLabelManagementActions(Collection<AbstractLabelManagementAction> managementActions) throws BusinessException;

	/**
	 * Updates the data like name and description for a storage.
	 * 
	 * @param storageData
	 *            Storage data object with new values.
	 * @throws BusinessException
	 *             If saving of updated data fails.
	 */
	void updateStorageData(StorageData storageData) throws BusinessException;

	/**
	 * Returns the amount of writing tasks storage still has to process. Note that this is an
	 * approximate number.
	 * 
	 * @param storageData
	 *            Storage data to get information for.
	 * @return Returns number of queued tasks. Note that if the storage is not in writable mode
	 *         <code>0</code> will be returned.
	 */
	long getStorageQueuedWriteTaskCount(StorageData storageData);

	/**
	 * Informs the CMR that the given storage data should be unpacked. The CMR will perform a search
	 * of a proper file in the upload folder that contains the given storage data info. If file is
	 * found it will be unpacked and storage will be prepared for usage.
	 * 
	 * @param storageData
	 *            Storage data that is packed in the file that needs to be unpacked.
	 * 
	 * @throws BusinessException
	 *             If exception occurs during the check.
	 */
	void unpackUploadedStorage(IStorageData storageData) throws BusinessException;

	/**
	 * Creates a storage form the uploaded local storage directory. The CMR will perform a search of
	 * a proper local file in the upload folder.
	 * 
	 * @param localStorageData
	 *            Local storage information.
	 * @throws BusinessException
	 *             If exception occurs during the check.
	 * 
	 */
	void createStorageFromUploadedDir(final IStorageData localStorageData) throws BusinessException;

	/**
	 * Caches the given collection of {@link DefaultData} for the storage. Data will be cached under
	 * the given hash. After caching the service can provide the file where the data is cached if
	 * the same hash is used.
	 * <p>
	 * Note that if the data is already cached with the same hash, no action will be performed.
	 * 
	 * @param storageData
	 *            Storage to hash data for.
	 * @param data
	 *            Data to be cached.
	 * @param hash
	 *            Hash to use for caching.
	 * @throws BusinessException
	 *             If storage does not exist or it is not finalized.
	 */
	void cacheStorageData(StorageData storageData, Collection<? extends DefaultData> data, int hash) throws BusinessException;

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
	 * @throws BusinessException
	 *             If storage does not exist.
	 */
	String getCachedStorageDataFileLocation(StorageData storageData, int hash) throws BusinessException;

}
