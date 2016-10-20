package rocks.inspectit.shared.cs.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.serializer.provider.SerializationManagerProvider;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.storage.IStorageDescriptor;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageDescriptor;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;
import rocks.inspectit.shared.cs.storage.util.StorageDeleteFileVisitor;

/**
 * Abstract class that defines basic storage functionality and properties.
 *
 * @author Ivan Senic
 *
 */
public abstract class StorageManager {

	/**
	 * The log of this class.
	 * <p>
	 * Set to public because of the tests.
	 */
	@Log
	Logger log;

	/**
	 * The rate in milliseconds for checking the remaining hard drive space.
	 */
	private static final int CHECK_HARD_DRIVE_RATE = 5000;

	/**
	 * Name of the cached data folder.
	 */
	private static final String CACHED_DATA_FOLDER = "cache";

	/**
	 * {@link SerializationManagerProvider}.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * Default storage folder.
	 */
	@Value(value = "${storage.storageDefaultFolder}")
	private String storageDefaultFolder;

	/**
	 * Default upload folder.
	 */
	@Value(value = "${storage.storageDefaultFolder}/uploads")
	private String storageUploadsFolder;

	/**
	 * Amount of bytes CMR can use on the Hard drive to write storage data.
	 */
	@Value("${storage.maxHardDriveOccupancy}")
	private int maxHardDriveOccupancy;

	/**
	 * Amount of bytes when warning the user about the critical hard drive space left should start.
	 * This applies to both hard drive space or max hard drive occupancy.
	 */
	@Value("${storage.warnHardDriveByteLeft}")
	private long warnBytesLeft = 1073741824;

	/**
	 * Amount of bytes when writing any more data is suspended because of the hard drive space left.
	 * This applies to both hard drive space or max hard drive occupancy.
	 */
	@Value("${storage.stopWriteHardDriveBytesLeft}")
	private long stopWriteBytesLeft = 104857600;

	/**
	 * Amount of space left for write in bytes. This value is either {@link #maxHardDriveOccupancy}
	 * or actual space left on the hard drive if no {@link #maxHardDriveOccupancy} is specified or
	 * space left is smaller than {@link #maxHardDriveOccupancy}.
	 */
	private long bytesHardDriveOccupancyLeft;

	/**
	 * Amount of total space on the hard drive in bytes.
	 */
	private long hardDriveSize;

	/**
	 * Returns the {@link Path} for given {@link IStorageIdProvider}.
	 *
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public abstract Path getStoragePath(IStorageData storageData);

	/**
	 * Returns the default storage directory as the absolute path.
	 *
	 * @return Returns the default storage directory as the absolute path.
	 */
	protected abstract Path getDefaultStorageDirPath();

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and
	 * {@link StorageDescriptor}.
	 *
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @param descriptor
	 *            {@link StorageDescriptor} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(IStorageData storageData, IStorageDescriptor descriptor) {
		return getStoragePath(storageData).resolve(descriptor.getChannelId() + StorageFileType.DATA_FILE.getExtension());
	}

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and ID of the channel.
	 *
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @param channelId
	 *            Id of channel.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(IStorageData storageData, int channelId) {
		return getStoragePath(storageData).resolve(channelId + StorageFileType.DATA_FILE.getExtension());
	}

	/**
	 * Returns path for the cached storage data file.
	 *
	 * @param storageData
	 *            {@link StorageData}
	 * @param hash
	 *            Hash to be used for hashing.
	 * @return Returns path for the cached storage data file.
	 */
	public Path getCachedDataPath(IStorageData storageData, int hash) {
		Path path = getStoragePath(storageData).resolve(CACHED_DATA_FOLDER).resolve(hash + StorageFileType.CACHED_DATA_FILE.getExtension());
		return path;
	}

