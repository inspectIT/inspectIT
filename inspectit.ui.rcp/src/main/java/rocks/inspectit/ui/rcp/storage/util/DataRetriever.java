package rocks.inspectit.ui.rcp.storage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;

import com.esotericsoftware.kryo.io.Input;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.storage.serializer.ISerializer;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;
import rocks.inspectit.shared.all.storage.serializer.util.KryoUtil;
import rocks.inspectit.shared.cs.indexing.storage.IStorageDescriptor;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageDescriptor;
import rocks.inspectit.shared.cs.storage.IStorageData;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.shared.cs.storage.nio.stream.InputStreamProvider;
import rocks.inspectit.shared.cs.storage.util.RangeDescriptor;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.storage.http.TransferDataMonitor;

/**
 * Class responsible for retrieving the data via HTTP, and de-serializing the data into objects.
 *
 * @author Ivan Senic
 *
 */
public class DataRetriever {

	/**
	 * Amount of serializers to be available to this class.
	 */
	private int serializerCount = 3;

	/**
	 * {@link StorageManager}.
	 */
	private StorageManager storageManager;

	/**
	 * Serialization manager provider.
	 */
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * Queue for {@link ISerializer} that are available.
	 */
	private BlockingQueue<ISerializer> serializerQueue = new LinkedBlockingQueue<>();

	/**
	 * Stream provider needed for local data reading.
	 */
	private InputStreamProvider streamProvider;

	/**
	 * Initializes the retriever.
	 *
	 * @throws Exception
	 *             If exception occurs.
	 */
	protected void init() throws Exception {
		for (int i = 0; i < serializerCount; i++) {
			serializerQueue.add(serializationManagerProvider.createSerializer());
		}
	}

