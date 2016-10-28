package rocks.inspectit.agent.java.sensor.method.remote.server.mq;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JmsListenerRemoteServerSensorTest extends TestBase {

	@InjectMocks
	JmsListenerRemoteServerSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	public static class GetServerRequestAdapter extends JmsListenerRemoteServerSensorTest {

		@Mock
		Object object;

		@Mock
		Message message;

		@Mock
		Destination jmsDestination;

		@Test
		public void properties() {
			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.JMS));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destination() throws Exception {
			String destination = "destination";
			when(message.getJMSDestination()).thenReturn(jmsDestination);
			when(jmsDestination.toString()).thenReturn(destination);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Jms.MESSAGE_DESTINATION, destination));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destinationNull() throws Exception {
			when(message.getJMSDestination()).thenReturn(null);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destinationException() throws Exception {
			when(message.getJMSDestination()).thenThrow(new Exception());

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void messageId() throws Exception {
			String id = "id";
			when(message.getJMSMessageID()).thenReturn(id);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Jms.MESSAGE_ID, id));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void messageIdException() throws Exception {
			when(message.getJMSMessageID()).thenThrow(new Exception());

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtraction() throws Exception {
			String key = "key";
			String value = "value";
			when(message.getStringProperty(key)).thenReturn(value);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getBaggageExtractAdapter().getBaggageItem(key), is(value));
			verifyZeroInteractions(object, rsc);
		}
	}

	public static class GetServerResponseAdapter extends JmsListenerRemoteServerSensorTest {

		@Mock
		Object object;

		@Mock
		Object result;

		@Test
		public void empty() {
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, result, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, result, rsc);
		}

	}


	interface Message {
		Destination getJMSDestination() throws Exception;

		String getJMSMessageID() throws Exception;
		String getStringProperty(String key);
	};

	interface Destination {
	};

}