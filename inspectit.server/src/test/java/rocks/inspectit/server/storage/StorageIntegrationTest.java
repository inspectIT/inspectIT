package rocks.inspectit.server.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.Input;

import rocks.inspectit.server.test.AbstractTransactionalTestNGLogSupport;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.serializer.util.KryoUtil;
import rocks.inspectit.shared.cs.indexing.storage.IStorageDescriptor;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.shared.cs.storage.label.StringStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.impl.RatingLabelType;
import rocks.inspectit.shared.cs.storage.nio.stream.InputStreamProvider;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataSaverProcessor;

/**
 * Tests the complete CMR storage functionality.
 *
 * @author Ivan Senic
 *
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class StorageIntegrationTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private CmrStorageManager storageManager;

	/**
	 * {@link InputStreamProvider}.
	 */
	@Autowired
	InputStreamProvider inputStreamProvider;

	/**
	 * {@link ISerializer}.
	 */
	@Autowired
	private ISerializer serializer;

	/**
	 * Storage data to be used in testing.
	 */
	private StorageData storageData;

	/**
	 * List of invocations that will be written to storage and then read.
	 */
	private List<InvocationSequenceData> createdInvocations;

	/**
	 * Indexing tree of storage.
	 */
	private IStorageTreeComponent<?> storageIndexingTree;

	/**
	 * Data saver processor.
	 */
	private DataSaverProcessor dataSaverProcessor;

	/**
	 * Init.
	 */
	@BeforeClass
	public void createStorageData() {
		storageData = getStorageData();
		createdInvocations = new ArrayList<>();
		List<Class<? extends DefaultData>> saverClasses = new ArrayList<>();
		saverClasses.add(InvocationSequenceData.class);
		dataSaverProcessor = new DataSaverProcessor(saverClasses, true);
	}

	/**
	 * We can not open not-existing storage.
	 */
	@Test(expectedExceptions = BusinessException.class)
	public void openUnexisting() throws IOException, SerializationException, BusinessException {
		storageManager.openStorage(new StorageData());
	}

	/**
	 * Tests creation of storage.
	 *
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 */
	@Test
	public void createStorageTest() throws IOException, SerializationException, BusinessException {
		storageManager.createStorage(storageData);

		File storageDir = getStorageFolder();

		assertThat(storageDir.isDirectory(), is(true));
		File[] storageFiles = storageDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(StorageFileType.STORAGE_FILE.getExtension());
			}
		});

		// only one storage file created
		assertThat(storageFiles.length, is(equalTo(1)));

		// get the data from file and check for equal
		byte[] storageDataBytes = Files.readAllBytes(storageFiles[0].toPath());
		Input input = new Input(storageDataBytes);
		StorageData deserializedStorageData = (StorageData) serializer.deserialize(input);
		assertThat(deserializedStorageData, is(equalTo(storageData)));

		storageManager.openStorage(storageData);
		assertThat(storageData.isStorageOpened(), is(true));
		assertThat(storageData.isStorageClosed(), is(false));

		// get the data from file and check for equal
		storageDataBytes = Files.readAllBytes(storageFiles[0].toPath());
		input.setBuffer(storageDataBytes);
		deserializedStorageData = (StorageData) serializer.deserialize(input);
		assertThat(deserializedStorageData, is(equalTo(storageData)));

		// storage manager know for the storage
		assertThat(storageManager.getExistingStorages(), hasItem(storageData));
		assertThat(storageManager.getOpenedStorages(), hasItem(storageData));
		assertThat(storageManager.getReadableStorages(), not(hasItem(storageData)));
		assertThat(storageData.isStorageOpened(), is(true));
		assertThat(storageData.isStorageClosed(), is(false));
	}

	/**
	 * Test write to storage.
	 *
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test(dependsOnMethods = { "createStorageTest" })
	public void testWrite() throws BusinessException, IOException, SerializationException {
		Random random = new Random();
		int repeat = random.nextInt(100);
		List<AbstractDataProcessor> processors = new ArrayList<>();
		processors.add(dataSaverProcessor);
		for (int i = 0; i < repeat; i++) {
			InvocationSequenceData invoc = getInvocationSequenceDataInstance(1 + random.nextInt(1000));
			boolean canAdd = true;
			if (invoc.getId() == 0) {
				canAdd = false;
			} else {
				for (InvocationSequenceData inCollection : createdInvocations) {
					if (invoc.getId() == inCollection.getId()) {
						canAdd = false;
						break;
					}
				}
			}
			if (canAdd) {
				createdInvocations.add(invoc);
			}
		}
		storageManager.writeToStorage(storageData, createdInvocations, processors, false);
	}

	/**
	 * Test storage finalization.
	 *
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs.
	 */
	@Test(dependsOnMethods = { "testWrite" })
	public void finalizeWriteTest() throws IOException, SerializationException, BusinessException {
		storageManager.closeStorage(storageData);

		assertThat(storageData.isStorageOpened(), is(false));
		assertThat(storageData.isStorageClosed(), is(true));

		File storageFolder = getStorageFolder();

		File[] indexFiles = storageFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(StorageFileType.INDEX_FILE.getExtension());
			}
		});
		assertThat(indexFiles.length, is(equalTo(1)));

		String indexFilePath = indexFiles[0].getPath();
		byte[] indexTreeBytes = Files.readAllBytes(Paths.get(indexFilePath));
		assertThat(indexTreeBytes.length, is(greaterThan(0)));

		ByteArrayInputStream bais = new ByteArrayInputStream(indexTreeBytes);
		Input input = new Input(bais);
		Object indexingTree = serializer.deserialize(input);
		assertThat(indexingTree, is(instanceOf(IStorageTreeComponent.class)));

		storageIndexingTree = (IStorageTreeComponent<?>) indexingTree;

		assertThat(storageManager.getReadableStorages(), hasItem(storageData));
	}

	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = { "testWrite" })
	public void storageDataCaching() throws IOException, SerializationException {
		int myHash = 13;
		storageManager.cacheStorageData(storageData, createdInvocations, myHash);

		Path cachedFile = storageManager.getCachedDataPath(storageData, myHash);
		assertThat(Files.exists(cachedFile), is(true));

		try (InputStream inputStream = Files.newInputStream(cachedFile, StandardOpenOption.READ)) {
			Input input = new Input(inputStream);
			List<InvocationSequenceData> deserialized = (List<InvocationSequenceData>) serializer.deserialize(input);
			assertThat(deserialized, is(equalTo(createdInvocations)));
		}

		String pathHttp = storageManager.getCachedStorageDataFileLocation(storageData, myHash);
		assertThat(pathHttp, is(notNullValue()));

		Files.deleteIfExists(cachedFile);

		pathHttp = storageManager.getCachedStorageDataFileLocation(storageData, myHash);
		assertThat(pathHttp, is(nullValue()));
	}

	/**
	 * Test that the storage can not be opened after it has been finalized.
	 */
	@Test(dependsOnMethods = { "finalizeWriteTest" }, expectedExceptions = { BusinessException.class })
	public void canNotOpenClosed() throws IOException, SerializationException, BusinessException {
		storageManager.openStorage(storageData);
	}

	/**
	 * Tests reading of data from created storage using our ExtendedByteBufferInputStream.
	 *
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test(dependsOnMethods = { "finalizeWriteTest" }, invocationCount = 5)
	public void readUsingExtendedByteBufferInputStream() throws SerializationException, IOException {
		if (storageIndexingTree == null) {
			return;
		}

		StorageIndexQuery query = new StorageIndexQuery();
		List<Class<?>> searchedClasses = new ArrayList<>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);

		List<IStorageDescriptor> descriptors = storageIndexingTree.query(query);
		assertThat("Amount of descriptors is less than the amount of invocations saved.", descriptors.size(), is(equalTo(createdInvocations.size())));
		for (IStorageDescriptor descriptor : descriptors) {
			assertThat("position of descriptor is negative.", descriptor.getPosition(), is(greaterThanOrEqualTo(0L)));
			assertThat("Size of the descriptor is wrong.", descriptor.getSize(), is(greaterThan(0L)));
		}

		int count = 0;
		try (InputStream result = inputStreamProvider.getExtendedByteBufferInputStream(storageData, descriptors);) {
			Input input = new Input(result);
			while (KryoUtil.hasMoreBytes(input)) {
				Object invocation = serializer.deserialize(input);
				assertThat(invocation, is(instanceOf(InvocationSequenceData.class)));
				assertThat(createdInvocations, hasItem((InvocationSequenceData) invocation));
				count++;
			}
		}
		assertThat("Amount of de-serialize objects is less than the amount of invocations saved.", count, is(equalTo(createdInvocations.size())));
	}

	/**
	 * Tests reading of data from created storage using the NIO streams.
	 *
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @see Files#newInputStream(Path, java.nio.file.OpenOption...)
	 */
	@Test(dependsOnMethods = { "finalizeWriteTest" })
	public void readUsingNioStream() throws SerializationException, IOException {
		if (storageIndexingTree == null) {
			return;
		}

		StorageIndexQuery query = new StorageIndexQuery();
		List<Class<?>> searchedClasses = new ArrayList<>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);

		List<IStorageDescriptor> descriptors = storageIndexingTree.query(query);
		assertThat("Amount of descriptors is less than the amount of invocations saved.", descriptors.size(), is(equalTo(createdInvocations.size())));
		for (IStorageDescriptor descriptor : descriptors) {
			assertThat("position of descriptor is negative.", descriptor.getPosition(), is(greaterThanOrEqualTo(0L)));
			assertThat("Size of the descriptor is wrong.", descriptor.getSize(), is(greaterThan(0L)));
		}
		Set<Path> allPaths = new HashSet<>();
		for (IStorageDescriptor desc : descriptors) {
			Path absolutePath = storageManager.getChannelPath(storageData, desc.getChannelId()).toAbsolutePath();
			allPaths.add(absolutePath);
		}

		int count = 0;
		for (Path path : allPaths) {
			try (InputStream result = Files.newInputStream(path, StandardOpenOption.READ)) {
				Input input = new Input(result);
				while (KryoUtil.hasMoreBytes(input)) {
					Object invocation = serializer.deserialize(input);
					assertThat(invocation, is(instanceOf(InvocationSequenceData.class)));
					assertThat(createdInvocations, hasItem((InvocationSequenceData) invocation));
					count++;
				}
			}
		}
		assertThat("Amount of de-serialize objects is less than the amount of invocations saved.", count, is(equalTo(createdInvocations.size())));
	}

	/**
	 * Test adding/removing of labels to a {@link StorageData} and successful saving to the disk.
	 *
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws BusinessException
	 */
	@Test
	public void testStorageLabels() throws IOException, SerializationException, BusinessException {
		RatingLabelType ratingLabelType = new RatingLabelType();
		StringStorageLabel label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating");

		// test add
		storageManager.addLabelToStorage(storageData, label, true);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				assertThat(storageToTest.isLabelPresent(ratingLabelType), is(true));
				assertThat(storageToTest.getLabels(ratingLabelType).size(), is(equalTo(1)));
				assertThat((StringStorageLabel) storageToTest.getLabels(ratingLabelType).get(0), is(equalTo(label)));
			}
		}

		// test overwrite
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating1");
		storageManager.addLabelToStorage(storageData, label, true);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				assertThat(storageToTest.isLabelPresent(ratingLabelType), is(true));
				assertThat(storageToTest.getLabels(ratingLabelType).size(), is(equalTo(1)));
				assertThat((StringStorageLabel) storageToTest.getLabels(ratingLabelType).get(0), is(equalTo(label)));
			}
		}

		// test no overwrite
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating2");
		storageManager.addLabelToStorage(storageData, label, false);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				assertThat(storageToTest.isLabelPresent(ratingLabelType), is(true));
				assertThat(storageToTest.getLabels(ratingLabelType).size(), is(equalTo(1)));
				assertThat((StringStorageLabel) storageToTest.getLabels(ratingLabelType).get(0), is(not(equalTo(label))));
			}
		}

		// test remove
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating1");
		assertThat(storageManager.removeLabelFromStorage(storageData, label), is(true));
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				assertThat(storageToTest.isLabelPresent(ratingLabelType), is(false));
				assertThat(storageToTest.getLabels(ratingLabelType), is(empty()));
			}
		}
	}

	/**
	 * Deletes created files after the test.
	 */
	@AfterTest
	public void deleteResources() {
		File storageFolder = getStorageFolder();
		if (storageFolder.exists()) {
			File[] files = storageFolder.listFiles();
			for (File file : files) {
				assertThat("Can not delete storage test file.", file.delete(), is(true));
			}
			assertThat("Can not delete storage test folder.", storageFolder.delete(), is(true));
		}
	}

	/**
	 * Returns storage folder.
	 *
	 * @return Returns storage folder.
	 */
	private File getStorageFolder() {
		return new File(storageManager.getStorageDefaultFolder() + File.separator + storageData.getStorageFolder() + File.separator);
	}

	/**
	 * @return Returns random storage data instance.
	 */
	private static StorageData getStorageData() {
		StorageData storageData = new StorageData();
		storageData.setName("My storage");
		return storageData;
	}

	/**
	 *
	 * @return One {@link SqlStatementData} with random values.
	 */
	private static SqlStatementData getSqlStatementInstance() {
		Random random = new Random();
		SqlStatementData sqlData = new SqlStatementData(new Timestamp(random.nextLong()), random.nextLong(), random.nextLong(), random.nextLong(), "New Sql String");
		sqlData.setCount(random.nextLong());
		sqlData.setCpuDuration(random.nextDouble());
		sqlData.calculateCpuMax(random.nextDouble());
		sqlData.calculateCpuMin(random.nextDouble());
		sqlData.setDuration(random.nextDouble());
		sqlData.setExclusiveCount(random.nextLong());
		sqlData.setExclusiveDuration(random.nextDouble());
		sqlData.calculateExclusiveMax(random.nextDouble());
		sqlData.calculateExclusiveMin(random.nextDouble());
		sqlData.setId(random.nextLong());
		sqlData.addInvocationParentId(random.nextLong());
		sqlData.setPreparedStatement(true);
		return sqlData;
	}

	/**
	 * Returns the random {@link InvocationSequenceData} instance.
	 *
	 * @param childCount
	 *            Desired child count.
	 * @return {@link InvocationSequenceData} instance.
	 */
	private static InvocationSequenceData getInvocationSequenceDataInstance(int childCount) {
		Random random = new Random();
		InvocationSequenceData invData = new InvocationSequenceData(new Timestamp(random.nextLong()), random.nextLong(), random.nextLong(), random.nextLong());
		invData.setDuration(random.nextDouble());
		invData.setId(random.nextLong());
		invData.setEnd(random.nextDouble());
		invData.setSqlStatementData(getSqlStatementInstance());
		if (childCount == 0) {
			return invData;
		}

		List<InvocationSequenceData> children = new ArrayList<>();
		for (int i = 0; i < childCount;) {
			int childCountForChild = childCount / 10;
			if ((childCountForChild + i + 1) > childCount) {
				childCountForChild = childCount - i - 1;
			}
			InvocationSequenceData child = getInvocationSequenceDataInstance(childCountForChild);
			child.setSqlStatementData(getSqlStatementInstance());
			child.setParentSequence(invData);
			children.add(child);
			i += childCountForChild + 1;

		}
		invData.setChildCount(childCount);
		invData.setNestedSequences(children);
		return invData;
	}

}
