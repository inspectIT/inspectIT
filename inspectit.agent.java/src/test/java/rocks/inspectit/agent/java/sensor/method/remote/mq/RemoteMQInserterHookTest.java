package rocks.inspectit.agent.java.sensor.method.remote.mq;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.mq.RemoteMQInserterHook;
import rocks.inspectit.agent.java.sensor.method.remote.mq.mock.JmsDestinationMock;
import rocks.inspectit.agent.java.sensor.method.remote.mq.mock.JmsMessageMock;
import rocks.inspectit.shared.all.communication.data.RemoteMQCallData;

/**
 * @author Thomas Kluge
 *
 */
public class RemoteMQInserterHookTest extends AbstractLogSupport {

	@Mock
	private IPlatformManager platformManager;

	@Mock
	private RemoteIdentificationManager remoteIdentificationManager;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private ICoreService coreService;

	@Mock
	private JmsMessageMock jmsMessageMock;

	@Mock
	private JmsDestinationMock jmsDestinationMock;

	RemoteMQInserterHook remoteMQInserterHook;

	@BeforeMethod
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sessioncapture", "false");
		remoteMQInserterHook = new RemoteMQInserterHook(platformManager, remoteIdentificationManager);
	}

	@Test
	public void oneRecordThatIsMQMessage() throws IdNotAvailableException {

		Long identification = 250l;
		String jmsDestinationQueue = "MyQueue";
		String jmsMessageID = "98853241";
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;

		RemoteMQCallData sample = Mockito.mock(RemoteMQCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);
		when(sample.getMessageDestination()).thenReturn(jmsDestinationQueue);
		when(sample.getMessageId()).thenReturn(jmsMessageID);

		ArgumentCaptor<RemoteMQCallData> captor = ArgumentCaptor.forClass(RemoteMQCallData.class);

		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);

		when(jmsMessageMock.getJMSMessageID()).thenReturn(jmsMessageID);
		when(jmsMessageMock.getJMSDestination()).thenReturn(jmsDestinationMock);
		when(jmsMessageMock.getObjectProperty(RemoteConstants.INSPECTIT_HTTP_HEADER)).thenReturn(null);

		when(jmsDestinationMock.getQueueName()).thenReturn(jmsDestinationQueue);

		Object[] parameters = new Object[] { jmsMessageMock };

		remoteMQInserterHook.beforeBody(methodId, sensorTypeId, null, parameters, registeredSensorConfig);

		remoteMQInserterHook.firstAfterBody(methodId, sensorTypeId, null, parameters, null, registeredSensorConfig);

		remoteMQInserterHook.secondAfterBody(coreService, methodId, sensorTypeId, null, parameters, null, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));
		assertThat(sample.getMessageDestination(), is(equalTo(captor.getValue().getMessageDestination())));
		assertThat(sample.getMessageId(), is(equalTo(captor.getValue().getMessageId())));
	}
}
