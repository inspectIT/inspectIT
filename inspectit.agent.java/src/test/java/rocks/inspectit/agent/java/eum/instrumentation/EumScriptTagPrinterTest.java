package rocks.inspectit.agent.java.eum.instrumentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Pattern;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class EumScriptTagPrinterTest extends TestBase {

	@InjectMocks
	EumScriptTagPrinter printer;

	public static class PrintTags extends EumScriptTagPrinterTest {

		@Test
		public void test() {
			printer.setScriptSourceURL("my_script_url with space");
			printer.setSetting("opt_a", "value_a");
			printer.setSetting("opt_b", "\"value b\"");

			String printedTags = printer.printTags();

			assertThat("Url not correctly set in script tag",
					Pattern.compile("<script[^>]*src(\\s*)=(\\s*)\"my_script_url with space\"").matcher(printedTags).find());
			assertThat("opt_a not correctly set as option",
					Pattern.compile("window\\.inspectIT_settings\\s*=\\s*\\{\\s*(.+\\s*,\\s*)?opt_a\\s*:\\s*value_a\\s*(,|\\})").matcher(printedTags).find());
			assertThat("opt_b not correctly set as option",
					Pattern.compile("window\\.inspectIT_settings\\s*=\\s*\\{\\s*(.+\\s*,\\s*)?opt_b\\s*:\\s*\"value b\"\\s*(,|\\})").matcher(printedTags).find());
		}
	}

	public static class Clone extends EumScriptTagPrinterTest {

		@Test
		public void testDataValid() {
			printer.setSetting("opta", "valuea");

			EumScriptTagPrinter copy = printer.clone();

			assertThat(copy.getSettings(), equalTo(printer.getSettings()));
			assertThat(copy.getScriptSourceURL(), equalTo(printer.getScriptSourceURL()));
		}

		@Test
		public void testModification() {
			printer.setSetting("opta", "valuea");

			EumScriptTagPrinter copy = printer.clone();
			copy.setSetting("opta", "notvaluea");

			assertThat(printer.getSettings().get("opta"), equalTo("valuea"));
		}
	}

}
