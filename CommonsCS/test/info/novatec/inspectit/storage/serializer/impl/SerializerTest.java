package info.novatec.inspectit.storage.serializer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.SimpleStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageBranch;
import info.novatec.inspectit.indexing.storage.impl.StorageBranchIndexer;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomBooleanLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomNumberLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomStringLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;
import info.novatec.inspectit.storage.serializer.schema.SchemaManagerTestProvider;
import info.novatec.inspectit.util.KryoNetNetwork;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Test the implementation of the {@link ISerializer} for correctness.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class SerializerTest {

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
			{ AggregatedHttpTimerData.class }, { AggregatedSqlStatementData.class }, { AggregatedTimerData.class } };

	/**
	 * Exceptions we want to test to be able to get serialized.
	 */
	private static final Object[][] EXCEPTION_CLASSES = new Object[][] { { StorageException.class }, { ServiceException.class } };

	/**
	 * Serializer.
	 */
	private SerializationManager serializer;

	/**
	 * Byte buffer.
	 */
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 20);

	/**
	 * Instantiates the {@link SerializationManager}.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@BeforeClass
	public void initSerializer() throws IOException {
		ClassSchemaManager schemaManager = SchemaManagerTestProvider.getClassSchemaManagerForTests();
		serializer = new SerializationManager();
		serializer.setSchemaManager(schemaManager);
		serializer.setKryoNetNetwork(new KryoNetNetwork());
		serializer.initKryo();
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
		Object object = testingClass.newInstance();
		Object deserialized = serializeBackAndForth(object);
		assertThat(deserialized, is(equalTo(object)));
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
	 * Tests our exception classes for serialization and de-serialization.
	 */
	@Test(dataProvider = "exceptionsProvider")
	public void exceptions(Class<? extends Exception> exceptionClass) throws SerializationException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Exception exception = new Exception("Cause message");
		Constructor<? extends Exception> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
		Exception realException = constructor.newInstance("Test message", exception);
		realException.printStackTrace();
		Exception deserialized = serializeBackAndForth(realException);

		// exceptions do not implement equalsTo
		// we need to manually prove
		assertThat(deserialized, is(instanceOf(exceptionClass)));
		assertThat(deserialized.getMessage(), is(equalTo(realException.getMessage())));
		assertThat(deserialized.getStackTrace(), is(equalTo(realException.getStackTrace())));
		assertThat(deserialized.getCause(), is(instanceOf(Exception.class)));
		assertThat(deserialized.getCause().getMessage(), is(equalTo(exception.getMessage())));
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "exceptionsProvider")
	public Object[][] exceptionsProvider() {
		return EXCEPTION_CLASSES;
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