	/**
	 * Returns the URL location of the file on the server where the descriptor is pointing to,
	 * without ip and port information.
	 * <p>
	 * Example locations is: /storageId/descriptorId.itdata
	 *
	 * @param storageData
	 *            {@link StorageData}
	 * @param descriptor
	 *            {@link StorageDescriptor}
	 * @return URL location without ip and port.
	 */
	public String getHttpFileLocation(IStorageData storageData, IStorageDescriptor descriptor) {
		return this.getHttpFileLocation(storageData, Integer.valueOf(descriptor.getChannelId()));
	}

	/**
	 * Returns the URL location of the file on the server where the channel ID is pointing to,
	 * without ip and port information.
	 * <p>
	 * Example locations is: /storageId/descriptorId.itdata
	 *
	 * @param storageData
	 *            {@link StorageData}
	 * @param channelId
	 *            Channel ID.
	 * @return URL location without ip and port.
	 */
	public String getHttpFileLocation(IStorageData storageData, Integer channelId) {
		StringBuilder sb = new StringBuilder();
		sb.append('/');
		sb.append(storageData.getId());
		sb.append('/');
		sb.append(channelId.intValue());
		sb.append(StorageFileType.DATA_FILE.getExtension());
		return sb.toString();
	}

	/**
	 * Writes the storage data file to disk (in the default storage directory). If the file already
	 * exists, it will be deleted.
	 *
	 * @param storageData
	 *            Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeStorageDataToDisk(StorageData storageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(storageData, getStoragePath(storageData), StorageFileType.STORAGE_FILE.getExtension());
	}

	/**
	 * Writes the storage data file to disk (in the default storage directory). If the file already
	 * exists, it will be deleted.
	 *
	 * @param localStorageData
	 *            Local Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeLocalStorageDataToDisk(LocalStorageData localStorageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(localStorageData, getStoragePath(localStorageData), StorageFileType.LOCAL_STORAGE_FILE.getExtension());
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 *
	 * @param storageData
	 *            Storage data.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeStorageDataToDisk(StorageData storageData, Path dir) throws IOException, SerializationException {
		this.writeStorageDataToDisk(storageData, dir, StorageFileType.STORAGE_FILE.getExtension());
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 *
	 * @param localStorageData
	 *            Local Storage data.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeLocalStorageDataToDisk(LocalStorageData localStorageData, Path dir) throws IOException, SerializationException {
		this.writeStorageDataToDisk(localStorageData, dir, StorageFileType.LOCAL_STORAGE_FILE.getExtension());
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 *
	 * @param storageData
	 *            Object that can provide ID of storage.
	 * @param extenstion
	 *            File extension to search for.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private void writeStorageDataToDisk(IStorageData storageData, Path dir, String extenstion) throws IOException, SerializationException {
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}

		Path storageDataFile = dir.resolve(storageData.getId() + extenstion);
		Files.deleteIfExists(storageDataFile);

		serializeDataToOutputStream(storageData, Files.newOutputStream(storageDataFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE), true);
	}

	/**
	 * Serializes given data to the {@link OutputStream}.
	 *
	 * @param data
	 *            Data to serialize.
	 * @param outputStream
	 *            {@link OutputStream}
	 * @param closeStream
	 *            If given output stream should be closed upon finish.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void serializeDataToOutputStream(Object data, OutputStream outputStream, boolean closeStream) throws SerializationException {
		ISerializer serializer = serializationManagerProvider.createSerializer();
		Output output = null;
		try {
			output = new Output(outputStream);
			serializer.serialize(data, output);
		} finally {
			if (null != output) {
				if (!closeStream) {
					output.setOutputStream(null);
				}
				output.close();
			}
		}

	}

	/**
	 * Deletes all files associated with given {@link StorageData}, thus completely removes storage
	 * from disk.
	 *
	 * @param storageData
	 *            Storage to delete data for.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 */
	protected void deleteCompleteStorageDataFromDisk(IStorageData storageData) throws IOException {
		Path storageDir = getStoragePath(storageData);

		if (log.isDebugEnabled()) {
			log.info("Deleting the complete storage data from disk. Path: " + storageDir);
		}

		if (Files.exists(storageDir)) {
			Files.walkFileTree(storageDir, new DeleteFileVisitor());
		}
	}

