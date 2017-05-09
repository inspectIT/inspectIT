package rocks.inspectit.agent.java.sensor.method.remote.server.mq;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
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
			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.JMS));
			assertThat(adapter.getFormat(), is(Format.Builtin.TEXT_MAP));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destination() throws Exception {
			String destination = "destination";
			when(message.getJMSDestination()).thenReturn(jmsDestination);
			when(jmsDestination.toString()).thenReturn(destination);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(ExtraTags.JMS_MESSAGE_DESTINATION, destination));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destinationNull() throws Exception {
			when(message.getJMSDestination()).thenReturn(null);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void destinationException() throws Exception {
			when(message.getJMSDestination()).thenThrow(new Exception());

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void messageId() throws Exception {
			String id = "id";
			when(message.getJMSMessageID()).thenReturn(id);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(ExtraTags.JMS_MESSAGE_ID, id));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void messageIdException() throws Exception {
			when(message.getJMSMessageID()).thenThrow(new Exception());

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtraction() throws Exception {
			String key = "key";
			String value = "value";
			when(message.getStringProperty(key)).thenReturn(value);
			doReturn(Collections.enumeration(Collections.singleton(key))).when(message).getPropertyNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			Entry<String, String> next = adapter.getCarrier().iterator().next();
			assertThat(next.getKey(), is(key));
			assertThat(next.getValue(), is(value));
			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtractionEnumerationEmpty() throws Exception {
			doReturn(Collections.enumeration(Collections.emptyList())).when(message).getPropertyNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtractionEnumerationNull() throws Exception {
			doReturn(null).when(message).getPropertyNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void contextStore() {
			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { message }, rsc);

			SpanContextStore spanContextStore = adapter.getSpanContextStore();
			assertThat(spanContextStore, is(not(nullValue())));
			assertThat(spanContextStore, is(instanceOf(NoopSpanContextStore.class)));
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
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, result, false, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, result, rsc);
		}

		@Test
		public void exception() {
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, new NullPointerException(), true, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(2));
			assertThat(tags, hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat(tags, hasEntry(ExtraTags.THROWABLE_TYPE, NullPointerException.class.getSimpleName()));
			verifyZeroInteractions(object, rsc);
		}

	}


	interface Message {
		Destination getJMSDestination() throws Exception;
		String getJMSMessageID() throws Exception;
		String getStringProperty(String key);
		Enumeration<?> getPropertyNames();
	};

	interface Destination {
	};

}