package rocks.inspectit.shared.cs.jaxb;

/**
 * Interface for XML types (POJOs) that are aware of their schema version.
 *
 * @author Ivan Senic
 *
 */
public interface ISchemaVersionAware {

	/**
	 * Sets the schema version. Usually called before marshaling.
	 *
	 * @param schemaVersion
	 *            version
	 */
	void setSchemaVersion(int schemaVersion);

	/**
	 * Information about schema version for the configuration interface.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface ConfigurationInterface {

		/** Current version. */
		int SCHEMA_VERSION = 5;
	}
}
