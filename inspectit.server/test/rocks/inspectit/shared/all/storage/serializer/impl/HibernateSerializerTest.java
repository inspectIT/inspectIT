package rocks.inspectit.shared.all.storage.serializer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.server.util.HibernateUtil;
import rocks.inspectit.shared.all.storage.serializer.ISerializer;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.serializer.schema.ClassSchemaManager;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.KryoNetNetwork;

/**
 * Test the implementation of the {@link ISerializer} for correctness.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class HibernateSerializerTest extends TestBase {

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
		serializer.hibernateUtil = new HibernateUtil();
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
	 * Tests that the Hibernate {@link PersistentList} can be serialized, but in way that
	 * deserialized class will be java list and but not {@link PersistentList}.
	 *
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentList() throws SerializationException {
		PersistentList object = new PersistentList();
		Object deserialized = serializeBackAndForth(object);
		assertThat(deserialized, is(not(instanceOf(PersistentList.class))));
		assertThat(deserialized, is(instanceOf(List.class)));
	}

	/**
	 * Tests that the Hibernate {@link PersistentSet} can be serialized, but in way that
	 * deserialized class will be java set and but not {@link PersistentSet}.
	 *
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentSet() throws SerializationException {
		PersistentSet object = new PersistentSet();
		Object deserialized = serializeBackAndForth(object);
		assertThat(deserialized, is(not(instanceOf(PersistentSet.class))));
		assertThat(deserialized, is(instanceOf(Set.class)));
	}

	/**
	 * Tests that the Hibernate {@link PersistentMap} can be serialized, but in way that
	 * deserialized class will be java map and but not {@link PersistentMap}.
	 *
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentMap() throws SerializationException {
		PersistentMap object = new PersistentMap();
		Object deserialized = serializeBackAndForth(object);
		assertThat(deserialized, is(not(instanceOf(PersistentMap.class))));
		assertThat(deserialized, is(instanceOf(Map.class)));
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
