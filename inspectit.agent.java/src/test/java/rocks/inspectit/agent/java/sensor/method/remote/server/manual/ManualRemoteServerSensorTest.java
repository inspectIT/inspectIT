package rocks.inspectit.agent.java.sensor.method.remote.server.manual;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class ManualRemoteServerSensorTest extends TestBase {

	@InjectMocks
	ManualRemoteServerSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	Object object;

	public static class GetServerRequestAdapter extends ManualRemoteServerSensorTest {

		@Test
		public void empty() {
			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, null, rsc);

			assertThat(adapter.getPropagationType(), is(nullValue()));
			assertThat(adapter.getTags().size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtraction() {
			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, null, rsc);

			assertThat(adapter.getBaggageExtractAdapter(), is(not(nullValue())));
			assertThat(adapter.getBaggageExtractAdapter().getBaggageItem("anykey"), is(nullValue()));
			verifyZeroInteractions(object, rsc);
		}

	}

	public static class GetServerResponseAdapter extends ManualRemoteServerSensorTest {

		@Mock
		Object result;

		@Test
		public void empty() {
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, result, rsc);

			assertThat(adapter.getTags().size(), is(0));
			verifyZeroInteractions(object, result, rsc);
		}

	}
}
