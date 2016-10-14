package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Test the {@link Alert}.
 * 
 * @author Marius Oehler
 *
 */
public class AlertTest extends TestBase {

	private AlertingDefinition alertingDefinition;

	@BeforeClass
	public void init() {
		alertingDefinition = new AlertingDefinition();
	}

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

	@Test
	public void testSetter() {
		Alert alert = new Alert(null, 0);

		alert.setAlertingDefinition(alertingDefinition);
		alert.setStartTimestamp(1L);
		alert.setStopTimestamp(2L);

		assertThat(alert.getId(), notNullValue());
		assertThat(alert.getAlertingDefinition(), is(alertingDefinition));
		assertThat(alert.getStartTimestamp(), is(1L));
		assertThat(alert.getStopTimestamp(), is(2L));
	}

}
