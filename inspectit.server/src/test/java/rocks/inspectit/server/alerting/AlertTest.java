package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Test the {@link Alert}.
 *
 * @author Marius Oehler
 *
 */
public class AlertTest extends TestBase {

	@Mock
	AlertingDefinition alertingDefinition;

	/**
	 * Test constructor.
	 *
	 * @author Marius Oehler
	 *
	 */
	public static class Constructor extends AlertTest {
		@Test
		public void testConstruction1() {
			Alert alert = new Alert(alertingDefinition, 1L);

			assertThat(alert.getId(), notNullValue());
			assertThat(alert.getAlertingDefinition(), is(alertingDefinition));
			assertThat(alert.getStartTimestamp(), is(1L));
		}

		@Test
		public void testConstruction2() {
			Alert alert = new Alert(alertingDefinition, 1L, 2L);

			assertThat(alert.getId(), notNullValue());
			assertThat(alert.getAlertingDefinition(), is(alertingDefinition));
			assertThat(alert.getStartTimestamp(), is(1L));
			assertThat(alert.getStopTimestamp(), is(2L));
		}
	}
}