	/**
	 * Retrieves the wanted data described in the {@link StorageDescriptor} from the desired
	 * {@link CmrRepositoryDefinition}. This method will try to invoke as less as possible HTTP
	 * requests for all descriptors.
	 * <p>
	 * The method will execute the HTTP requests sequentially.
	 * <p>
	 * It is not guaranteed that amount of returned objects in the list is same as the amount of
	 * provided descriptors. If some of the descriptors are pointing to the wrong files or files
	 * positions, it can happen that this influences the rest of the descriptor that point to the
	 * same file. Thus, a special care needs to be taken that the data in descriptors is correct.
	 *
	 * @param <E>
	 *            Type of the objects are wanted.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData} that points to the wanted storage.
	 * @param descriptors
	 *            Descriptors.
	 * @return List of objects in the supplied generic type. Note that if the data described in the
	 *         descriptor is not of a supplied generic type, there will be a casting exception
	 *         thrown.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getDataViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageData storageData, List<IStorageDescriptor> descriptors)
			throws IOException, SerializationException {
		Map<Integer, List<IStorageDescriptor>> separateFilesGroup = createFilesGroup(descriptors);
		List<E> receivedData = new ArrayList<>();
		String serverUri = getServerUri(cmrRepositoryDefinition);

		HttpClient httpClient = new DefaultHttpClient();
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : separateFilesGroup.entrySet()) {
			HttpGet httpGet = new HttpGet(serverUri + storageManager.getHttpFileLocation(storageData, entry.getKey()));
			StringBuilder rangeHeader = new StringBuilder("bytes=");

			RangeDescriptor rangeDescriptor = null;
			for (IStorageDescriptor descriptor : entry.getValue()) {
				if (null == rangeDescriptor) {
					rangeDescriptor = new RangeDescriptor(descriptor);
				} else {
					if ((rangeDescriptor.getEnd() + 1) == descriptor.getPosition()) {
						rangeDescriptor.setEnd((descriptor.getPosition() + descriptor.getSize()) - 1);
					} else {
						rangeHeader.append(rangeDescriptor.toString());
						rangeHeader.append(',');
						rangeDescriptor = new RangeDescriptor(descriptor);
					}
				}
			}
			rangeHeader.append(rangeDescriptor);

			httpGet.addHeader("Range", rangeHeader.toString());
			ISerializer serializer = null;
			try {
				serializer = serializerQueue.take();
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			InputStream inputStream = null;
			Input input = null;
			try {
				HttpResponse response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				if (MultipartEntityUtil.isMultipart(entity)) {
					inputStream = entity.getContent();
					@SuppressWarnings("deprecation")
					// all non-deprecated constructors have default modifier
					MultipartStream multipartStream = new MultipartStream(inputStream, MultipartEntityUtil.getBoundary(entity).getBytes());
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					boolean nextPart = multipartStream.skipPreamble();
					while (nextPart) {
						multipartStream.readHeaders();
						multipartStream.readBodyData(byteArrayOutputStream);
						input = new Input(byteArrayOutputStream.toByteArray());
						while (KryoUtil.hasMoreBytes(input)) {
							Object object = serializer.deserialize(input);
							E element = (E) object;
							receivedData.add(element);
						}
						nextPart = multipartStream.readBoundary();
					}
				} else {
					// when kryo changes the visibility of optional() method, we can really stream
					input = new Input(EntityUtils.toByteArray(entity));
					while (KryoUtil.hasMoreBytes(input)) {
						Object object = serializer.deserialize(input);
						E element = (E) object;
						receivedData.add(element);
					}
				}
			} finally {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != input) {
					input.close();
				}
				serializerQueue.add(serializer);
			}
		}
		return receivedData;
	}

	/**
	 * Retrieves the wanted data described in the {@link StorageDescriptor} from the desired
	 * offline-available storage.
	 * <p>
	 * It is not guaranteed that amount of returned objects in the list is same as the amount of
	 * provided descriptors. If some of the descriptors are pointing to the wrong files or files
	 * positions, it can happen that this influences the rest of the descriptor that point to the
	 * same file. Thus, a special care needs to be taken that the data in descriptors is correct.
	 *
	 * @param <E>
	 *            Type of the objects are wanted.
	 * @param localStorageData
	 *            {@link LocalStorageData} that points to the wanted storage.
	 * @param descriptors
	 *            Descriptors.
	 * @return List of objects in the supplied generic type. Note that if the data described in the
	 *         descriptor is not of a supplied generic type, there will be a casting exception
	 *         thrown.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getDataLocally(LocalStorageData localStorageData, List<IStorageDescriptor> descriptors) throws IOException, SerializationException {
		Map<Integer, List<IStorageDescriptor>> separateFilesGroup = createFilesGroup(descriptors);
		List<IStorageDescriptor> optimizedDescriptors = new ArrayList<>();
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : separateFilesGroup.entrySet()) {
			StorageDescriptor storageDescriptor = null;
			for (IStorageDescriptor descriptor : entry.getValue()) {
				if (null == storageDescriptor) {
					storageDescriptor = new StorageDescriptor(entry.getKey());
					storageDescriptor.setPositionAndSize(descriptor.getPosition(), descriptor.getSize());
				} else {
					if (!storageDescriptor.join(descriptor)) {
						optimizedDescriptors.add(storageDescriptor);
						storageDescriptor = new StorageDescriptor(entry.getKey());
						storageDescriptor.setPositionAndSize(descriptor.getPosition(), descriptor.getSize());
					}
				}
			}
			optimizedDescriptors.add(storageDescriptor);
		}

		List<E> receivedData = new ArrayList<>(descriptors.size());

		ISerializer serializer = null;
		try {
			serializer = serializerQueue.take();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		InputStream inputStream = null;
		Input input = null;
		try {
			inputStream = streamProvider.getExtendedByteBufferInputStream(localStorageData, optimizedDescriptors);
			input = new Input(inputStream);
			while (KryoUtil.hasMoreBytes(input)) {
				Object object = serializer.deserialize(input);
				E element = (E) object;
				receivedData.add(element);
			}
		} finally {
			if (null != input) {
				input.close();
			}
			serializerQueue.add(serializer);
		}

		return receivedData;
	}

	/**
	 * Returns cached data for the storage from the CMR if the cached data exists for given hash. If
	 * data does not exist <code>null</code> is returned.
	 *
	 * @param <E>
	 *            Type of the objects are wanted.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData} that points to the wanted storage.
	 * @param hash
	 *            Hash under which the cached data is stored.
	 * @return Returns cached data for the storage from the CMR if the cached data exists for given
	 *         hash. If data does not exist <code>null</code> is returned.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurred.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getCachedDataViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, int hash)
			throws BusinessException, IOException, SerializationException {
		String cachedFileLocation = cmrRepositoryDefinition.getStorageService().getCachedStorageDataFileLocation(storageData, hash);
		if (null == cachedFileLocation) {
			return null;
		} else {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(getServerUri(cmrRepositoryDefinition) + cachedFileLocation);
			ISerializer serializer = null;
			try {
				serializer = serializerQueue.take();
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			InputStream inputStream = null;
			Input input = null;
			try {
				HttpResponse response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				inputStream = entity.getContent();
				input = new Input(inputStream);
				Object object = serializer.deserialize(input);
				List<E> receivedData = (List<E>) object;
				return receivedData;
			} finally {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != input) {
					input.close();
				}
				serializerQueue.add(serializer);
			}
		}
	}

	/**
	 * Returns cached data for the given hash locally. This method can be used when storage if fully
	 * downloaded.
	 *
	 * @param <E>
	 *            Type of the objects are wanted.
	 *
	 * @param localStorageData
	 *            {@link LocalStorageData} that points to the wanted storage.
	 * @param hash
	 *            Hash under which the cached data is stored.
	 * @return Returns cached data for the storage if the cached data exists for given hash. If data
	 *         does not exist <code>null</code> is returned.
	 * @throws SerializationException
	 *             If {@link SerializationException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getCachedDataLocally(LocalStorageData localStorageData, int hash) throws IOException, SerializationException {
		Path path = storageManager.getCachedDataPath(localStorageData, hash);
		if (Files.notExists(path)) {
			return null;
		} else {
			ISerializer serializer = null;
			try {
				serializer = serializerQueue.take();
			} catch (InterruptedException e) {
				Thread.interrupted();
			}

			Input input = null;
			try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
				input = new Input(inputStream);
				Object object = serializer.deserialize(input);
				List<E> receivedData = (List<E>) object;
				return receivedData;
			} finally {
				if (null != input) {
					input.close();
				}
				serializerQueue.add(serializer);
			}
		}
	}

	/**
	 * Downloads and saves locally wanted files associated with given {@link StorageData}. Files
	 * will be saved in passed directory. The caller can specify the type of the files to download
	 * by passing the proper {@link StorageFileType}s to the method.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param directory
	 *            Directory to save objects. compressBefore Should data files be compressed on the
	 *            fly before sent.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @param subMonitor
	 *            {@link SubMonitor} for process reporting.
	 * @param fileTypes
	 *            Files that should be downloaded.
	 * @throws BusinessException
	 *             If directory to save does not exists. If files wanted can not be found on the
	 *             server.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void downloadAndSaveStorageFiles(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, final Path directory, boolean compressBefore, boolean decompressContent,
			SubMonitor subMonitor, StorageFileType... fileTypes) throws BusinessException, IOException {
		if (!Files.isDirectory(directory)) {
			throw new BusinessException("Download and save storage files for storage " + storageData + " to the path " + directory.toString() + ".", StorageErrorCodeEnum.FILE_DOES_NOT_EXIST);
		}

		Map<String, Long> allFiles = getFilesFromCmr(cmrRepositoryDefinition, storageData, fileTypes);

		if (MapUtils.isNotEmpty(allFiles)) {
			PostDownloadRunnable postDownloadRunnable = new PostDownloadRunnable() {
				@Override
				public void process(InputStream content, String fileName) throws IOException {
					String[] splittedFileName = fileName.split("/");
					Path writePath = directory;
					// first part is empty, second is storage id, we don't need it
					for (int i = 2; i < splittedFileName.length; i++) {
						writePath = writePath.resolve(splittedFileName[i]);
					}
					// ensure all dirs are created
					if (Files.notExists(writePath.getParent())) {
						Files.createDirectories(writePath.getParent());
					}
					Files.copy(content, writePath, StandardCopyOption.REPLACE_EXISTING);
				}
			};
			this.downloadAndSaveObjects(cmrRepositoryDefinition, allFiles, postDownloadRunnable, compressBefore, decompressContent, subMonitor);
		}
	}

	/**
	 * Downloads and saves locally wanted files associated with given {@link StorageData}. Files
	 * will be saved in passed directory. The caller can specify the type of the files to download
	 * by passing the proper {@link StorageFileType}s to the method.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param zos
	 *            {@link ZipOutputStream} to place files to.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @param subMonitor
	 *            {@link SubMonitor} for process reporting.
	 * @param fileTypes
	 *            Files that should be downloaded.
	 * @throws BusinessException
	 *             If directory to save does not exists. If files wanted can not be found on the
	 *             server.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void downloadAndZipStorageFiles(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, final ZipOutputStream zos, boolean compressBefore, boolean decompressContent,
			SubMonitor subMonitor, StorageFileType... fileTypes) throws BusinessException, IOException {
		Map<String, Long> allFiles = getFilesFromCmr(cmrRepositoryDefinition, storageData, fileTypes);

		PostDownloadRunnable postDownloadRunnable = new PostDownloadRunnable() {
			@Override
			public void process(InputStream content, String fileName) throws IOException {
				String[] splittedFileName = fileName.split("/");
				String originalFileName = splittedFileName[splittedFileName.length - 1];
				ZipEntry zipEntry = new ZipEntry(originalFileName);
				zos.putNextEntry(zipEntry);
				IOUtils.copy(content, zos);
				zos.closeEntry();
			}
		};
		this.downloadAndSaveObjects(cmrRepositoryDefinition, allFiles, postDownloadRunnable, compressBefore, decompressContent, subMonitor);
	}

	/**
	 * Returns the map of the existing files for the given storage. The value in the map is file
	 * size. Only wanted file types will be included in the map.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}
	 * @param fileTypes
	 *            Files that should be included.
	 * @return Map of file names with their size.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs during service invocation.
	 */
	private Map<String, Long> getFilesFromCmr(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, StorageFileType... fileTypes) throws BusinessException {
		Map<String, Long> allFiles = new HashMap<>();

		// agent files
		if (ArrayUtils.contains(fileTypes, StorageFileType.AGENT_FILE)) {
			Map<String, Long> platformIdentsFiles = cmrRepositoryDefinition.getStorageService().getAgentFilesLocations(storageData);
			allFiles.putAll(platformIdentsFiles);
		}

		// business context files
		if (ArrayUtils.contains(fileTypes, StorageFileType.BUSINESS_CONTEXT_FILE)) {
			Map<String, Long> businessContextFiles = cmrRepositoryDefinition.getStorageService().getBusinessContextFilesLocation(storageData);
			allFiles.putAll(businessContextFiles);
		}

		// indexing files
		if (ArrayUtils.contains(fileTypes, StorageFileType.INDEX_FILE)) {
			Map<String, Long> indexingTreeFiles = cmrRepositoryDefinition.getStorageService().getIndexFilesLocations(storageData);
			allFiles.putAll(indexingTreeFiles);
		}

		if (ArrayUtils.contains(fileTypes, StorageFileType.DATA_FILE)) {
			// data files
			Map<String, Long> dataFiles = cmrRepositoryDefinition.getStorageService().getDataFilesLocations(storageData);
			allFiles.putAll(dataFiles);
		}

		if (ArrayUtils.contains(fileTypes, StorageFileType.CACHED_DATA_FILE)) {
			// data files
			Map<String, Long> dataFiles = cmrRepositoryDefinition.getStorageService().getCachedDataFilesLocations(storageData);
			allFiles.putAll(dataFiles);
		}

		return allFiles;
	}

