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

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Part of the special unsafe access to the CMR services built for the REST services.
 * 
 * This class is the center of the unsafe access chain and is responsible for persisting the method names.
 * 
 * The re-wiring of the interfaces is necessary, because the REST services do not provide authentication mechanisms.
 * 
 * The normal method names are mapped to unsafe methods, if there exists a permission test for the inspectIT client,
 * so the REST services still have unrestricted access.
 */
public class UnsafeStorageService implements IUnsafeStorageService {
	
	/**
	 * Linking the interfaces.
	 */
	@Autowired
	private IUnsafeEntryForStorageService entry;

	@Override
	public StorageData createAndOpenStorage(StorageData storageData) throws BusinessException {
		return entry.unsafeCreateAndOpenStorage(storageData);
	}

	@Override
	public void closeStorage(StorageData storageData) throws BusinessException {
		entry.unsafeCloseStorage(storageData);
	}

	@Override
	public void deleteStorage(StorageData storageData) throws BusinessException {
		entry.unsafeDeleteStorage(storageData);
	}

	@Override
	public boolean isStorageOpen(StorageData storageData) {
		return entry.isStorageOpen(storageData);
	}

	@Override
	public List<StorageData> getOpenedStorages() {
		return entry.getOpenedStorages();
	}

	@Override
	public List<StorageData> getExistingStorages() {
		return entry.unsafeGetExistingStorages();
	}

	@Override
	public List<StorageData> getReadableStorages() {
		return entry.getReadableStorages();
	}

	@Override
	public RecordingState getRecordingState() {
		return entry.getRecordingState();
	}

	@Override
	public StorageData startOrScheduleRecording(StorageData storageData, RecordingProperties recordingProperties) throws BusinessException {
		return entry.unsafeStartOrScheduleRecording(storageData, recordingProperties);
	}

	@Override
	public void stopRecording() throws BusinessException {
		entry.unsafeStopRecording();
	}

	@Override
	public RecordingData getRecordingData() {
		return entry.getRecordingData();
	}

	@Override
	public void writeToStorage(StorageData storageData, Collection<DefaultData> defaultDataCollection, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously)
			throws BusinessException {
		entry.writeToStorage(storageData, defaultDataCollection, dataProcessors, synchronously);
	}

	@Override
	public StorageData copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize) throws BusinessException {
		return entry.copyBufferToStorage(storageData, platformIdents, dataProcessors, autoFinalize);
	}

	@Override
	public StorageData copyDataToStorage(StorageData storageData, Collection<Long> elementIds, long platformIdent, Collection<AbstractDataProcessor> dataProcessors, boolean autoFinalize)
			throws BusinessException {
		return entry.copyDataToStorage(storageData, elementIds, platformIdent, dataProcessors, autoFinalize);
	}

	@Override
	public Map<String, Long> getIndexFilesLocations(StorageData storageData) throws BusinessException {
		return entry.getIndexFilesLocations(storageData);
	}

	@Override
	public Map<String, Long> getDataFilesLocations(StorageData storageData) throws BusinessException {
		return entry.getDataFilesLocations(storageData);
	}

	@Override
	public Map<String, Long> getCachedDataFilesLocations(StorageData storageData) throws BusinessException {
		return entry.getCachedDataFilesLocations(storageData);
	}

	@Override
	public Map<String, Long> getAgentFilesLocations(StorageData storageData) throws BusinessException {
		return entry.getAgentFilesLocations(storageData);
	}

	@Override
	public StorageData addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws BusinessException {
		return entry.unsafeAddLabelToStorage(storageData, storageLabel, doOverwrite);
	}

	@Override
	public StorageData addLabelsToStorage(StorageData storageData, Collection<AbstractStorageLabel<?>> storageLabels, boolean doOverwrite) throws BusinessException {
		return entry.unsafeAddLabelsToStorage(storageData, storageLabels, doOverwrite);
	}

	@Override
	public StorageData removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws BusinessException {
		return entry.unsafeRemoveLabelFromStorage(storageData, storageLabel);
	}

	@Override
	public StorageData removeLabelsFromStorage(StorageData storageData, List<AbstractStorageLabel<?>> storageLabelList) throws BusinessException {
		return entry.unsafeRemoveLabelsFromStorage(storageData, storageLabelList);
	}

	@Override
	public Collection<AbstractStorageLabel<?>> getAllLabelsInStorages() {
		return entry.getAllLabelsInStorages();
	}

	@Override
	public List<AbstractStorageLabel<?>> getAllLabels() {
		return entry.getAllLabels();
	}

	@Override
	public <E> List<AbstractStorageLabel<E>> getLabelSuggestions(AbstractStorageLabelType<E> labelType) {
		return entry.getLabelSuggestions(labelType);
	}

	@Override
	public void saveLabelToCmr(AbstractStorageLabel<?> storageLabel) {
		entry.saveLabelToCmr(storageLabel);
	}

	@Override
	public void saveLabelsToCmr(Collection<AbstractStorageLabel<?>> storageLabels) {
		entry.saveLabelsToCmr(storageLabels);
	}

	@Override
	public void removeLabelFromCmr(AbstractStorageLabel<?> storageLabel, boolean removeFromStoragesAlso) throws BusinessException {
		entry.removeLabelFromCmr(storageLabel, removeFromStoragesAlso);
	}

	@Override
	public void removeLabelsFromCmr(Collection<AbstractStorageLabel<?>> storageLabels, boolean removeFromStoragesAlso) throws BusinessException {
		entry.removeLabelsFromCmr(storageLabels, removeFromStoragesAlso);
	}

	@Override
	public void saveLabelType(AbstractStorageLabelType<?> labelType) {
		entry.saveLabelType(labelType);
	}

	@Override
	public void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException {
		entry.removeLabelType(labelType);
	}

	@Override
	public <E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass) {
		return entry.getLabelTypes(labelTypeClass);
	}

	@Override
	public List<AbstractStorageLabelType<?>> getAllLabelTypes() {
		return entry.getAllLabelTypes();
	}

	@Override
	public void executeLabelManagementActions(Collection<AbstractLabelManagementAction> managementActions) throws BusinessException {
		entry.executeLabelManagementActions(managementActions);
	}

	@Override
	public void updateStorageData(StorageData storageData) throws BusinessException {
		entry.unsafeUpdateStorageData(storageData);
	}

	@Override
	public long getStorageQueuedWriteTaskCount(StorageData storageData) {
		return entry.getStorageQueuedWriteTaskCount(storageData);
	}

	@Override
	public void unpackUploadedStorage(IStorageData storageData) throws BusinessException {
		entry.unpackUploadedStorage(storageData);
	}

	@Override
	public void createStorageFromUploadedDir(IStorageData localStorageData) throws BusinessException {
		entry.createStorageFromUploadedDir(localStorageData);
	}

	@Override
	public void cacheStorageData(StorageData storageData, Collection<? extends DefaultData> data, int hash) throws BusinessException {
		entry.cacheStorageData(storageData, data, hash);
	}

	@Override
	public String getCachedStorageDataFileLocation(StorageData storageData, int hash) throws BusinessException {
		return entry.getCachedStorageDataFileLocation(storageData, hash);
	}
}
