package rocks.inspectit.shared.cs.storage.serializer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.CmrStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.RemoteException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.storage.serializer.ISerializer;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.storage.serializer.schema.ClassSchemaManager;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.KryoNetNetwork;
import rocks.inspectit.shared.cs.communication.data.cmr.RecordingData;
import rocks.inspectit.shared.cs.indexing.indexer.impl.InvocationChildrenIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.MethodIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.ObjectTypeIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.PlatformIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.SensorTypeIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.SqlStringIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.TimestampIndexer;
import rocks.inspectit.shared.cs.indexing.storage.impl.ArrayBasedStorageLeaf;
import rocks.inspectit.shared.cs.indexing.storage.impl.SimpleStorageDescriptor;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageBranch;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageBranchIndexer;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.label.BooleanStorageLabel;
import rocks.inspectit.shared.cs.storage.label.DateStorageLabel;
import rocks.inspectit.shared.cs.storage.label.NumberStorageLabel;
import rocks.inspectit.shared.cs.storage.label.StringStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.impl.AssigneeLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CreationDateLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CustomBooleanLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CustomDateLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CustomNumberLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CustomStringLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.ExploredByLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.RatingLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.StatusLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.UseCaseLabelType;
import rocks.inspectit.shared.cs.storage.serializer.SerializationManagerPostProcessor;

