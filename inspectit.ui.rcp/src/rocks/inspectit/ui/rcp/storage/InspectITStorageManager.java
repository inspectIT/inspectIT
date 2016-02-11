package info.novatec.inspectit.rcp.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.enumeration.StorageErrorCodeEnum;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinitionProvider;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.rcp.storage.util.DataRetriever;
import info.novatec.inspectit.rcp.storage.util.DataUploader;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageFileType;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.util.ObjectUtils;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.mutable.MutableObject;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;

import com.esotericsoftware.kryo.io.Input;

/**
 * {@link StorageManager} for GUI.
 * 
 * @author Ivan Senic
 * 
 */
public class InspectITStorageManager extends StorageManager implements CmrRepositoryChangeListener { // NOPMD

	/**
	 * List of downloaded storages.
	 */
	private Set<LocalStorageData> downloadedStorages = Collections.newSetFromMap(new ConcurrentHashMap<LocalStorageData, Boolean>(16, 0.75f, 2));

	/**
	 * Map of mounted and online not available storages.
	 */
	private Set<LocalStorageData> mountedNotAvailableStorages = Collections.newSetFromMap(new ConcurrentHashMap<LocalStorageData, Boolean>(16, 0.75f, 2));
	/**
	 * Map of mounted and online not available storages.
	 */
	private Map<LocalStorageData, CmrRepositoryDefinition> mountedAvailableStorages = new ConcurrentHashMap<LocalStorageData, CmrRepositoryDefinition>(16, 0.75f, 2);

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedRepositoriesStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>(16, 0.75f, 2);

	/**
	 * List of {@link StorageChangeListener}s.
	 */
	private List<StorageChangeListener> storageChangeListeners = new ArrayList<StorageChangeListener>();

	/**
	 * {@link DataRetriever}.
	 */
	private DataRetriever dataRetriever;

	/**
	 * {@link DataUploader}.
	 */
	private DataUploader dataUploader;

	/**
	 * {@link StorageRepositoryDefinitionProvider}.
	 */
	private StorageRepositoryDefinitionProvider storageRepositoryDefinitionProvider;

