package info.novatec.inspectit.storage.serializer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.storage.serializer.schema.ClassSchema;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.util.Random;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

@SuppressWarnings("PMD")
public class BackwardForwardCompatibilityTest {

	@Mock
	private ClassSchemaManager classSchemaManager;

	@Mock
	private ClassSchema classSchema;

	private Random random = new Random();

	/**
	 * Init mocks.
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(classSchemaManager.getSchema(Mockito.anyString())).thenReturn(classSchema);
	}

	@Test
	public void allFields() {
		long longValue = random.nextLong();
		double doubleValue = random.nextDouble();
		float floatValue = random.nextFloat();

		TestClassLongDoubleFloat object = new TestClassLongDoubleFloat();
		object.longField = longValue;
		object.doubleField = doubleValue;
		object.floatField = floatValue;

		Mockito.when(classSchema.getFieldMarker("longField")).thenReturn(Integer.valueOf(1));
		Mockito.when(classSchema.getFieldMarker("doubleField")).thenReturn(Integer.valueOf(2));
		Mockito.when(classSchema.getFieldMarker("floatField")).thenReturn(Integer.valueOf(3));
		Kryo kryo = new Kryo();
		kryo.register(TestClassLongDoubleFloat.class, new CustomCompatibleFieldSerializer<TestClassLongDoubleFloat>(kryo, TestClassLongDoubleFloat.class, classSchemaManager));

		Output output = new Output(1024);
		kryo.writeClassAndObject(output, object);
		Input input = new Input(output.getBuffer());
		Object deserialized = kryo.readClassAndObject(input);

		Assert.assertTrue(deserialized instanceof TestClassLongDoubleFloat);
		TestClassLongDoubleFloat deserializedTestClass = (TestClassLongDoubleFloat) deserialized;
		assertThat(deserializedTestClass.longField, is(equalTo(longValue)));
		assertThat(deserializedTestClass.doubleField, is(equalTo(doubleValue)));
		assertThat(deserializedTestClass.floatField, is(equalTo(floatValue)));
	}

	@Test
	public void zeroField() {
		long longValue = random.nextLong();
		double doubleValue = random.nextDouble();
		float floatValue = random.nextFloat();

		TestClassLongDoubleFloat object = new TestClassLongDoubleFloat();
		object.longField = longValue;
		object.doubleField = doubleValue;
		object.floatField = floatValue;

		Mockito.when(classSchema.getFieldMarker("longField")).thenReturn(null);
		Mockito.when(classSchema.getFieldMarker("doubleField")).thenReturn(null);
		Mockito.when(classSchema.getFieldMarker("floatField")).thenReturn(null);
		Kryo kryo = new Kryo();
		kryo.register(TestClassLongDoubleFloat.class, new CustomCompatibleFieldSerializer<TestClassLongDoubleFloat>(kryo, TestClassLongDoubleFloat.class, classSchemaManager));

		Output output = new Output(1024);
		kryo.writeClassAndObject(output, object);
		Input input = new Input(output.getBuffer());
		Object deserialized = kryo.readClassAndObject(input);

		Assert.assertTrue(deserialized instanceof TestClassLongDoubleFloat);
		TestClassLongDoubleFloat deserializedTestClass = (TestClassLongDoubleFloat) deserialized;
		assertThat(deserializedTestClass.longField, is(equalTo(0L)));
		assertThat(deserializedTestClass.doubleField, is(equalTo(0D)));
		assertThat(deserializedTestClass.floatField, is(equalTo(0F)));
	}

	@Test
	public void addAndRemoveFields() {
		long longValue = random.nextLong();
		double doubleValue = random.nextDouble();

		TestClassLongDouble object = new TestClassLongDouble();
		object.longField = longValue;
		object.doubleField = doubleValue;

		Mockito.when(classSchema.getFieldMarker("longField")).thenReturn(Integer.valueOf(1));
		Mockito.when(classSchema.getFieldMarker("doubleField")).thenReturn(Integer.valueOf(2));

		Kryo kryo = new Kryo();
		kryo.register(TestClassLongDouble.class, new CustomCompatibleFieldSerializer<TestClassLongDouble>(kryo, TestClassLongDouble.class, classSchemaManager));

		Output output = new Output(1024);
		kryo.writeClassAndObject(output, object);

		Mockito.when(classSchema.getFieldMarker("floatField")).thenReturn(3);
		kryo = new Kryo();
		kryo.register(TestClassDoubleFloat.class, new CustomCompatibleFieldSerializer<TestClassDoubleFloat>(kryo, TestClassDoubleFloat.class, classSchemaManager));
		Input input = new Input(output.getBuffer());
		Object deserialized = kryo.readClassAndObject(input);

		assertThat(deserialized, is(instanceOf(TestClassDoubleFloat.class)));
		TestClassDoubleFloat deserializedTestClass = (TestClassDoubleFloat) deserialized;
		assertThat(deserializedTestClass.doubleField, is(equalTo(doubleValue)));
		assertThat(deserializedTestClass.floatField, is(equalTo(0F)));
	}

	@Test
	public void removeField() {
		long longValue = random.nextLong();
		double doubleValue = random.nextDouble();
		float floatValue = random.nextFloat();

		TestClassLongDoubleFloat object = new TestClassLongDoubleFloat();
		object.longField = longValue;
		object.doubleField = doubleValue;
		object.floatField = floatValue;

		Mockito.when(classSchema.getFieldMarker("longField")).thenReturn(Integer.valueOf(1));
		Mockito.when(classSchema.getFieldMarker("doubleField")).thenReturn(Integer.valueOf(2));
		Mockito.when(classSchema.getFieldMarker("floatField")).thenReturn(Integer.valueOf(3));
		Kryo kryo = new Kryo();
		kryo.register(TestClassLongDoubleFloat.class, new CustomCompatibleFieldSerializer<TestClassLongDoubleFloat>(kryo, TestClassLongDoubleFloat.class, classSchemaManager));

		Output output = new Output(1024);
		kryo.writeClassAndObject(output, object);

		kryo = new Kryo();
		kryo.register(TestClassLongDouble.class, new CustomCompatibleFieldSerializer<TestClassLongDouble>(kryo, TestClassLongDouble.class, classSchemaManager));
		Input input = new Input(output.getBuffer());
		Object deserialized = kryo.readClassAndObject(input);

		assertThat(deserialized, is(instanceOf(TestClassLongDouble.class)));
		TestClassLongDouble deserializedTestClass = (TestClassLongDouble) deserialized;
		assertThat(deserializedTestClass.longField, is(equalTo(longValue)));
		assertThat(deserializedTestClass.doubleField, is(equalTo(doubleValue)));
	}

	@Test
	public void addField() {
		long longValue = random.nextLong();
		double doubleValue = random.nextDouble();

		TestClassLongDouble object = new TestClassLongDouble();
		object.longField = longValue;
		object.doubleField = doubleValue;

		Mockito.when(classSchema.getFieldMarker("longField")).thenReturn(Integer.valueOf(1));
		Mockito.when(classSchema.getFieldMarker("doubleField")).thenReturn(Integer.valueOf(2));
		Kryo kryo = new Kryo();
		kryo.register(TestClassLongDouble.class, new CustomCompatibleFieldSerializer<TestClassLongDouble>(kryo, TestClassLongDouble.class, classSchemaManager));

		Output output = new Output(1024);
		kryo.writeClassAndObject(output, object);

		Mockito.when(classSchema.getFieldMarker("floatField")).thenReturn(Integer.valueOf(3));
		kryo = new Kryo();
		kryo.register(TestClassLongDoubleFloat.class, new CustomCompatibleFieldSerializer<TestClassLongDoubleFloat>(kryo, TestClassLongDoubleFloat.class, classSchemaManager));
		Input input = new Input(output.getBuffer());
		Object deserialized = kryo.readClassAndObject(input);

		assertThat(deserialized, is(instanceOf(TestClassLongDoubleFloat.class)));
		TestClassLongDoubleFloat deserializedTestClass = (TestClassLongDoubleFloat) deserialized;
		assertThat(deserializedTestClass.longField, is(equalTo(longValue)));
		assertThat(deserializedTestClass.doubleField, is(equalTo(doubleValue)));
		assertThat(deserializedTestClass.floatField, is(equalTo(0F)));
	}

	static class TestClassLongDoubleFloat {

		private long longField;

		private double doubleField;

		private float floatField;

	}

	static class TestClassLongDouble {

		private long longField;

		private double doubleField;

	}

	static class TestClassDoubleFloat {

		private double doubleField;

		private float floatField;

	}
}