	/**
	 * Deletes all files associated with given {@link StorageData}, but only of a types supplied
	 * with a fileTypes parameter.
	 *
	 * @param storageData
	 *            Storage to delete data for.
	 * @param fileTypes
	 *            File types to delete.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 */
	protected void deleteStorageDataFromDisk(IStorageData storageData, StorageFileType... fileTypes) throws IOException {
		Path storageDir = getStoragePath(storageData);

		if (log.isDebugEnabled()) {
			log.info("Deleting the storage data from disk. Path: " + storageDir + ". File types to delete: " + ArrayUtils.toString(fileTypes));
		}

		if (Files.exists(storageDir)) {
			Files.walkFileTree(storageDir, new StorageDeleteFileVisitor(fileTypes, false));
		}
	}

	/**
	 * @return Returns if the write of data can be performed in terms of hard disk space left.
	 */
	public boolean canWriteMore() {
		return bytesHardDriveOccupancyLeft > stopWriteBytesLeft;
	}

	/**
	 * @return Returns if the warn about the low space left is active.
	 */
	public boolean isSpaceWarnActive() {
		return bytesHardDriveOccupancyLeft < warnBytesLeft;
	}

	/**
	 * Updates the space left on the hard drive.
	 *
	 * @throws IOException
	 *             IF {@link IOException} occurs.
	 */
	@Scheduled(fixedRate = CHECK_HARD_DRIVE_RATE)
	protected void updatedStorageSpaceLeft() throws IOException {
		Path defaultDirectory = getDefaultStorageDirPath();

		Path parent = defaultDirectory;
		while (Files.notExists(parent)) {
			parent = parent.getParent();
		}
		FileStore fileStore = Files.getFileStore(parent);
		hardDriveSize = fileStore.getTotalSpace();
		long bytesAvailable = fileStore.getUsableSpace();

		if (Files.exists(defaultDirectory) && (maxHardDriveOccupancy > 0) && (bytesAvailable > maxHardDriveOccupancy)) {
			final MutableLong totalSizeInBytes = new MutableLong();
			Files.walkFileTree(defaultDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					totalSizeInBytes.add(Files.size(file));
					return FileVisitResult.CONTINUE;
				}
			});

