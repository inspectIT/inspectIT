package rocks.inspectit.server.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

/**
 * Tests the {@link TemplateManager} service.
 *
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class TemplateManagerTest extends TestBase {
	/**
	 * What folder to use for testing.
	 */
	private static final String TEST_FOLDER = "testTemplateDir";

	String key1 = "{adj}";
	String key2 = "{date}";
	String value1 = "great";
	String value2 = "01.01.2010";
	String templateStart = "This is my ";
	String templateEnd = "by me";
	String template = templateStart + key1 + " template created on " + key2 + templateEnd;

	@BeforeMethod
	public void init() throws IOException {
		Path testTemplatePath = Paths.get(TEST_FOLDER, ItestTemplateType.TEST_TEMPLATE.getFileName());
		Files.createDirectories(Paths.get(TEST_FOLDER));
		Files.createFile(testTemplatePath.toAbsolutePath());
		com.google.common.io.Files.write(template, testTemplatePath.toFile(), Charsets.UTF_8);
	}

	/**
	 * Tests the {@link TemplateManager#resolveTemplate(ITemplateType, java.util.Map)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class ResolveTemplate extends TemplateManagerTest {

		@Test(priority = 0)
		public void resolveSuccessful() throws IOException {
			TemplateManager templateManager = new TemplateManager();
			templateManager.templatesDir = Paths.get(TEST_FOLDER).toAbsolutePath().toFile();

			Map<String, String> replacements = new HashMap<>();
			replacements.put(key1, value1);
			replacements.put(key2, value2);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.TEST_TEMPLATE, replacements);

			assertThat(resolved, startsWith(templateStart));
			assertThat(resolved, containsString(value1));
			assertThat(resolved, containsString(value2));
			assertThat(resolved, endsWith(templateEnd));
		}

		@Test(priority = 1)
		public void resolveWithMissingReplacement() throws IOException {
			TemplateManager templateManager = new TemplateManager();
			templateManager.templatesDir = Paths.get(TEST_FOLDER).toAbsolutePath().toFile();

			Map<String, String> replacements = new HashMap<>();
			replacements.put(key1, value1);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.TEST_TEMPLATE, replacements);

			assertThat(resolved, startsWith(templateStart));
			assertThat(resolved, containsString(value1));
			assertThat(resolved, not(containsString(value2)));
			assertThat(resolved, endsWith(templateEnd));
		}

		@Test(priority = 2, expectedExceptions = { FileNotFoundException.class })
		public void resolveWithWrongTemplate() throws IOException {
			TemplateManager templateManager = new TemplateManager();
			templateManager.templatesDir = Paths.get(TEST_FOLDER).toAbsolutePath().toFile();

			Map<String, String> replacements = new HashMap<>();
			replacements.put(key1, value1);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.INVALID_TEMPLATE, replacements);

			assertThat(resolved, startsWith(templateStart));
			assertThat(resolved, containsString(value1));
			assertThat(resolved, not(containsString(value2)));
			assertThat(resolved, endsWith(templateEnd));
		}
	}

	/**
	 * Clean test folder after each test.
	 */
	@AfterMethod
	public void cleanUp() throws IOException {
		if (Files.exists(Paths.get(TEST_FOLDER))) {
			Files.walkFileTree(Paths.get(TEST_FOLDER), new DeleteFileVisitor());
			Files.deleteIfExists(Paths.get(TEST_FOLDER));
		}
	}

	public enum ItestTemplateType implements ITemplateType {
		TEST_TEMPLATE, INVALID_TEMPLATE;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getFileName() {
			if (this.equals(ItestTemplateType.TEST_TEMPLATE)) {
				return "test-template.txt";
			} else {
				return "invalid.txt";
			}
		}
	}
}
