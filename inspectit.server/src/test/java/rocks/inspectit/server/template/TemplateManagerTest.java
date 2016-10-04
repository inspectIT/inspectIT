package rocks.inspectit.server.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

	private static final String KEY_1 = "{adj}";
	private static final String KEY_2 = "{date}";
	private static final String VALUE_1 = "great";
	private static final String VALUE_2 = "01.01.2010";
	private static final String TEMPLATE_START = "This is my ";
	private static final String TEMPLATE_END = "by me";
	private static final String TEMPLATE = TEMPLATE_START + KEY_1 + " template created on " + KEY_2 + TEMPLATE_END;

	@BeforeMethod
	public void init() throws IOException {
		Path testTemplatePath = Paths.get(TEST_FOLDER, ItestTemplateType.TEST_TEMPLATE.getFileName());
		Files.createDirectories(Paths.get(TEST_FOLDER));
		Files.createFile(testTemplatePath.toAbsolutePath());
		Files.write(testTemplatePath, TEMPLATE.getBytes());
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
			replacements.put(KEY_1, VALUE_1);
			replacements.put(KEY_2, VALUE_2);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.TEST_TEMPLATE, replacements);

			assertThat(resolved, startsWith(TEMPLATE_START));
			assertThat(resolved, containsString(VALUE_1));
			assertThat(resolved, containsString(VALUE_2));
			assertThat(resolved, endsWith(TEMPLATE_END));
		}

		@Test(priority = 1)
		public void resolveWithMissingReplacement() throws IOException {
			TemplateManager templateManager = new TemplateManager();
			templateManager.templatesDir = Paths.get(TEST_FOLDER).toAbsolutePath().toFile();

			Map<String, String> replacements = new HashMap<>();
			replacements.put(KEY_1, VALUE_1);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.TEST_TEMPLATE, replacements);

			assertThat(resolved, startsWith(TEMPLATE_START));
			assertThat(resolved, containsString(VALUE_1));
			assertThat(resolved, not(containsString(VALUE_2)));
			assertThat(resolved, endsWith(TEMPLATE_END));
		}

		@Test(priority = 2, expectedExceptions = { NoSuchFileException.class })
		public void resolveWithWrongTemplate() throws IOException {
			TemplateManager templateManager = new TemplateManager();
			templateManager.templatesDir = Paths.get(TEST_FOLDER).toAbsolutePath().toFile();

			Map<String, String> replacements = new HashMap<>();
			replacements.put(KEY_1, VALUE_1);

			String resolved = templateManager.resolveTemplate(ItestTemplateType.INVALID_TEMPLATE, replacements);

			assertThat(resolved, startsWith(TEMPLATE_START));
			assertThat(resolved, containsString(VALUE_1));
			assertThat(resolved, not(containsString(VALUE_2)));
			assertThat(resolved, endsWith(TEMPLATE_END));
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