	/**
	 * Mounts a new storage locally. Same as calling
	 * {@link #mountStorage(StorageData, CmrRepositoryDefinition, false, false)}.
	 * 
	 * @param storageData
	 *            Storage to mount.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param subMonitor
	 *            {@link SubMonitor} to report to.
	 * @throws BusinessException
	 *             If storage directory can not be created or storage file can not be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 */
	public void mountStorage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition, SubMonitor subMonitor) throws BusinessException, IOException, SerializationException {
		this.mountStorage(storageData, cmrRepositoryDefinition, false, false, subMonitor);
	}

	/**
	 * Mounts a new storage locally. Provides option to specify if the complete download should be
	 * performed.
	 * 
	 * @param storageData
	 *            Storage to mount.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param fullyDownload
	 *            Should storage be immediately fully down-loaded. Intended for future use.
	 * @param compressBefore
	 *            If the fullyDownalod is <code>true</code>, this parameter can define if data files
	 *            should be compressed on the fly before sent.
	 * @param subMonitor
	 *            {@link SubMonitor} to report to.
	 * @throws BusinessException
	 *             If storage directory can not be created or storage file can not be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * 
	 */
	private void mountStorage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition, boolean fullyDownload, boolean compressBefore, SubMonitor subMonitor) throws BusinessException,
			IOException, SerializationException {
		LocalStorageData localStorageData = new LocalStorageData(storageData);

		Path directory = getStoragePath(localStorageData);
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		if (fullyDownload) {
			try {
				subMonitor.setTaskName("Downloading storage files for storage '" + storageData.getName() + "'..");
				dataRetriever.downloadAndSaveStorageFiles(cmrRepositoryDefinition, storageData, directory, compressBefore, true, subMonitor, StorageFileType.values());
				downloadedStorages.add(localStorageData);
				localStorageData.setFullyDownloaded(true);
			} catch (Exception e) {
				deleteLocalStorageData(localStorageData, false);
				throw e;
			}
		} else {
			try {
				subMonitor.setTaskName("Downloading agent and indexing files for storage '" + storageData.getName() + "'..");
				dataRetriever.downloadAndSaveStorageFiles(cmrRepositoryDefinition, storageData, directory, compressBefore, true, subMonitor, StorageFileType.AGENT_FILE, StorageFileType.INDEX_FILE);
			} catch (Exception e) {
				deleteLocalStorageData(localStorageData, false);
				throw e;
			}
		}

		writeLocalStorageDataToDisk(localStorageData);

		final String systemUserName = getSystemUsername();
		try {
			if (null != systemUserName) {
				StringStorageLabel mountedByLabel = new StringStorageLabel(systemUserName, new ExploredByLabelType());
				cmrRepositoryDefinition.getStorageService().addLabelToStorage(storageData, mountedByLabel, true);
			}
		} catch (final Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					InspectIT.getDefault().createInfoDialog("'Mounted by' label with value '" + systemUserName + "'was not added to the storage. Exception message is:\n\n" + e.getMessage(), -1);
				}
			});
		}

		mountedAvailableStorages.put(localStorageData, cmrRepositoryDefinition);
	}

	/**
	 * Returns if the storage data is already downloaded.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @return Returns if the storage data is already downloaded.
	 */
	public boolean isFullyDownloaded(StorageData storageData) {
		for (LocalStorageData lsd : downloadedStorages) {
			if (ObjectUtils.equals(lsd.getId(), storageData.getId())) {
				return lsd.isFullyDownloaded();
			}
		}

		return false;
	}

	/**
	 * Fully downloads selected storage.
	 * 
	 * @param storageData
	 *            StorageData.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param subMonitor
	 *            {@link SubMonitor} to report to.
	 * @throws SerializationException
	 *             If serialization exception occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 *             If storage is already fully downloaded.
	 */
	public void fullyDownloadStorage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition, boolean compressBefore, SubMonitor subMonitor) throws BusinessException, IOException,
			SerializationException {
		LocalStorageData localStorageData = null;
		for (LocalStorageData lsd : mountedAvailableStorages.keySet()) {
			if (ObjectUtils.equals(lsd.getId(), storageData.getId())) {
				localStorageData = lsd;
				break;
			}
		}

		if (null == localStorageData) {
			mountStorage(storageData, cmrRepositoryDefinition, true, compressBefore, subMonitor);
			return;
		}

		if (localStorageData.isFullyDownloaded()) {
			throw new BusinessException("Download the storage " + storageData + ".", StorageErrorCodeEnum.STORAGE_ALREADY_DOWNLOADED);
		}

		Path directory = getStoragePath(localStorageData);
		subMonitor.setTaskName("Downloading storage data files for storage '" + storageData.getName() + "'..");
		dataRetriever.downloadAndSaveStorageFiles(cmrRepositoryDefinition, storageData, directory, compressBefore, true, subMonitor, StorageFileType.DATA_FILE, StorageFileType.CACHED_DATA_FILE);
		downloadedStorages.add(localStorageData);
		localStorageData.setFullyDownloaded(true);
		writeLocalStorageDataToDisk(localStorageData);

	}

	/**
	 * Deletes all local data saved for given {@link LocalStorageData}, unmount storage.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @throws IOException
	 *             If deleting of the local data fails.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	public void deleteLocalStorageData(LocalStorageData localStorageData) throws IOException, SerializationException {
		this.deleteLocalStorageData(localStorageData, true);
	}

	/**
	 * Deletes all local data saved for given {@link LocalStorageData}, unmount storage.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param informListeners
	 *            Should the listeners be informed.
	 * @throws IOException
	 *             If deleting of the local data fails.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private void deleteLocalStorageData(LocalStorageData localStorageData, boolean informListeners) throws IOException, SerializationException {
		localStorageData.setFullyDownloaded(false);
		downloadedStorages.remove(localStorageData);
		if (mountedAvailableStorages.containsKey(localStorageData) || mountedNotAvailableStorages.contains(localStorageData)) {
			super.deleteStorageDataFromDisk(localStorageData, StorageFileType.DATA_FILE);
			writeLocalStorageDataToDisk(localStorageData);
		} else {
			super.deleteCompleteStorageDataFromDisk(localStorageData);
		}

		if (informListeners) {
			synchronized (storageChangeListeners) {
				for (StorageChangeListener storageChangeListener : storageChangeListeners) {
					storageChangeListener.storageLocallyDeleted(localStorageData);
				}
			}
		}
	}

	/**
	 * Informs the {@link InspectITStorageManager} that a {@link StorageData} has been remotely
	 * deleted.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void storageRemotelyDeleted(StorageData storageData) throws IOException, SerializationException {
		LocalStorageData localStorageData = null;
		for (Map.Entry<LocalStorageData, CmrRepositoryDefinition> entry : mountedAvailableStorages.entrySet()) {
			if (ObjectUtils.equals(entry.getKey().getId(), storageData.getId())) {
				localStorageData = entry.getKey();
				break;
			}
		}
		if (null != localStorageData && !localStorageData.isFullyDownloaded()) {
			deleteLocalStorageData(localStorageData, false);
		} else {
			for (LocalStorageData notAvailable : mountedNotAvailableStorages) {
				if (ObjectUtils.equals(notAvailable.getId(), storageData.getId())) {
					localStorageData = notAvailable;
					break;
				}
			}
			if (null != localStorageData && !localStorageData.isFullyDownloaded()) {
				deleteLocalStorageData(localStorageData, false);
			}
		}

		synchronized (storageChangeListeners) {
			for (StorageChangeListener storageChangeListener : storageChangeListeners) {
				storageChangeListener.storageRemotelyDeleted(storageData);
			}
		}
	}

	/**
	 * Informs the Storage Manager that the {@link StorageData} has been remotely updated, so that
	 * existing local clone of the data can be updated.
	 * 
	 * @param storageData
	 *            {@link StorageData} that was updated.
	 * @throws IOException
	 *             If {@link IOException} occurs during saving data to disk.
	 * @throws SerializationException
	 *             If serialization fails during saving data to disk.
	 */
	public void storageRemotelyUpdated(StorageData storageData) throws IOException, SerializationException {
		LocalStorageData localStorageData = getLocalDataForStorage(storageData);
		if (null != localStorageData) {
			updateLocalStorageData(localStorageData, storageData);
		}

		synchronized (storageChangeListeners) {
			for (StorageChangeListener storageChangeListener : storageChangeListeners) {
				storageChangeListener.storageDataUpdated(storageData);
			}
		}
	}

	/**
	 * Returns mounted and available storages, thus the ones that can be read from.
	 * 
	 * @return List of {@link LocalStorageData}.
	 */
	public Collection<LocalStorageData> getMountedAvailableStorages() {
		return Collections.unmodifiableSet(mountedAvailableStorages.keySet());
	}

	/**
	 * Returns mounted but not available storages, thus the ones that can not be read from because
	 * there is no CMR that can handle them.
	 * 
	 * @return List of {@link LocalStorageData}.
	 */
	public Collection<LocalStorageData> getMountedUnavailableStorages() {
		return Collections.unmodifiableSet(mountedNotAvailableStorages);
	}

	/**
	 * Returns collection of fully downloaded storages.
	 * 
	 * @return List of {@link LocalStorageData}.
	 */
	public Collection<LocalStorageData> getDownloadedStorages() {
		return Collections.unmodifiableSet(downloadedStorages);
	}

	/**
	 * Returns storage {@link Path}.
	 * 
	 * @param storageData
	 *            Storage.
	 * @return Returns storage {@link Path}.
	 * @see Paths#get(String, String...)
	 */
	public Path getStoragePath(IStorageData storageData) {
		return getDefaultStorageDirPath().resolve(storageData.getStorageFolder());
	}

	/**
	 * Loads initial local mounted storage information.
	 */
	public void startUp() {
		List<LocalStorageData> mountedStorages;
		try {
			mountedStorages = getMountedStoragesFromDisk();
		} catch (Exception e) {
			mountedStorages = Collections.emptyList();
		}
		Map<StorageData, CmrRepositoryDefinition> onlineStorages = getOnlineStorages();

		for (LocalStorageData localStorageData : mountedStorages) {
			if (localStorageData.isFullyDownloaded()) {
				downloadedStorages.add(localStorageData);
			}
			boolean availableOnline = false;
			for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : onlineStorages.entrySet()) {
				if (ObjectUtils.equals(entry.getKey().getId(), localStorageData.getId())) {
					availableOnline = true;
					try {
						updateLocalStorageData(localStorageData, entry.getKey());
					} catch (final Exception e) {
						final String name = localStorageData.getName();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Local data for the storage '" + name + "' could not be updated with the online data.", e, -1);
							}
						});
					}
					mountedAvailableStorages.put(localStorageData, entry.getValue());
					break;
				}
			}
			if (!availableOnline) {
				mountedNotAvailableStorages.add(localStorageData);
			}
		}

		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
	}

	/**
	 * Instantiates the new storage repository definition based on the {@link LocalStorageData}
	 * provided. Note that the local storage data has to be in the collection of
	 * {@link #getMountedAvailableStorages()}. Otherwise the creation will fail with an Exception
	 * being thrown..
	 * <p>
	 * A special care needs to be taken with the method, because the the returned object could be
	 * quite big, since it will hold the complete indexing tree. Thus, it is important that the
	 * caller of this method take responsibility to make earlier created definitions ready for
	 * garbage collection as soon as they are not needed anymore.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData} to create the definition for.
	 * @return {@link StorageRepositoryDefinition}.
	 * @throws BusinessException
	 *             If the wanted {@link LocalStorageData} is not available.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * 
	 */
	public StorageRepositoryDefinition getStorageRepositoryDefinition(LocalStorageData localStorageData) throws BusinessException, SerializationException, IOException {
		// check if it is available
		if (!mountedAvailableStorages.keySet().contains(localStorageData) && !downloadedStorages.contains(localStorageData)) {
			throw new BusinessException("Create repository definition for the storage " + localStorageData + ".", StorageErrorCodeEnum.STORAGE_DOES_NOT_EXIST);
		}

		// find CMR repository def, allow null for downloaded and not mounted storages
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		cmrRepositoryDefinition = mountedAvailableStorages.get(localStorageData);

		// get agents
		List<PlatformIdent> platformIdents = getPlatformIdentsLocally(localStorageData);
		if (null == platformIdents) {
			platformIdents = Collections.emptyList();
		}

		// get indexing tree
		IStorageTreeComponent<? extends DefaultData> indexingTree = getIndexingTree(localStorageData);
		if (null == indexingTree) {
			indexingTree = new ArrayBasedStorageLeaf<DefaultData>();
		}

		// create new storage repository definition
		StorageRepositoryDefinition storageRepositoryDefinition = storageRepositoryDefinitionProvider.createStorageRepositoryDefinition();
		storageRepositoryDefinition.setAgents(platformIdents);
		storageRepositoryDefinition.setIndexingTree(indexingTree);
		storageRepositoryDefinition.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageRepositoryDefinition.setLocalStorageData(localStorageData);
		storageRepositoryDefinition.initServices();
		return storageRepositoryDefinition;
	}

	/**
	 * Checks if the storage is locally mounted.
	 * 
	 * @param storageData
	 *            Storage data to check.
	 * @return True if storage is mounted, false otherwise.
	 */
	public boolean isStorageMounted(StorageData storageData) {
		return getLocalDataForStorage(storageData) != null;
	}

	/**
	 * Returns the local data for storage if the storage is mounted or downloaded.
	 * 
	 * @param storageData
	 *            Storage data to check.
	 * @return {@link LocalStorageData}.
	 */
	public LocalStorageData getLocalDataForStorage(StorageData storageData) {
		for (LocalStorageData downloadedStorage : downloadedStorages) {
			if (ObjectUtils.equals(downloadedStorage.getId(), storageData.getId())) {
				return downloadedStorage;
			}
		}
		for (LocalStorageData mountedStorage : mountedNotAvailableStorages) {
			if (ObjectUtils.equals(storageData.getId(), mountedStorage.getId())) {
				return mountedStorage;
			}
		}
		for (LocalStorageData mountedStorage : mountedAvailableStorages.keySet()) {
			if (ObjectUtils.equals(storageData.getId(), mountedStorage.getId())) {
				return mountedStorage;
			}
		}
		return null;
	}

	/**
	 * Registers a {@link StorageChangeListener} if the same listener does not already exist.
	 * 
	 * @param storageChangeListener
	 *            {@link StorageChangeListener} to add.
	 */
	public void addStorageChangeListener(StorageChangeListener storageChangeListener) {
		synchronized (storageChangeListeners) {
			if (!storageChangeListeners.contains(storageChangeListener)) {
				storageChangeListeners.add(storageChangeListener);
			}
		}
	}

	/**
	 * Removes a {@link StorageChangeListener}.
	 * 
	 * @param storageChangeListener
	 *            {@link StorageChangeListener} to remove.
	 */
	public void removeStorageChangeListener(StorageChangeListener storageChangeListener) {
		synchronized (storageChangeListeners) {
			storageChangeListeners.remove(storageChangeListener);
		}
	}

	/**
	 * Uploads a file to the {@link CmrRepositoryDefinition} storage uploads.
	 * 
	 * @param fileName
	 *            Name of file.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param subMonitor
	 *            {@link SubMonitor} to report progress to.
	 * @throws IOException
	 *             If upload file does not exist or upload fails.
	 */
	public void uploadZippedStorage(String fileName, CmrRepositoryDefinition cmrRepositoryDefinition, SubMonitor subMonitor) throws IOException {
		Path file = Paths.get(fileName);
		Path relativizePath = file.getParent();
		String tmpDir = "tmp" + UUID.randomUUID().hashCode();
		subMonitor.setTaskName("Uploading storage file..");
		// no compressing since it is already zipped
		dataUploader.uploadFileToStorageUploads(file, relativizePath, tmpDir, cmrRepositoryDefinition, subMonitor);
	}

	/**
	 * Uploads a complete storage to the {@link CmrRepositoryDefinition} upload folder. All files
	 * belonging to the local storage data will be uploaded to the temporary directory.
	 * 
	 * @param localStorageData
	 *            Storage to upload.
	 * @param cmrRepositoryDefinition
	 *            Repository definition.
	 * @param subMonitor
	 *            The monitor to report upload progress to.
	 * @throws BusinessException
	 *             If storage is not fully downloaded.
	 * @throws IOException
	 *             If {@link IOException} occurss during operation.
	 */
	public void uploadCompleteStorage(LocalStorageData localStorageData, final CmrRepositoryDefinition cmrRepositoryDefinition, SubMonitor subMonitor) throws BusinessException, IOException {
		if (!localStorageData.isFullyDownloaded()) {
			throw new BusinessException("Uploading the storage " + localStorageData + ".", StorageErrorCodeEnum.STORAGE_IS_NOT_DOWNLOADED);
		}

		String tmpDir = "tmp" + UUID.randomUUID().hashCode();
		Path storageDir = getStoragePath(localStorageData);
		final List<Path> toUpload = new ArrayList<Path>();
		Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				toUpload.add(file);
				return FileVisitResult.CONTINUE;
			}
		});
		subMonitor.setTaskName("Uploading storage files..");
		dataUploader.uploadFileToStorageUploads(toUpload, storageDir, tmpDir, cmrRepositoryDefinition, subMonitor);
	}

	/**
	 * Compresses the content of the local storage data folder to the file. File name is provided
	 * via given path. If the file already exists, it will be deleted first.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData} to zip.
	 * @param zipFileName
	 *            Zip file name.
	 * @throws BusinessException
	 *             If the storage is not fully downloaded.
	 * @throws IOException
	 *             If {@link IOException} occurs during compressing.
	 */
	public void zipStorageData(LocalStorageData localStorageData, String zipFileName) throws BusinessException, IOException {
		if (!localStorageData.isFullyDownloaded()) {
			throw new BusinessException("Zip data for the storage " + localStorageData + ".", StorageErrorCodeEnum.STORAGE_IS_NOT_DOWNLOADED);
		} else {
			Path zipPath = Paths.get(zipFileName);
			if (Files.exists(zipPath)) {
				Files.delete(zipPath);
			}

			try {
				super.zipStorageData(localStorageData, zipPath);
			} catch (IOException e) {
				Files.deleteIfExists(zipPath);
				throw e;
			}
		}
	}

	/**
	 * Zips the remote storage files to the file. File name is provided via given path. If the file
	 * already exists, it will be deleted first.
	 * 
	 * @param storageData
	 *            Remote storage to zip.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} where storage is located.
	 * @param zipFileName
	 *            Zip file name.
	 * @param compressBefore
	 *            Defines if the data should be compressed before downloading.
	 * @param subMonitor
	 *            {@link SubMonitor} to report to.
	 * @throws BusinessException
	 *             If serialization of data fails during zipping.
	 * @throws IOException
	 *             If {@link IOException} occurs during compressing.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 */
	public void zipStorageData(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition, String zipFileName, boolean compressBefore, SubMonitor subMonitor) throws BusinessException,
			IOException, SerializationException {
		Path zipPath = Paths.get(zipFileName);
		if (Files.exists(zipPath)) {
			Files.delete(zipPath);
		}

		try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
			// download and pack at the same time
			subMonitor.setTaskName("Downloading and packing storage files..");
			dataRetriever.downloadAndZipStorageFiles(cmrRepositoryDefinition, storageData, zos, compressBefore, false, subMonitor, StorageFileType.values());

			// add local storage data info
			LocalStorageData localStorageData = new LocalStorageData(storageData);
			localStorageData.setFullyDownloaded(true);
			String fileName = localStorageData.getId() + StorageFileType.LOCAL_STORAGE_FILE.getExtension();
			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);
			serializeDataToOutputStream(localStorageData, zos, false);
			zos.closeEntry();
		} catch (IOException | BusinessException | SerializationException e) {
			Files.deleteIfExists(zipPath);
			throw e;
		}

	}

	/**
	 * Returns the {@link StorageData} object that exists in the compressed storage file.
	 * 
	 * @param zipFileName
	 *            Compressed storage file name.
	 * @return {@link IStorageData} object or <code>null</code> if the given file is not of correct
	 *         type or does not exist.
	 */
	public IStorageData getStorageDataFromZip(String zipFileName) {
		return this.getStorageDataFromZip(Paths.get(zipFileName));
	}

	/**
	 * Unzips the content of the zip file provided to the default storage folder. The method will
	 * first unzip the complete content of the zip file to the temporary folder and then rename the
	 * temporary folder to match the storage ID.
	 * <p>
	 * The method will also check if the imported storage is available online, and if it is will
	 * update the local data saved.
	 * 
	 * @param fileName
	 *            File to unzip.
	 * @throws BusinessException
	 *             If given file does not exist or content is not proper.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization exception occurs if data needs to be updated.
	 * 
	 */
	public void unzipStorageData(String fileName) throws BusinessException, IOException, SerializationException {
		Path zipPath = Paths.get(fileName);
		IStorageData packedStorageData = getStorageDataFromZip(zipPath);
		this.unzipStorageData(zipPath, getStoragePath(packedStorageData));

		List<LocalStorageData> localStorageDataList = getMountedStoragesFromDisk();
		for (LocalStorageData localStorageData : localStorageDataList) {
			if (localStorageData.isFullyDownloaded() && !downloadedStorages.contains(localStorageData)) {
				downloadedStorages.add(localStorageData);
				for (StorageData storageData : getOnlineStorages().keySet()) {
					if (ObjectUtils.equals(storageData.getId(), localStorageData.getId())) {
						updateLocalStorageData(localStorageData, storageData);
						break;
					}
				}
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.addMountedStorages(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.removeMountedStorages(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition cmrRepositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING) {
			OnlineStatus cachedStatus = cachedRepositoriesStatus.get(cmrRepositoryDefinition);
			if (!ObjectUtils.equals(cachedStatus, newStatus)) {
				if (newStatus == OnlineStatus.ONLINE) {
					this.addMountedStorages(cmrRepositoryDefinition);
				} else if (newStatus == OnlineStatus.OFFLINE) {
					this.removeMountedStorages(cmrRepositoryDefinition);
				}
			}
			cachedRepositoriesStatus.put(cmrRepositoryDefinition, newStatus);
		}
	}

	/**
	 * Updates the information of the local storage data saved on the client machine with the data
	 * provided in the storage data available online.
	 * 
	 * @param localStorageData
	 *            Local storage data to update.
	 * @param storageData
	 *            Storage data that holds new information.
	 * @throws SerializationException
	 *             If serialization of data fails.
	 * @throws IOException
	 *             If {@link IOException} occurs during data saving to disk.
	 */
	private void updateLocalStorageData(LocalStorageData localStorageData, StorageData storageData) throws IOException, SerializationException {
		localStorageData.copyStorageDataInformation(storageData);
		writeLocalStorageDataToDisk(localStorageData);
	}

	/**
	 * Adds mounted storages that are bounded to the given {@link CmrRepositoryDefinition} to
	 * "available" map.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	private void addMountedStorages(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			List<StorageData> closedStorages = cmrRepositoryDefinition.getStorageService().getReadableStorages();
			List<LocalStorageData> newAvailableStoarges = new ArrayList<LocalStorageData>();
			for (LocalStorageData localStorageData : mountedNotAvailableStorages) {
				for (StorageData storageData : closedStorages) {
					if (ObjectUtils.equals(localStorageData.getId(), storageData.getId())) {
						newAvailableStoarges.add(localStorageData);
						try {
							updateLocalStorageData(localStorageData, storageData);
						} catch (final SerializationException | IOException e) {
							final String name = localStorageData.getName();
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									InspectIT.getDefault().createErrorDialog("Local data for the storage '" + name + "' could not be updated with the online data.", e, -1);
								}
							});
						}
						mountedAvailableStorages.put(localStorageData, cmrRepositoryDefinition);
						break;
					}
				}
			}

			if (!newAvailableStoarges.isEmpty()) {
				mountedNotAvailableStorages.removeAll(newAvailableStoarges);
			}
		}
	}

	/**
	 * Removes mounted storages that are bounded to the given {@link CmrRepositoryDefinition} to
	 * "available" map.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	private void removeMountedStorages(CmrRepositoryDefinition cmrRepositoryDefinition) {
		List<LocalStorageData> removeList = new ArrayList<LocalStorageData>();
		for (Map.Entry<LocalStorageData, CmrRepositoryDefinition> entry : mountedAvailableStorages.entrySet()) {
			if (!entry.getKey().isFullyDownloaded()) {
				if (ObjectUtils.equals(entry.getValue(), cmrRepositoryDefinition)) {
					removeList.add(entry.getKey());
				}
			}
		}
		if (!removeList.isEmpty()) {
			mountedAvailableStorages.keySet().removeAll(removeList);
			mountedNotAvailableStorages.addAll(removeList);
		}
	}

	/**
	 * Loads {@link PlatformIdent}s from a disk for a storage.
	 * 
	 * @param storageData
	 *            {@link IStorageData}
	 * @return List of {@link PlatformIdent}s involved in the storage data or null if no file
	 *         exists.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private List<PlatformIdent> getPlatformIdentsLocally(final IStorageData storageData) throws IOException, SerializationException {
		Path storagePath = getStoragePath(storageData);
		List<PlatformIdent> returnList = this.getObjectsByFileTreeWalk(storagePath, StorageFileType.AGENT_FILE.getExtension());
		if (!returnList.isEmpty()) {
			return returnList;
		} else {
			return null;
		}
	}

	/**
	 * Loads indexing tree from a disk for a storage.
	 * 
	 * @param storageData
	 *            {@link IStorageData}
	 * @return Indexing tree or null if it can not be found.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private IStorageTreeComponent<DefaultData> getIndexingTree(final IStorageData storageData) throws IOException, SerializationException {
		Path storagePath = getStoragePath(storageData);
		List<IStorageTreeComponent<DefaultData>> indexingTrees = this.getObjectsByFileTreeWalk(storagePath, StorageFileType.INDEX_FILE.getExtension());
		if (!indexingTrees.isEmpty()) {
			if (indexingTrees.size() == 1) {
				return indexingTrees.get(0);
			} else {
				CombinedStorageBranch<DefaultData> combinedStorageBranch = new CombinedStorageBranch<DefaultData>(indexingTrees);
				return combinedStorageBranch;
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns all storages that have been mounted locally.
	 * 
	 * @return Returns all storages that have been mounted locally as a list of
	 *         {@link LocalStorageData}.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private List<LocalStorageData> getMountedStoragesFromDisk() throws IOException, SerializationException {
		return this.getObjectsByFileTreeWalk(getDefaultStorageDirPath(), StorageFileType.LOCAL_STORAGE_FILE.getExtension());
	}

	/**
	 * Reads the objects from files that are in a given path or sub-paths. Note that generic can be
	 * used to specify the wanted class. How ever, if the object loaded from a file is not of a
	 * wanted class, {@link ClassCastException} will be thrown as usual.
	 * 
	 * @param <E>
	 *            Wanted type. Use object if it is uncertain what types object will be.
	 * @param path
	 *            {@link Path} to look in.
	 * @param fileSufix
	 *            Ending of the files (extension).
	 * @return List of deserialized objects.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 * 
	 */
	private <E> List<E> getObjectsByFileTreeWalk(Path path, final String fileSufix) throws IOException, SerializationException {
		if (!Files.isDirectory(path)) {
			return Collections.emptyList();
		}

		final ISerializer serializer = getSerializationManagerProvider().createSerializer();
		final MutableObject mutableException = new MutableObject();
		final List<E> returnList = new ArrayList<E>();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@SuppressWarnings("unchecked")
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(fileSufix)) {
					InputStream inputStream = null;
					Input input = null;
					try {
						inputStream = Files.newInputStream(file, StandardOpenOption.READ);
						input = new Input(inputStream);
						Object deserialized = serializer.deserialize(input);
						returnList.add((E) deserialized);
					} catch (SerializationException e) {
						mutableException.setValue(e);
						return FileVisitResult.TERMINATE;
					} finally {
						if (null != input) {
							input.close();
						}
					}

				}
				return FileVisitResult.CONTINUE;
			}
		});

		SerializationException serializationException = (SerializationException) mutableException.getValue();
		if (null != serializationException) {
			throw serializationException;
		} else {
			return returnList;
		}
	}

	/**
	 * Returns map of online available storages with their {@link CmrRepositoryDefinition} as a
	 * value.
	 * 
	 * @return Map of online available storages with their {@link CmrRepositoryDefinition} as a
	 *         value.
	 */
	private Map<StorageData, CmrRepositoryDefinition> getOnlineStorages() {
		Map<StorageData, CmrRepositoryDefinition> storageMap = new HashMap<StorageData, CmrRepositoryDefinition>();
		List<CmrRepositoryDefinition> allRepositories = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : allRepositories) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				List<StorageData> closedStorages = cmrRepositoryDefinition.getStorageService().getReadableStorages();
				if (null != closedStorages) {
					for (StorageData storageData : closedStorages) {
						storageMap.put(storageData, cmrRepositoryDefinition);
					}
				}
			}
		}
		return storageMap;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Path getDefaultStorageDirPath() {
		return InspectIT.getDefault().getRuntimeDir().resolve(getStorageDefaultFolder()).toAbsolutePath();
	}

	/**
	 * 
	 * @return Returns the system username.
	 */
	private String getSystemUsername() {
		return System.getProperty("user.name");
	}

	/**
	 * @param dataRetriever
	 *            the httpDataRetriever to set
	 */
	public void setDataRetriever(DataRetriever dataRetriever) {
		this.dataRetriever = dataRetriever;
	}

	/**
	 * Sets {@link #dataUploader}.
	 * 
	 * @param dataUploader
	 *            New value for {@link #dataUploader}
	 */
	public void setDataUploader(DataUploader dataUploader) {
		this.dataUploader = dataUploader;
	}

	/**
	 * Sets {@link #storageRepositoryDefinitionProvider}.
	 * 
	 * @param storageRepositoryDefinitionProvider
	 *            New value for {@link #storageRepositoryDefinitionProvider}
	 */
	public void setStorageRepositoryDefinitionProvider(StorageRepositoryDefinitionProvider storageRepositoryDefinitionProvider) {
		this.storageRepositoryDefinitionProvider = storageRepositoryDefinitionProvider;
	}

}
