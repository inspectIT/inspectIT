package rocks.inspectit.agent.java.sensor.method.remote.client.mq;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JmsRemoteClientSensorTest extends TestBase {

	@InjectMocks
	JmsRemoteClientSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	public static class GetClientRequestAdapter extends JmsRemoteClientSensorTest {

		@Mock
		Object object;

		@Mock
		Message message;

		@Mock
		Destination jmsDestination;

		@Test
		public void properties() {
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.JMS));
			assertThat(adapter.getReferenceType(), is(References.FOLLOWS_FROM));
			assertThat(adapter.getFormat(), is(Format.Builtin.TEXT_MAP));
			verifyZeroInteractions(object);
		}

		@Test
		public void destination() throws Exception {
			String destination = "destination";
			when(message.getJMSDestination()).thenReturn(jmsDestination);
			when(jmsDestination.toString()).thenReturn(destination);
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(ExtraTags.JMS_MESSAGE_DESTINATION, destination));
			verifyZeroInteractions(object);
		}

		@Test
		public void destinationNull() throws Exception {
			when(message.getJMSDestination()).thenReturn(null);
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object);
		}

		@Test
		public void destinationException() throws Exception {
			when(message.getJMSDestination()).thenThrow(new Exception());
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object);
		}

		@Test
		public void messageId() throws Exception {
			String id = "id";
			when(message.getJMSMessageID()).thenReturn(id);
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(ExtraTags.JMS_MESSAGE_ID, id));
			verifyZeroInteractions(object);
		}

		@Test
		public void messageIdException() throws Exception {
			when(message.getJMSMessageID()).thenThrow(new Exception());
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object);
		}

		@Test
		public void baggageInjection() throws Exception {
			String key = "key";
			String value = "value";
			when(rsc.getParameterTypes()).thenReturn(Collections.singletonList("javax.jms.Message"));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { message }, rsc);
			adapter.getCarrier().put(key, value);

			verify(message).setStringProperty(key, value);
			verifyZeroInteractions(object);
		}
	}

	public static class GetClientResponseAdapter extends JmsRemoteClientSensorTest {

		@Mock
		Object object;

		@Mock
		Object result;

		@Test
		public void empty() {
			ResponseAdapter adapter = sensor.getClientResponseAdapter(object, null, result, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, result, rsc);
		}

	}

	interface Message {
		Destination getJMSDestination()  throws Exception;
		String getJMSMessageID()  throws Exception;
		void setStringProperty(String key, String value)  throws Exception;
	};

	interface Destination {
	};

}