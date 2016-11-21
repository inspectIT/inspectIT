package rocks.inspectit.server.influx.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.influxdb.InfluxDB;
import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.util.InfluxClientFactory;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link InfluxClientFactory} class.
 *
 * @author Marius Oehler
 *
 */
public class InfluxClientFactoryTest extends TestBase {

	@InjectMocks
	InfluxClientFactory clientFactory;

	/**
	 * Tests the {@link InfluxClientFactory#createClient()} method.
	 */
	public static class CreateClient extends InfluxClientFactoryTest {

		@Test
		public void validData() {
			clientFactory.host = "localhost";
			clientFactory.port = 1;
			clientFactory.user = "user";

			InfluxDB client = clientFactory.createClient();

			assertThat(client, not(nullValue()));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void hostEmpty() {
			clientFactory.host = "";
			clientFactory.port = 1;
			clientFactory.user = "user";

			clientFactory.createClient();
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void hostNull() {
			clientFactory.port = 1;
			clientFactory.user = "user";

			clientFactory.createClient();
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void portInvalid() {
			clientFactory.host = "localhost";
			clientFactory.port = 0;
			clientFactory.user = "user";

			clientFactory.createClient();
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void userEmpty() {
			clientFactory.host = "localhost";
			clientFactory.port = 1;
			clientFactory.user = "";

			clientFactory.createClient();
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void userNull() {
			clientFactory.host = "localhost";
			clientFactory.port = 1;

			clientFactory.createClient();
		}
	}
}
