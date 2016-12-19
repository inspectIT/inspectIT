package rocks.inspectit.shared.cs.jaxb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mockito.InjectMocks;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class JAXBTransformatorTest extends TestBase {

	@InjectMocks
	JAXBTransformator transformator;

	Path path = Paths.get("tst.xml").toAbsolutePath();

	@AfterTest
	public void clean() throws IOException {
		Files.deleteIfExists(path);
	}

	public static class Marshall extends JAXBTransformatorTest {

		@Test
		public void basic() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);

			transformator.marshall(path, testData, null);

			assertThat(Files.exists(path), is(true));
			JAXBContext context = JAXBContext.newInstance(TestData.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
				Object unmarshaled = unmarshaller.unmarshal(is);
				assertThat(unmarshaled, is((Object) testData));
			}
		}

		@Test
		public void schemaVersion() throws Exception {
			String value = "value";
			int schemaVersion = 11;
			TestData testData = new TestData();
			testData.setValue(value);

			transformator.marshall(path, testData, null, schemaVersion);

			assertThat(testData.getSchemaVersion(), is(schemaVersion));
			JAXBContext context = JAXBContext.newInstance(TestData.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
				Object unmarshaled = unmarshaller.unmarshal(is);
				assertThat(unmarshaled, is((Object) testData));
			}
		}

		@Test
		public void schemaLocation() throws Exception {
			String schemaLocation = "something.xsd";

			transformator.marshall(path, new TestData(), schemaLocation);

			try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(is);

				Element element = document.getDocumentElement();
				element.normalize();

				assertThat(element.hasAttribute("xsi:noNamespaceSchemaLocation"), is(true));
				assertThat(element.getAttribute("xsi:noNamespaceSchemaLocation"), is(schemaLocation));
			}

		}

		@Test(expectedExceptions = IOException.class)
		public void directoryAsPath() throws Exception {
			transformator.marshall(path.getParent(), new TestData(), null);
		}

		@Test
		public void bytesBasic() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);

			byte[] data = transformator.marshall(testData, null);

			assertThat(Files.exists(path), is(true));
			JAXBContext context = JAXBContext.newInstance(TestData.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			try (InputStream is = new ByteArrayInputStream(data)) {
				Object unmarshaled = unmarshaller.unmarshal(is);
				assertThat(unmarshaled, is((Object) testData));
			}
		}

		@Test
		public void bytesSchemaVersion() throws Exception {
			String value = "value";
			int schemaVersion = 11;
			TestData testData = new TestData();
			testData.setValue(value);

			byte[] data = transformator.marshall(testData, null, schemaVersion);

			assertThat(testData.getSchemaVersion(), is(schemaVersion));
			JAXBContext context = JAXBContext.newInstance(TestData.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			try (InputStream is = new ByteArrayInputStream(data)) {
				Object unmarshaled = unmarshaller.unmarshal(is);
				assertThat(unmarshaled, is((Object) testData));
			}
		}

		@Test
		public void bytesSchemaLocation() throws Exception {
			String schemaLocation = "something.xsd";

			byte[] data = transformator.marshall(new TestData(), schemaLocation);

			try (InputStream is = new ByteArrayInputStream(data)) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(is);

				Element element = document.getDocumentElement();
				element.normalize();

				assertThat(element.hasAttribute("xsi:noNamespaceSchemaLocation"), is(true));
				assertThat(element.getAttribute("xsi:noNamespaceSchemaLocation"), is(schemaLocation));
			}

		}

	}

	public static class Unmarshal extends JAXBTransformatorTest {

		private Path schemaPath = Paths.get("src", "test", "resources", "rocks", "inspectit", "shared", "cs", "jaxb", "schema.xsd").toAbsolutePath();

		private Path invalidSchemaPath = Paths.get("src", "test", "resources", "rocks", "inspectit", "shared", "cs", "jaxb", "schemaInvalid.xsd").toAbsolutePath();

		private Path migrationPath = schemaPath.getParent().resolve("migration");


		@Test
		public void basic() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			transformator.marshall(path, testData, null);

			TestData unmarshalled = transformator.unmarshall(path, schemaPath, TestData.class);

			assertThat(unmarshalled, is(testData));
		}

		@Test(expectedExceptions = UnmarshalException.class)
		public void schemaNotValid() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			transformator.marshall(path, testData, null);

			transformator.unmarshall(path, invalidSchemaPath, TestData.class);
		}

		@Test
		public void schemaVersionUpToDate() throws Exception {
			int schemaVersion = 10;
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			transformator.marshall(path, testData, null, schemaVersion);

			TestData unmarshalled = transformator.unmarshall(path, schemaPath, schemaVersion, migrationPath, TestData.class);

			assertThat(unmarshalled, is(testData));
		}

		@Test
		public void schemaVersionNotUpToDate() throws Exception {
			int schemaVersion = 0;
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			transformator.marshall(path, testData, null, schemaVersion);
			// manually remove the value attribute
			List<String> update = new ArrayList<>();
			List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
			for (String line : lines) {
				if (line.contains("value=")) {
					line = line.replace("value=\"value\"", "");
				}
				update.add(line);
			}
			Files.write(path, update, Charset.defaultCharset());

			TestData unmarshalled = transformator.unmarshall(path, schemaPath, schemaVersion + 1, migrationPath, TestData.class);

			assertThat(unmarshalled.getSchemaVersion(), is(1));
			assertThat(unmarshalled.getValue(), is("bar"));
		}

		@Test
		public void directoryAsPath() throws Exception {
			TestData unmarshalled = transformator.unmarshall(path.getParent(), schemaPath, TestData.class);

			assertThat(unmarshalled, is(nullValue()));
		}

		@Test
		public void notExistingPath() throws Exception {
			TestData unmarshalled = transformator.unmarshall(Paths.get("something.xml").toAbsolutePath(), schemaPath, TestData.class);

			assertThat(unmarshalled, is(nullValue()));
		}

		@Test
		public void bytesBasic() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			byte[] data = transformator.marshall(testData, null);

			TestData unmarshalled = transformator.unmarshall(data, schemaPath, TestData.class);

			assertThat(unmarshalled, is(testData));
		}

		@Test(expectedExceptions = UnmarshalException.class)
		public void bytesSchemaNotValid() throws Exception {
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			byte[] data = transformator.marshall(testData, null);

			transformator.unmarshall(data, invalidSchemaPath, TestData.class);
		}

		@Test
		public void bytesSchemaVersionUpToDate() throws Exception {
			int schemaVersion = 10;
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			byte[] data = transformator.marshall(testData, null, schemaVersion);

			TestData unmarshalled = transformator.unmarshall(data, schemaPath, schemaVersion, migrationPath, TestData.class);

			assertThat(unmarshalled, is(testData));
		}

		@Test
		public void bytesSchemaVersionNotUpToDate() throws Exception {
			int schemaVersion = 0;
			String value = "value";
			TestData testData = new TestData();
			testData.setValue(value);
			byte[] data = transformator.marshall(testData, null, schemaVersion);
			String dataAsString = new String(data);
			// manually remove the value attribute
			dataAsString = dataAsString.replace("value=\"value\"", "");
			data = dataAsString.getBytes();

			TestData unmarshalled = transformator.unmarshall(data, schemaPath, schemaVersion + 1, migrationPath, TestData.class);

			assertThat(unmarshalled.getSchemaVersion(), is(1));
			assertThat(unmarshalled.getValue(), is("bar"));
		}
	}
}
