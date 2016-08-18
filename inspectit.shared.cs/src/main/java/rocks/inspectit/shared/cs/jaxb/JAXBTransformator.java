package rocks.inspectit.shared.cs.jaxb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.xml.sax.SAXException;

import com.esotericsoftware.minlog.Log;

/**
 * Simple component for marshall and unmarshall operation on JAXB.
 *
 * @author Ivan Senic
 *
 */
public class JAXBTransformator {

	/**
	 * Logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(JAXBTransformator.class);

	/**
	 * Marshals the object to the given path that must represent a path to the file.
	 *
	 * @param path
	 *            Path to file
	 * @param object
	 *            Object to marshal
	 * @param noNamespaceSchemaLocation
	 *            NoNamespaceSchemaLocation to set. If it's <code>null</code> no location will be
	 *            set.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void marshall(Path path, Object object, String noNamespaceSchemaLocation) throws JAXBException, IOException {
		marshall(path, object, noNamespaceSchemaLocation, 0);
	}

	/**
	 * Marshals the object to the given path that must represent a path to the file.
	 * <p>
	 * This method is capable of setting the schema version to the object being marshalled.
	 *
	 * @param path
	 *            Path to file
	 * @param object
	 *            Object to marshal
	 * @param noNamespaceSchemaLocation
	 *            NoNamespaceSchemaLocation to set. If it's <code>null</code> no location will be
	 *            set.
	 * @param schemaVersion
	 *            If schema version is set and object to marshall is instance of
	 *            {@link ISchemaVersionAware} then the given schema version will be set to the
	 *            object. Use <code>0</code> to ignore setting of schema version.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void marshall(Path path, Object object, String noNamespaceSchemaLocation, int schemaVersion) throws JAXBException, IOException {
		if (Files.isDirectory(path)) {
			throw new IOException("Can not marshal object to the path that represents the directory");
		}
		Files.deleteIfExists(path);
		Files.createDirectories(path.getParent());

		JAXBContext context = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		if (null != noNamespaceSchemaLocation) {
			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, noNamespaceSchemaLocation);
		}

		// set schema version if needed
		if ((object instanceof ISchemaVersionAware) && (0 != schemaVersion)) {
			((ISchemaVersionAware) object).setSchemaVersion(schemaVersion);
		}

		try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
			marshaller.marshal(object, outputStream);
		}
	}

	/**
	 * Marshals the object to the bytes.
	 *
	 * @param object
	 *            Object to marshal
	 * @param noNamespaceSchemaLocation
	 *            NoNamespaceSchemaLocation to set. If it's <code>null</code> no location will be
	 *            set.
	 * @return bytes representing the results of the marshall operation
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public byte[] marshall(Object object, String noNamespaceSchemaLocation) throws JAXBException, IOException {
		return marshall(object, noNamespaceSchemaLocation, 0);
	}

	/**
	 * Marshals the object to the bytes.
	 *
	 * @param object
	 *            Object to marshal
	 * @param noNamespaceSchemaLocation
	 *            NoNamespaceSchemaLocation to set. If it's <code>null</code> no location will be
	 *            set.
	 * @param schemaVersion
	 *            If schema version is set and object to marshall is instance of
	 *            {@link ISchemaVersionAware} then the given schema version will be set to the
	 *            object. Use <code>0</code> to ignore setting of schema version.
	 * @return bytes representing the results of the marshall operation
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public byte[] marshall(Object object, String noNamespaceSchemaLocation, int schemaVersion) throws JAXBException, IOException {
		JAXBContext context = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		if (null != noNamespaceSchemaLocation) {
			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, noNamespaceSchemaLocation);
		}

		// set schema version if needed
		if ((object instanceof ISchemaVersionAware) && (0 != schemaVersion)) {
			((ISchemaVersionAware) object).setSchemaVersion(schemaVersion);
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			marshaller.marshal(object, outputStream);
			return outputStream.toByteArray();
		}
	}

	/**
	 * Unmarshalls the given file. The root class of the XML must be given.
	 * <p>
	 * No migration will be tried if schema validation fails.
	 *
	 * @param <T>
	 *            Type of root object.
	 * @param path
	 *            Path to file to unmarshall.
	 * @param schemaPath
	 *            Path to the XSD schema that will be used to validate the XML file. If no schema is
	 *            provided no validation will be performed.
	 * @param rootClass
	 *            Root class of the XML document.
	 * @return Unmarshalled object.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	public <T> T unmarshall(Path path, Path schemaPath, Class<T> rootClass) throws JAXBException, IOException, SAXException {
		return this.unmarshall(path, schemaPath, 0, null, rootClass);
	}

	/**
	 * Unmarshalls the given file. The root class of the XML must be given.
	 * <p>
	 * This method allows migration of the specified XML file to the wanted target schema version
	 * using the files in the migration path (if possible).
	 *
	 * @param <T>
	 *            Type of root object.
	 * @param path
	 *            Path to file to unmarshall.
	 * @param schemaPath
	 *            Path to the XSD schema that will be used to validate the XML file. If no schema is
	 *            provided no validation will be performed.
	 * @param targetSchemaVersion
	 *            The current schema version that is used as target.
	 * @param migrationPath
	 *            Path that contains the XSLT migration files to use if schema validation fails.
	 * @param rootClass
	 *            Root class of the XML document.
	 * @return Unmarshalled object.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshall(Path path, Path schemaPath, int targetSchemaVersion, Path migrationPath, Class<T> rootClass) throws JAXBException, IOException, SAXException {
		if (Files.notExists(path) || Files.isDirectory(path)) {
			return null;
		}

		// check if we need to migrate
		InputStream inputStream = null;
		if (null != migrationPath) {
			int xmlVersion = getSchemaVersion(Files.newInputStream(path, StandardOpenOption.READ), 0);

			// migrate if versions differ
			if (xmlVersion != targetSchemaVersion) {
				try {
					inputStream = migrate(Files.newInputStream(path, StandardOpenOption.READ), migrationPath, xmlVersion, targetSchemaVersion);
				} catch (TransformerException e) {
					LOG.warn("Migration of the file " + path.toAbsolutePath().toString() + " failed.", e);
				}
			}
		}

		// no matter of migration, if here we have null as stream use original
		if (null == inputStream) {
			inputStream = Files.newInputStream(path, StandardOpenOption.READ);
		}

		Unmarshaller unmarshaller = getUnmarshaller(schemaPath, rootClass);

		try {
			return (T) unmarshaller.unmarshal(inputStream);
		} finally {
			inputStream.close();
		}
	}

	/**
	 * Unmarshalls the bytes. The root class of the XML must be given.
	 * <p>
	 * No migration will be tried if schema validation fails.
	 *
	 * @param <T>
	 *            Type of root object.
	 * @param data
	 *            bytes
	 * @param schemaPath
	 *            Path to the XSD schema that will be used to validate the XML file. If no schema is
	 *            provided no validation will be performed.
	 * @param rootClass
	 *            Root class of the XML document.
	 * @return Unmarshalled object.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	public <T> T unmarshall(byte[] data, Path schemaPath, Class<T> rootClass) throws JAXBException, IOException, SAXException {
		return this.unmarshall(data, schemaPath, 0, null, rootClass);
	}

	/**
	 * Unmarshalls the bytes. The root class of the XML must be given.
	 * <p>
	 * This method allows migration of the specified XML file to the wanted target schema version
	 * using the files in the migration path (if possible).
	 *
	 * @param <T>
	 *            Type of root object.
	 * @param data
	 *            bytes
	 * @param schemaPath
	 *            Path to the XSD schema that will be used to validate the XML file. If no schema is
	 *            provided no validation will be performed.
	 * @param targetSchemaVersion
	 *            The current schema version that is used as target.
	 * @param migrationPath
	 *            Path that contains the XSLT migration files to use if schema validation fails.
	 * @param rootClass
	 *            Root class of the XML document.
	 * @return Unmarshalled object.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshall(byte[] data, Path schemaPath, int targetSchemaVersion, Path migrationPath, Class<T> rootClass) throws JAXBException, IOException, SAXException {
		// check if we need to migrate
		InputStream inputStream = null;
		if (null != migrationPath) {
			int xmlVersion = getSchemaVersion(new ByteArrayInputStream(data), 0);

			// migrate if versions differ
			if (xmlVersion != targetSchemaVersion) {
				try {
					inputStream = migrate(new ByteArrayInputStream(data), migrationPath, xmlVersion, targetSchemaVersion);
				} catch (TransformerException e) {
					LOG.warn("Migration of the data bytes failed.", e);
				}
			}
		}

		// no matter of migration, if here we have null as stream use original
		if (null == inputStream) {
			inputStream = new ByteArrayInputStream(data);
		}

		Unmarshaller unmarshaller = getUnmarshaller(schemaPath, rootClass);

		try {
			return (T) unmarshaller.unmarshal(inputStream);
		} finally {
			inputStream.close();
		}
	}


	/**
	 * Tries to find the first <code>schemaVersion=""</code> attribute in the XML and returns value
	 * inside as integer.
	 *
	 * @param xmlInputStream
	 *            Stream to read from.
	 * @param defaultValue
	 *            default value to return to caller if version could not be found or attribute does
	 *            not have an integer number.
	 * @return Version attribute value in first element found.
	 */
	private int getSchemaVersion(InputStream xmlInputStream, int defaultValue) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(xmlInputStream))) {
			String line = reader.readLine();
			while (null != line) {
				// skip XML def line
				if (!line.startsWith("<?xml")) {
					Pattern pattern = Pattern.compile(".*schemaVersion=\"([0-9]*\\.?[0-9]+)\".*");
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						try {
							return (int) Double.parseDouble(matcher.group(1));
						} catch (NumberFormatException e) { // NOPMD //NOCHK
							// ignore
						}
					}
				}
				line = reader.readLine();
			}
			return defaultValue;
		} catch (IOException e) {
			LOG.warn("Error trying to get the schema version attribute.", e);
			return defaultValue;
		}
	}

	/**
	 * Creates new {@link Unmarshaller} for the given root class.
	 * <p>
	 * If the schema path is given then the schema will be set to the {@link Unmarshaller} for the
	 * validation.
	 *
	 * @param schemaPath
	 *            Path to the XSD schema that will be used in {@link Unmarshaller} for the
	 *            validation.
	 * @param rootClass
	 *            Root class.
	 * @return {@link Unmarshaller} instance.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs due to the root class not being valid..
	 * @throws IOException
	 *             If {@link IOException} occurs during loading of the schema file.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	private Unmarshaller getUnmarshaller(Path schemaPath, Class<?> rootClass) throws JAXBException, IOException, SAXException {
		JAXBContext context = JAXBContext.newInstance(rootClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		if ((null != schemaPath) && Files.exists(schemaPath)) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try (InputStream inputStream = Files.newInputStream(schemaPath, StandardOpenOption.READ)) {
				Schema schema = sf.newSchema(new StreamSource(inputStream));
				unmarshaller.setSchema(schema);
			}
		}

		return unmarshaller;
	}

	/**
	 * Tries to migrate the XML contained in the given {@link InputStream} with the XSLT files
	 * contained in the migration path. If migration is successful the migrated XML is returned as
	 * content of the input stream.
	 *
	 * @param inputStream
	 *            Input stream of the original XML.
	 * @param migrationPath
	 *            Path containing migration XSLT file(s).
	 * @param fromVersion
	 *            Version to migrate from.
	 * @param toVersion
	 *            Version to migrate to.
	 * @return Unmarshalled object after XML migration, or <code>null</code> if migrating is not
	 *         possible due to the
	 * @throws TransformerException
	 *             If {@link TransformerException} occurs during transforming of the XML.
	 * @throws IOException
	 *             If migration file(s) can not be loaded.
	 * @throws JAXBException
	 *             If unmarshall fails with migrated XML.
	 */
	private InputStream migrate(InputStream inputStream, Path migrationPath, int fromVersion, int toVersion) throws TransformerException, IOException, JAXBException {
		// get migration paths
		List<Path> migrationFiles = getMigrationFiles(migrationPath);
		if (CollectionUtils.isEmpty(migrationFiles)) {
			Log.warn("Migration not possible. No migration file exists in " + migrationPath.toAbsolutePath().toString());
			return null;
		}

		// cut the list based on the wanted versions, we expect sorted already
		try {
			migrationFiles = migrationFiles.subList(fromVersion, toVersion);
		} catch (IndexOutOfBoundsException e) {
			Log.warn("Migration not possible, try to migrate from version " + fromVersion + " to version " + toVersion + ". Migration files avaliable is " + migrationFiles.size());
			return null;
		}

		// create transformer factory
		TransformerFactory factory = TransformerFactory.newInstance();
		// input stream for the migration
		InputStream xmlInputStream = inputStream;
		for (Path migrationFile : migrationFiles) {
			// take migration xslt file
			try (InputStream xsltInputStream = Files.newInputStream(migrationFile, StandardOpenOption.READ); InputStream is = xmlInputStream) {
				// create sources (both xslt and xml to migrate)
				Source xsltSource = new StreamSource(xsltInputStream);
				Source toMigrate = new StreamSource(is);

				// output stream for writing results
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Result result = new StreamResult(outputStream);

				// transform
				Transformer transformer = factory.newTransformer(xsltSource);
				transformer.transform(toMigrate, result);

				// and save the result as the input for the next migration or unmarshaling
				xmlInputStream = new ByteArrayInputStream(outputStream.toByteArray());
			}
		}

		// return migrated input stream
		return xmlInputStream;
	}

	/**
	 * Collects the files from given directory if it exists. Files will be sorted by name.
	 *
	 * @param migrationPath
	 *            Path
	 * @return Sorted files
	 * @throws IOException
	 *             If directory stream fails
	 */
	private List<Path> getMigrationFiles(Path migrationPath) throws IOException {
		if ((null == migrationPath) || !Files.exists(migrationPath) || !Files.isDirectory(migrationPath)) {
			return Collections.emptyList();
		}

		// get directory stream and add all files
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(migrationPath);
		List<Path> migrationsFiles = new ArrayList<>();
		for (Path migrationFile : directoryStream) {
			if (!Files.isDirectory(migrationFile)) {
				migrationsFiles.add(migrationFile);
			}
		}
		// sort by name
		Collections.sort(migrationsFiles, new Comparator<Path>() {
			@Override
			public int compare(Path o1, Path o2) {
				return o1.toAbsolutePath().toString().compareTo(o2.toAbsolutePath().toString());
			}
		});
		return migrationsFiles;
	}

}
