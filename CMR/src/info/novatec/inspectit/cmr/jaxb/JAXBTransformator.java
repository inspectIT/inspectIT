package info.novatec.inspectit.cmr.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Simple component for marshall and unmarshall operation on JAXB.
 * 
 * @author Ivan Senic
 * 
 */
public class JAXBTransformator {

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
		if (Files.isDirectory(path)) {
			throw new IOException("Can not marshal object to the path that represents the directory");
		}
		Files.deleteIfExists(path);

		JAXBContext context = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = context.createMarshaller();
		if (null != noNamespaceSchemaLocation) {
			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, noNamespaceSchemaLocation);
		}

		try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
			marshaller.marshal(object, outputStream);
		}
	}

	/**
	 * Unmarshalls the given file. The root class of the XML must be given.
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
	@SuppressWarnings("unchecked")
	public <T> T unmarshall(Path path, Path schemaPath, Class<T> rootClass) throws JAXBException, IOException, SAXException {
		if (Files.notExists(path) || Files.isDirectory(path)) {
			return null;
		}

		JAXBContext context = JAXBContext.newInstance(rootClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		if (null != schemaPath && Files.exists(schemaPath)) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try (InputStream inputStream = Files.newInputStream(schemaPath, StandardOpenOption.READ)) {
				Schema schema = sf.newSchema(new StreamSource(inputStream));
				unmarshaller.setSchema(schema);
			}
		}

		try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
			return (T) unmarshaller.unmarshal(inputStream);
		}
	}

}