			long totalSize = totalSizeInBytes.longValue();
			bytesHardDriveOccupancyLeft = maxHardDriveOccupancy - totalSize;
			if (bytesHardDriveOccupancyLeft < 0) {
				bytesHardDriveOccupancyLeft = 0;
			}
		} else {
			bytesHardDriveOccupancyLeft = bytesAvailable;
		}
	}

	/**
	 * Compresses the content of the storage data folder to the file. File name is provided via
	 * given path. If the file already exists, it will be deleted first.
	 *
	 * @param storageData
	 *            {@link StorageData} to zip.
	 * @param zipPath
	 *            Path to the zip file.
	 * @throws IOException
	 *             If {@link IOException} occurs during compressing.
	 */
	protected void zipStorageData(IStorageData storageData, final Path zipPath) throws IOException {
		final Path storageDir = getStoragePath(storageData);
		this.zipFiles(storageDir, zipPath);
	}

	/**
	 * Zips all files in the given directory to the provided zipPath.
	 *
	 * @param directory
	 *            Directory where files to be zipped are placed.
	 * @param zipPath
	 *            Path to the zip file.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	protected void zipFiles(final Path directory, Path zipPath) throws IOException {
		// check the given directory where the files are
		if (Files.notExists(directory)) {
			throw new IOException("Can not create zip file. The directory " + directory.toString() + " does not exist.");
		}
		if (!Files.isDirectory(directory)) {
			throw new IOException("Can not create zip file. Given path " + directory.toString() + " is not the directory.");
		}

		// delete zipPath if exists
		Files.deleteIfExists(zipPath);

		// try with resources
		try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String fileName = directory.relativize(file).toString();
					ZipEntry zipEntry = new ZipEntry(fileName);
					zos.putNextEntry(zipEntry);
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});

		}
	}

	/**
	 * Returns the {@link StorageData} object that exists in the compressed storage file.
	 *
	 * @param zipFilePath
	 *            Compressed storage file.
	 * @return StorageData object or <code>null</code> if the given path is not of correct type.
	 */
	protected IStorageData getStorageDataFromZip(final Path zipFilePath) {
		if (Files.notExists(zipFilePath)) {
			return null;
		}

		final ISerializer serializer = serializationManagerProvider.createSerializer();
		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				if (zipEntry.getName().endsWith(StorageFileType.LOCAL_STORAGE_FILE.getExtension())) {

					// check if data is gziped
					boolean isGzip = false;
					try (InputStream is = zipFile.getInputStream(zipEntry)) {
						isGzip = isGzipCompressedData(is);
					}

					// then open the input stream again and copy to destination
					try (Input input = isGzip ? new Input(new GZIPInputStream(zipFile.getInputStream(zipEntry))) : new Input(zipFile.getInputStream(zipEntry))) {
						try {
							Object deserialized = serializer.deserialize(input);
							if (deserialized instanceof IStorageData) {
								return (IStorageData) deserialized;
							}
						} catch (SerializationException e) {
							continue;
						}

					}
				}
			}
		} catch (IOException e) {
			return null;
		}

		return null;
	}

	/**
	 * Unzips the content of the zip file provided to the default storage folder.
	 *
	 * @param zipFilePath
	 *            Path to the zip file.
	 * @param destinationPath
	 *            The path where it should be unpacked.
	 * @throws BusinessException
	 *             If zipFilePath does not exist or destination path does exist.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 *
	 */
	protected void unzipStorageData(final Path zipFilePath, final Path destinationPath) throws BusinessException, IOException {
		if (Files.notExists(zipFilePath)) {
			throw new BusinessException("Unpack the storage file with path " + zipFilePath + ".", StorageErrorCodeEnum.FILE_DOES_NOT_EXIST);
		}

		Files.createDirectories(destinationPath);

		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				Path path = destinationPath.resolve(Paths.get(zipEntry.getName()));

				if (Files.isDirectory(path)) {
					// if it is a directory just create
					Files.createDirectories(path);
				} else {
					// first create directories to the file if needed
					Path parent = path.getParent();
					if (null != parent) {
						if (Files.notExists(parent)) {
							Files.createDirectories(parent);
						}
					}

					// check if data is gziped
					boolean isGzip = false;
					try (InputStream is = zipFile.getInputStream(zipEntry)) {
						isGzip = isGzipCompressedData(is);
					}

					// then open the input stream again and copy to destination
					try (InputStream is = zipFile.getInputStream(zipEntry)) {
						if (isGzip) {
							try (GZIPInputStream gis = new GZIPInputStream(is)) {
								Files.copy(gis, path, StandardCopyOption.REPLACE_EXISTING);
							}
						} else {
							Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns true if the data stored in the input stream is in a GZIP format. The input stream
	 * will be closed at the end.
	 *
	 * @param is
	 *            File to check.
	 * @return True if the data is in GZIP format, false otherwise.
	 * @throws IOException
	 *             If file does not exists or can not be opened for read.
	 */
	private boolean isGzipCompressedData(InputStream is) throws IOException {
		try {
			byte[] firstTwoBytes = new byte[2];
			int read = 0;
			// safety from reading one byte only
			while (read < 2) {
				read += is.read(firstTwoBytes, read, 2 - read);
			}
			int head = (firstTwoBytes[0] & 0xff) | ((firstTwoBytes[1] << 8) & 0xff00);
			return GZIPInputStream.GZIP_MAGIC == head;
		} finally {
			if (null != is) {
				is.close();
			}
		}
	}

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
	 * @throws IOException
	 *             If {@link IOException} is thrown during operation.
	 * @throws SerializationException
	 *             If {@link SerializationException} is thrown during operation.
	 */
	public void cacheStorageData(IStorageData storageData, Collection<? extends DefaultData> data, int hash) throws IOException, SerializationException {
		Path path = getCachedDataPath(storageData, hash);
		if (Files.notExists(path)) {
			Path parent = path.getParent();
			if (Files.notExists(parent)) {
				Files.createDirectories(parent);
			}

			try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
				serializeDataToOutputStream(data, outputStream, true);
			}
		}
	}

	/**
	 * Returns if the results of this query/aggregator combination can be used for caching.
	 *
	 * @param indexQuery
	 *            {@link IIndexQuery}
	 * @param aggregator
	 *            {@link IAggregator}
	 * @return Returns if the results of this query/aggregator combination can be used for caching.
	 */
	public boolean canBeCached(IIndexQuery indexQuery, IAggregator<?> aggregator) {
		// we don't want to cache results that are not aggregated, there is no point
		if (null == aggregator) {
			return false;
		}

		// we won't cache if interval is set, simply because to many cached sets can occur due to
		// the graph views and live mode
		if (indexQuery.isIntervalSet()) {
			return false;
		}

		return true;
	}

	/**
	 * Returns hash for the storage data to be cached for given query and aggregator.
	 * <p>
	 * <B>WARNING:</b> There is small possibility that we get the hash collision. We are aware of
	 * this, but we are taking our chances.
	 *
	 * @param indexQuery
	 *            {@link IIndexQuery}, must not be <code>null</code>
	 * @param aggregator
	 *            {@link IAggregator}
	 * @return Hash
	 */
	public int getCachedDataHash(IIndexQuery indexQuery, IAggregator<?> aggregator) {
		if (null == indexQuery) {
			throw new IllegalArgumentException("Can not create cached data hash when index query is null.");
		}

		final int prime = 31;
		int result = 0;
		result = (prime * result) + indexQuery.hashCode();
		result = (prime * result) + ((aggregator == null) ? 0 : aggregator.hashCode());
		return result;
	}

	/**
	 * Gets {@link #serializationManagerProvider}.
	 *
	 * @return {@link #serializationManagerProvider}
	 */
	public SerializationManagerProvider getSerializationManagerProvider() {
		return serializationManagerProvider;
	}

	/**
	 * Sets {@link #serializationManagerProvider}.
	 *
	 * @param serializationManagerProvider
	 *            New value for {@link #serializationManagerProvider}
	 */
	public void setSerializationManagerProvider(SerializationManagerProvider serializationManagerProvider) {
		this.serializationManagerProvider = serializationManagerProvider;
	}

	/**
	 * Gets {@link #storageUploadsFolder}.
	 *
	 * @return {@link #storageUploadsFolder}
	 */
	public String getStorageUploadsFolder() {
		return storageUploadsFolder;
	}

	/**
	 * @return the storageDefaultFolder
	 */
	public String getStorageDefaultFolder() {
		return storageDefaultFolder;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 *
	 * @param storageDefaultFolder
	 *            the storageDefaultFolder to set
	 */
	public void setStorageDefaultFolder(String storageDefaultFolder) {
		this.storageDefaultFolder = storageDefaultFolder;
	}

	/**
	 * Gets {@link #bytesHardDriveOccupancyLeft}.
	 *
	 * @return {@link #bytesHardDriveOccupancyLeft}
	 */
	public long getBytesHardDriveOccupancyLeft() {
		return bytesHardDriveOccupancyLeft;
	}

	/**
	 * Gets {@link #maxHardDriveOccupancy}.
	 *
	 * @return {@link #maxHardDriveOccupancy}
	 */
	public long getMaxBytesHardDriveOccupancy() {
		if (maxHardDriveOccupancy > 0) {
			return maxHardDriveOccupancy;
		} else {
			return hardDriveSize;
		}
	}

}