	/**
	 * Down-loads and saves the file from a {@link CmrRepositoryDefinition}. Files will be saved in
	 * the directory that is denoted as the given Path object. Original file names will be used.
	 *
	 * @param cmrRepositoryDefinition
	 *            Repository.
	 * @param files
	 *            Map with file names and sizes.
	 * @param postDownloadRunnable
	 *            {@link PostDownloadRunnable} that will be executed after successful request.
	 * @param useGzipCompression
	 *            If the GZip compression should be used when files are downloaded.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @param subMonitor
	 *            {@link SubMonitor} for process reporting.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 *             If status of HTTP response is not successful (codes 2xx).
	 */
	private void downloadAndSaveObjects(CmrRepositoryDefinition cmrRepositoryDefinition, Map<String, Long> files, PostDownloadRunnable postDownloadRunnable, boolean useGzipCompression,
			boolean decompressContent, final SubMonitor subMonitor) throws IOException, BusinessException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		final TransferDataMonitor transferDataMonitor = new TransferDataMonitor(subMonitor, files, useGzipCompression);
		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
				response.setEntity(new DownloadHttpEntityWrapper(response.getEntity(), transferDataMonitor));
			}
		});

		if (useGzipCompression && decompressContent) {
			httpClient.addResponseInterceptor(new GzipHttpResponseInterceptor());
		}

		for (Map.Entry<String, Long> fileEntry : files.entrySet()) {
			String fileName = fileEntry.getKey();
			String fileLocation = getServerUri(cmrRepositoryDefinition) + fileName;
			HttpGet httpGet = new HttpGet(fileLocation);
			if (useGzipCompression) {
				httpGet.addHeader("accept-encoding", "gzip");
			}

			transferDataMonitor.startTransfer(fileName);
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			if (HttpStatus.valueOf(statusLine.getStatusCode()).series().equals(Series.SUCCESSFUL)) {
				HttpEntity entity = response.getEntity();
				try (InputStream is = entity.getContent()) {
					postDownloadRunnable.process(is, fileName);
				}
			}
			transferDataMonitor.endTransfer(fileName);
		}
	}

	/**
	 * Returns the URI of the server in format 'http://ip:port'.
	 *
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return URI as string.
	 */
	private String getServerUri(CmrRepositoryDefinition repositoryDefinition) {
		return "http://" + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort();
	}

	/**
	 * Creates the pairs that have a channel ID as a key, and list of descriptors as value. All the
	 * descriptors in the list are associated with the channel, thus all the data described in the
	 * descriptors can be retrieved with a single HTTP/local request.
	 *
	 * @param descriptors
	 *            Un-grouped descriptors.
	 *
	 * @return Map of channel IDs with its descriptors.
	 */
	private Map<Integer, List<IStorageDescriptor>> createFilesGroup(List<IStorageDescriptor> descriptors) {
		Map<Integer, List<IStorageDescriptor>> filesMap = new HashMap<>();
		for (IStorageDescriptor storageDescriptor : descriptors) {
			Integer channelId = Integer.valueOf(storageDescriptor.getChannelId());
			List<IStorageDescriptor> oneFileList = filesMap.get(channelId);
			if (null == oneFileList) {
				oneFileList = new ArrayList<>();
				filesMap.put(channelId, oneFileList);
			}
			oneFileList.add(storageDescriptor);
		}

		// sort lists
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : filesMap.entrySet()) {
			List<IStorageDescriptor> list = entry.getValue();
			Collections.sort(list, new Comparator<IStorageDescriptor>() {

				@Override
				public int compare(IStorageDescriptor o1, IStorageDescriptor o2) {
					return Long.compare(o1.getPosition(), o2.getPosition());
				}
			});
		}

		return filesMap;
	}

	/**
	 * Sets {@link #storageManager}.
	 *
	 * @param storageManager
	 *            New value for {@link #storageManager}
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	/**
	 *
	 * @param entity
	 *            {@link HttpEntity}
	 * @return True if the GZip encoding is active.
	 */
	private static boolean isGZipContentEncoding(HttpEntity entity) {
		Header ceHeader = entity.getContentEncoding();
		if (ceHeader != null) {
			HeaderElement[] codecs = ceHeader.getElements();
			for (HeaderElement codec : codecs) {
				if (codec.getName().equalsIgnoreCase("gzip")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets {@link #serializerCount}.
	 *
	 * @param serializerCount
	 *            New value for {@link #serializerCount}
	 */
	public void setSerializerCount(int serializerCount) {
		this.serializerCount = serializerCount;
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
	 * Sets {@link #streamProvider}.
	 *
	 * @param streamProvider
	 *            New value for {@link #streamProvider}
	 */
	public void setStreamProvider(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	/**
	 * A wrapper for the {@link HttpEntity} that will surround the entity's input stream with the
	 * {@link GZIPInputStream}. *
	 * <p>
	 * <b>IMPORTANT:</b> The class code is copied/taken/based from <a href=
	 * "https://svn.apache.org/repos/asf/httpcomponents/httpcore/branches/4.0.x/contrib/src/main/java/org/apache/http/contrib/compress/GzipDecompressingEntity.java"
	 * >Http Core's GzipDecompressingEntity</a>. License info can be found
	 * <a href="http://www.apache.org/licenses/LICENSE-2.0">here</a>.
	 *
	 *
	 */
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

		/**
		 * Default constructor.
		 *
		 * @param entity
		 *            Entity that has the response in the GZip format.
		 */
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public InputStream getContent() throws IOException {
			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getContentLength() {
			// length of uncompressed content is not known
			return -1;
		}

	}

	/**
	 * Response interceptor that alters the response entity if the encoding is gzip.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class GzipHttpResponseInterceptor implements HttpResponseInterceptor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			if (isGZipContentEncoding(response.getEntity())) {
				response.setEntity(new GzipDecompressingEntity(response.getEntity()));
			}
		}

	}

	/**
	 * Simple interface to enable multiple operations after file download.
	 *
	 * @author Ivan Senic
	 *
	 */
	private interface PostDownloadRunnable {

		/**
		 * Process the input stream. If stream is not closed, it will be after exiting this method.
		 *
		 * @param content
		 *            {@link InputStream} that represents content of downloaded file.
		 * @param fileName
		 *            Name of the file being downloaded.
		 * @throws IOException
		 *             If {@link IOException} occurs.
		 */
		void process(InputStream content, String fileName) throws IOException;
	}

}
