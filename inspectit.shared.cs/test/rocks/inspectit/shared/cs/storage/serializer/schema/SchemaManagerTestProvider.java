package info.novatec.inspectit.storage.serializer.schema;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * A test utility class that provides instances of {@link ClassSchemaManager} for testing purposes.
 * 
 * @author Ivan Senic
 * 
 */
public final class SchemaManagerTestProvider {

	/**
	 * Private constructor.
	 */
	private SchemaManagerTestProvider() {
	}

	/**
	 * Returns properly instantiated {@link ClassSchemaManager} that can be used in tests.
	 * 
	 * @return Returns properly instantiated {@link ClassSchemaManager} that can be used in tests.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public static ClassSchemaManager getClassSchemaManagerForTests() throws IOException {
		ClassSchemaManager schemaManager = new ClassSchemaManager();
		schemaManager.log = LoggerFactory.getLogger(ClassSchemaManager.class);
		schemaManager.setSchemaListFile(new ClassPathResource(ClassSchemaManager.SCHEMA_DIR + "/" + ClassSchemaManager.SCHEMA_LIST_FILE, schemaManager.getClass().getClassLoader()));
		schemaManager.loadSchemasFromLocations();
		return schemaManager;
	}
}
