package rocks.inspectit.agent.java.sensor.method.remote.client.mq;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.MQRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.data.impl.JmsMessage;

/**
 * Remote client sensor for intercepting JMS message sending.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code javax.jms.MessageProducer#send(javax.jms.Message)}
 * <li>{@code javax.jms.MessageProducer#send(javax.jms.Message int int long)}
 * <li>{@code javax.jms.MessageProducer#send(javax.jms.Queue javax.jms.Message)}
 * <li>{@code javax.jms.MessageProducer#send(javax.jms.Queue javax.jms.Message int int long)}
 * </ul>
 * <p>
 * Please note that this sensor would work with any method has the javax.jms.Message as one of the
 * parameters, as we are figuring out the parameter index by inspecting the
 * {@link RegisteredSensorConfig}.
 *
 * @author Ivan Senic
 */
public class JmsRemoteClientSensor extends RemoteClientSensor implements ClientAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientAdapterProvider getClientAdapterProvider() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientRequestAdapter<TextMap> getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object message = getMessage(parameters, rsc);
		JmsMessage jmsMessage = new JmsMessage(message, CACHE);
		return new MQRequestAdapter(jmsMessage, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		if (exception) {
			return new ThrowableAwareResponseAdapter(result.getClass().getSimpleName());
		} else {
			return EmptyResponseAdapter.INSTANCE;
		}
	}

	/**
	 * Gets the message object from the parameters. This method will consult the
	 * {@link RegisteredSensorConfig} in order to find parameter index with the FQN of
	 * {@value JmsMessage#JAVAX_JMS_MESSAGE}.
	 *
	 * @param parameters
	 *            Parameters of method invocation.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}
	 * @return Message object or <code>null</code> if one can not be located.
	 */
	private Object getMessage(Object[] parameters, RegisteredSensorConfig rsc) {
		int index = rsc.getParameterTypes().indexOf(JmsMessage.JAVAX_JMS_MESSAGE);
		if (index >= 0) {
			return parameters[index];
		}
		return null;
	}

}
