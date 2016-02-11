package info.novatec.inspectit.storage.serializer.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests if the schemas for serialization are up-to-date. Any change of the domain classes, needs to
 * be reflected in the schema. Thus, this test should prove that all schemas are correct.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class SchemaTest {

	/**
	 * Schema manager.
	 */
	private ClassSchemaManager schemaManager;

	/**
	 * Initializes the {@link ClassSchemaManager}.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@BeforeClass
	public void init() throws IOException {
		schemaManager = SchemaManagerTestProvider.getClassSchemaManagerForTests();
	}

	/**
	 * Tests schemas for correctness.
	 * 
	 * @param className
	 *            Name of the class to test.
	 * @param schema
	 *            {@link ClassSchema} for given class.
	 * 
	 * @throws ClassNotFoundException
	 *             If class loading fails.
	 */
	@Test(dataProvider = "schemaProvider")
	public void checkClassFieldsWithSchema(String className, ClassSchema schema, List<String> excludedFields) throws ClassNotFoundException {
		// assert that schema gotten by the .getSchema is same
		assertThat(schemaManager.getSchema(className), is(equalTo(schema)));

		Class<?> clazz = Class.forName(className);
		Set<Integer> markerSet = new HashSet<Integer>();
		while (!clazz.equals(Object.class)) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !excludedFields.contains(field.getName())) {
					Integer marker = schema.getFieldMarker(field.getName());
					// assert that the field will be in schema and that it has different marker
					// than other fields
					assertThat("Field " + field.getName() + " of class " + className + " is not available in class schema.", marker, is(notNullValue()));
					assertThat("Same marker exists for two different fields in class " + className + ". Duplicated number for field " + field.getName(), markerSet.add(marker), is(true));
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * @return Schemas to test.
	 */
	@DataProvider(name = "schemaProvider")
	public Object[][] getSchemas() {
		Map<String, ClassSchema> schemasMap = schemaManager.getSchemaMap();
		Object[][] data = new Object[schemasMap.size()][3];
		int i = 0;
		for (Map.Entry<String, ClassSchema> entry : schemasMap.entrySet()) {
			List<String> excludedFields = new ArrayList<String>();
			if (entry.getKey().equals(InvocationSequenceData.class.getName())) {
				excludedFields.add("parentSequence");
			}

			data[i][0] = entry.getKey();
			data[i][1] = entry.getValue();
			data[i][2] = excludedFields;
			i++;
		}
		return data;
	}

}
