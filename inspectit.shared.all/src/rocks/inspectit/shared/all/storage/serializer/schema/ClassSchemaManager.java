package rocks.inspectit.shared.all.storage.serializer.schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;

/**
 * {@link ClassSchemaManager} holds all schemas that are defined, and provides them to the
 * {@link SerializationManager}.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ClassSchemaManager implements InitializingBean {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Default directory location of all schemas.
	 */
	public static final String SCHEMA_DIR = "schema";

	/**
	 * The file name where list of schemas are. This file is supposed to be in default schema
	 * directory.
	 */
	public static final String SCHEMA_LIST_FILE = "schemaList.txt";

	/**
	 * Schemas mapped to the class names.
	 */
	private Map<String, ClassSchema> schemaMap = new HashMap<String, ClassSchema>();

	/**
	 * Resource of the schema list file.
	 */
	@Value("classpath:" + SCHEMA_DIR + "/" + SCHEMA_LIST_FILE)
	private Resource schemaListFile;

	/**
	 * Adds one {@link ClassSchema} to the {@link ClassSchemaManager}.
	 * 
	 * @param schema
	 *            Schema to be added. Note that the class name inside schema has to be defined, for
	 *            schema to be added.
	 */
	public void addSchema(ClassSchema schema) {
		if (schema != null) {
			if (schemaMap == null) {
				schemaMap = new HashMap<String, ClassSchema>();
			}
			schemaMap.put(schema.getClassName(), schema);
		}
	}

	/**
	 * Gets {@link ClassSchema} for a class name.
	 * 
	 * @param className
	 *            Name of the class schema is needed.
	 * @return Schema or null if schema for the class is not present in schema manager.
	 */
	public ClassSchema getSchema(String className) {
		return schemaMap.get(className);
	}

	/**
	 * Loads schemas.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void loadSchemasFromLocations() throws IOException {
		log.info("||-Class Schema Manager started..");

		InputStream is = schemaListFile.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		ClassLoader classLoader = ClassSchemaManager.class.getClassLoader();

		String schemaLocation = br.readLine();
		while (null != schemaLocation) {
			InputStream inputStream = classLoader.getResourceAsStream(schemaLocation);
			if (null == inputStream) {
				throw new IllegalArgumentException("Schema file '" + schemaLocation + "' can not be found.");
			}
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			loadSchemaFromReader(bufferedReader);

			schemaLocation = br.readLine();

			if (null != bufferedReader) {
				bufferedReader.close();
			}
			if (null != inputStreamReader) {
				inputStreamReader.close();
			}
			if (null != inputStream) {
				inputStream.close();
			}
		}

		if (null != br) {
			br.close();
		}
		if (null != isr) {
			isr.close();
		}
		if (null != is) {
			is.close();
		}
	}

	/**
	 * Loads all schemas from a given directory.
	 * 
	 * @param directory
	 *            File that points to the directory.
	 */
	protected void loadSchemas(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Schemas can not be loaded. Provided file is not a directory.");
		}

		File[] files = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(ClassSchema.SCHEMA_EXT);
			}
		});

		loadSchemas(files);
	}

	/**
	 * Load schemas from schemas files.
	 * 
	 * @param files
	 *            Array of schema files.
	 */
	protected void loadSchemas(File[] files) {
		for (File file : files) {
			FileReader fileReader = null;
			BufferedReader reader = null;
			try {
				fileReader = new FileReader(file);
				reader = new BufferedReader(fileReader);
				loadSchemaFromReader(reader);
			} catch (IOException exception) {
				log.warn("Exception occurred during reading the schema file.", exception);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (fileReader != null) {
						fileReader.close();
					}
				} catch (IOException exception) {
					log.warn("Exception occurred trying to close the schema file,", exception);
				}
			}
		}
	}

	/**
	 * Loads schema from a {@link BufferedReader}. It is responsibility of a caller to close the
	 * reader.
	 * 
	 * @param reader
	 *            BufferReader.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	private void loadSchemaFromReader(BufferedReader reader) throws IOException {
		if (reader != null) {
			Map<String, String> schemaInitMap = new HashMap<String, String>();
			String line = reader.readLine();
			while (line != null) {
				if (line.length() > 0 && line.charAt(0) != '#') {
					String[] tokens = line.split(":");
					if (tokens.length == 2) {
						schemaInitMap.put(tokens[0].trim(), tokens[1].trim());
					}
				}
				line = reader.readLine();
			}
			ClassSchema schema = new ClassSchema(schemaInitMap);
			this.addSchema(schema);
			if (log.isDebugEnabled()) {
				log.info("||-Successfully loaded schema for class " + schema.getClassName());
			}
		}
	}

	/**
	 * @return the schemaMap Returns the unmodifiable schema map.
	 */
	protected Map<String, ClassSchema> getSchemaMap() {
		return Collections.unmodifiableMap(schemaMap);
	}

	/**
	 * @param schemaListFile
	 *            the schemaListFile to set
	 */
	public void setSchemaListFile(Resource schemaListFile) {
		this.schemaListFile = schemaListFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		loadSchemasFromLocations();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("schemaMap", schemaMap);
		return toStringBuilder.toString();
	}

}