/**
 * Test the implementation of the {@link ISerializer} for correctness.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SerializerTest extends TestBase {

	/**
	 * Classes to be tested in the {@link #testClassesForPlanSerialization(Class)}, so to be sure
	 * that every class can be serialized by our Kryo implementation.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TimerData.class }, { SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class },
		{ ClassLoadingInformationData.class }, { CompilationInformationData.class }, { MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class },
		{ ThreadInformationData.class }, { HttpTimerData.class }, { ParameterContentData.class }, { VmArgumentData.class }, { PlatformIdent.class }, { MethodIdent.class },
		{ MethodSensorTypeIdent.class }, { MethodIdentToSensorType.class }, { PlatformSensorTypeIdent.class }, { SimpleStorageDescriptor.class }, { ArrayBasedStorageLeaf.class },
		{ StorageData.class }, { LocalStorageData.class }, { PlatformIdentIndexer.class }, { ObjectTypeIndexer.class }, { MethodIdentIndexer.class }, { SensorTypeIdentIndexer.class },
		{ TimestampIndexer.class }, { InvocationChildrenIndexer.class }, { StorageBranch.class }, { StorageBranchIndexer.class }, { BooleanStorageLabel.class }, { DateStorageLabel.class },
		{ NumberStorageLabel.class }, { StringStorageLabel.class }, { AssigneeLabelType.class }, { CreationDateLabelType.class }, { CustomBooleanLabelType.class }, { CustomDateLabelType.class },
		{ CustomNumberLabelType.class }, { CustomStringLabelType.class }, { ExploredByLabelType.class }, { RatingLabelType.class }, { StatusLabelType.class }, { UseCaseLabelType.class },
		{ SqlStringIndexer.class }, { BooleanStorageLabel.class }, { DateStorageLabel.class }, { NumberStorageLabel.class }, { StringStorageLabel.class }, { CustomDateLabelType.class },
		{ CmrStatusData.class }, { AgentStatusData.class }, { RecordingData.class }, { CustomBooleanLabelType.class }, { CustomNumberLabelType.class }, { CustomStringLabelType.class },
		{ AssigneeLabelType.class }, { RatingLabelType.class }, { ExploredByLabelType.class }, { CreationDateLabelType.class }, { StatusLabelType.class }, { UseCaseLabelType.class },
		{ AggregatedHttpTimerData.class }, { AggregatedSqlStatementData.class }, { AggregatedTimerData.class }, { ArrayBasedStorageLeaf.class } };

		/**
		 * Serializer.
		 */
		private SerializationManager serializer;

		/**
		 * Byte buffer.
		 */
		private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 20);

		@InjectMocks
		private ClassSchemaManager schemaManager;

		@Mock
		private Logger log;

		/**
		 * Instantiates the {@link SerializationManager}.
		 *
		 * @throws IOException
		 *             If {@link IOException} occurs.
		 */
		@BeforeMethod
		public void initSerializer() throws IOException {
			schemaManager.setSchemaListFile(new ClassPathResource(ClassSchemaManager.SCHEMA_DIR + "/" + ClassSchemaManager.SCHEMA_LIST_FILE, schemaManager.getClass().getClassLoader()));
			schemaManager.loadSchemasFromLocations();

			serializer = new SerializationManager();
			serializer.setSchemaManager(schemaManager);
			serializer.setKryoNetNetwork(new KryoNetNetwork());
			serializer.initKryo();

			SerializationManagerPostProcessor postProcessor = new SerializationManagerPostProcessor();
			postProcessor.postProcessAfterInitialization(serializer, "serializerTest");
		}

		/**
		 * Prepare the buffer before the test.
		 */
		@BeforeMethod
		public void prepareBuffer() {
			byteBuffer.clear();
		}

		/**
		 * Tests if the data deserialzied from empty buffer is <code>null</code>.
		 *
		 * @throws SerializationException
		 *             Serialization Exception
		 */
		@Test
		public void emptyBufferSerialization() throws SerializationException {
			// I need to create a new buffer, because clear on the buffer will not actually erase the
			// data in the buffer, but only move the pointers
			ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(1024);
			ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(newByteBuffer);
			Input input = new Input(byteBufferInputStream);
			Object data = serializer.deserialize(input);
			assertThat(data, is(nullValue()));
		}

		/**
		 * Tests if the data de-serialzed from buffer with random data is <code>null</code>.
		 *
		 * @throws SerializationException
		 *             Serialization Exception
		 */
		@Test
		public void radomBufferDataSerialization() throws SerializationException {

			for (int i = 0; i < 64; i++) {
				byteBuffer.putInt(i);
			}
			byteBuffer.flip();
			ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
			Input input = new Input(byteBufferInputStream);
			Object data = serializer.deserialize(input);
			assertThat(data, is(nullValue()));
		}

		/**
		 * Provides classes to be tested.
		 *
		 * @return Provides classes to be tested.
		 */
		@DataProvider(name = "classProvider")
		public Object[][] classProvider() {
			return TESTING_CLASSES;
		}

		/**
		 * Tests the class that extends the {@link DefaultData} class via reflection. Note that tested
		 * class can not be abstract.
		 *
		 * @param testingClass
		 *            Class to test.
		 * @throws InstantiationException
		 *             InstantiationException
		 * @throws IllegalAccessException
		 *             IllegalAccessException
		 * @throws SerializationException
		 *             SerializationException
		 */
		@Test(dataProvider = "classProvider")
		public void classesPlanSerialization(Class<?> testingClass) throws InstantiationException, IllegalAccessException, SerializationException {
			Object object = getInstanceWithPrimitiveFieldsSet(testingClass);
			Object deserialized = serializeBackAndForth(object);
			assertThat(deserialized, is(equalTo(object)));
		}

		@Test(dataProvider = "classProvider")
		public void copy(Class<?> testingClass) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
			Object object = getInstanceWithPrimitiveFieldsSet(testingClass);
			Object copy = serializer.copy(object);
			assertThat(copy, is(equalTo(object)));
			assertThat(copy == object, is(false));
		}

		private Object getInstanceWithPrimitiveFieldsSet(Class<?> testingClass) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
			Object object = testingClass.newInstance();

			for (Field field : testingClass.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				if (field.getType().equals(long.class)) {
					field.set(object, RandomUtils.nextLong());
				} else if (field.getType().equals(int.class)) {
					field.set(object, RandomUtils.nextInt());
				} else if (field.getType().equals(double.class)) {
					field.set(object, RandomUtils.nextDouble());
				} else if (field.getType().equals(boolean.class)) {
					field.set(object, RandomUtils.nextBoolean());
				} else if (field.getType().equals(String.class)) {
					field.set(object, RandomStringUtils.random(100));
				}
			}

			return object;
		}

		/**
		 * Tests the java collections. Compares array gotten from collections because the equal is
		 * missing in those collections.
		 */
		@Test(dataProvider = "javaCollectionsProvider")
		public void javaCollections(Collection<?> testCollection) throws SerializationException {
			Collection<?> deserialized = serializeBackAndForth(testCollection);
			assertThat(deserialized, is(instanceOf(testCollection.getClass())));
			assertThat(deserialized.toArray(), is(equalTo(testCollection.toArray())));
		}

		@DataProvider(name = "javaCollectionsProvider")
		public Object[][] javaCollectionsProvider() {
			List<Collection<?>> collections = new ArrayList<Collection<?>>();

			// unmodifiable empty
			collections.add(Collections.unmodifiableCollection(new ArrayList<Object>()));
			collections.add(Collections.unmodifiableList(new ArrayList<Object>()));
			collections.add(Collections.unmodifiableSet(new HashSet<Object>()));
			collections.add(Collections.unmodifiableSortedSet(new TreeSet<Object>()));

			// unmodifiable with one element
			collections.add(Collections.unmodifiableCollection(Collections.singleton("blub")));
			collections.add(Collections.unmodifiableList(Collections.singletonList("blub")));
			collections.add(Collections.unmodifiableSet(Collections.singleton("blub")));
			TreeSet<Object> treeSet = new TreeSet<Object>();
			treeSet.add("blub");
			collections.add(Collections.unmodifiableSortedSet(treeSet));

			// synchronized empty
			collections.add(Collections.synchronizedCollection(new ArrayList<Object>()));
			collections.add(Collections.synchronizedList(new ArrayList<Object>()));
			collections.add(Collections.synchronizedSet(new HashSet<Object>()));
			collections.add(Collections.synchronizedSortedSet(new TreeSet<Object>()));

			// synchronized with one element
			collections.add(Collections.synchronizedCollection(Collections.singleton("blub")));
			collections.add(Collections.synchronizedList(Collections.singletonList("blub")));
			collections.add(Collections.synchronizedSet(Collections.singleton("blub")));
			collections.add(Collections.synchronizedSortedSet(treeSet));

			// singletons
			collections.add(Collections.singleton("blub"));
			collections.add(Collections.singletonList("blub"));
			collections.add(Collections.singleton("blub"));

			Object[][] returnData = new Object[collections.size()][1];
			for (int i = 0; i < collections.size(); i++) {
				returnData[i][0] = collections.get(i);
			}
			return returnData;
		}

		/**
		 * Tests the java maps. Compares the entries of maps.
		 */
		@Test(dataProvider = "javaMapsProvider")
		public void javaMaps(Map<Object, Object> testMap) throws SerializationException {
			Map<?, ?> deserialized = serializeBackAndForth(testMap);
			assertThat(deserialized, is(instanceOf(testMap.getClass())));

			for (Entry<Object, Object> originalEntry : testMap.entrySet()) {
				assertThat(deserialized, hasEntry(originalEntry.getKey(), originalEntry.getValue()));
			}
		}

		@DataProvider(name = "javaMapsProvider")
		public Object[][] javaMapsProvider() {
			List<Map<?, ?>> maps = new ArrayList<Map<?, ?>>();

			// unmodifiable
			maps.add(Collections.unmodifiableMap(Collections.singletonMap("Key", "Value")));
			TreeMap<String, String> treeMap = new TreeMap<String, String>();
			treeMap.put("Key", "Value");
			maps.add(Collections.unmodifiableSortedMap(treeMap));

			// synchronized
			maps.add(Collections.synchronizedMap(Collections.singletonMap("Key", "Value")));
			maps.add(Collections.synchronizedSortedMap(treeMap));

			// singleton
			maps.add(Collections.singletonMap("Key", "Value"));

			Object[][] returnData = new Object[maps.size()][1];
			for (int i = 0; i < maps.size(); i++) {
				returnData[i][0] = maps.get(i);
			}
			return returnData;
		}

		/**
		 * Tests serialization of remote exception.
		 */
		@Test
		public void remoteException() throws SerializationException {
			Exception exception = new Exception("Cause message");
			RemoteException remoteException = new RemoteException(exception);
			Exception deserialized = serializeBackAndForth(remoteException);

			assertThat(deserialized, is(instanceOf(RemoteException.class)));
			assertThat(deserialized.getMessage(), is(equalTo(exception.getMessage())));
			assertThat(deserialized.getStackTrace(), is(equalTo(exception.getStackTrace())));
		}

		/**
		 * Test the Business exception.
		 */
		@Test
		public void businessException() throws SerializationException {
			BusinessException businessException = new BusinessException("Message", StorageErrorCodeEnum.CAN_NOT_START_RECORDING);
			businessException.printStackTrace();
			BusinessException deserialized = serializeBackAndForth(businessException);

			assertThat(deserialized, is(instanceOf(BusinessException.class)));
			assertThat(deserialized.getMessage(), is(equalTo(businessException.getMessage())));
			assertThat(deserialized.getStackTrace(), is(equalTo(businessException.getStackTrace())));
			assertThat(deserialized.getActionPerformed(), is(equalTo(businessException.getActionPerformed())));
			assertThat(deserialized.getErrorCode(), is(equalTo(businessException.getErrorCode())));
		}

		/**
		 * Test a {@link IOException} throw from another method.
		 */
		@Test
		public void thrownException() throws SerializationException {
			try {
				throwIOException();
			} catch (IOException original) {
				original.printStackTrace();
				Exception deserialized = serializeBackAndForth(original);
				assertThat(deserialized, is(instanceOf(original.getClass())));
				assertThat(deserialized.getMessage(), is(equalTo(original.getMessage())));
				assertThat(deserialized.getStackTrace(), is(equalTo(original.getStackTrace())));
			}
		}

		private void throwIOException() throws IOException {
			throw new IOException("Just for testing");
		}

		/**
		 * Performs the serialization of the given object to bytes and then performs de-serialization
		 * from those bytes and returns the de-serialized object back.
		 *
		 * @param original
		 *            Original object.
		 * @return De-serialized objects from bytes gotten from the serialization of original.
		 * @throws SerializationException
		 *             If serialization fails.
		 */
		@SuppressWarnings("unchecked")
		private <T> T serializeBackAndForth(Object original) throws SerializationException {
			ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
			Output output = new Output(byteBufferOutputStream);
			serializer.serialize(original, output);
			byteBuffer.flip();
			ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
			Input input = new Input(byteBufferInputStream);
			return (T) serializer.deserialize(input);

		}

}
