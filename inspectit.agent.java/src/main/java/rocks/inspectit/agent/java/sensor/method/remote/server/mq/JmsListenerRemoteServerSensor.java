package rocks.inspectit.agent.java.sensor.method.remote.server.mq;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.server.RemoteServerSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.MQRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.data.impl.JmsMessage;

/**
 * Remote server sensor for intercepting JMS message receiving. This sensor is intended to work with
 * JMS listener, but in theory can be placed on any method where JMS message is first parameter.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code javax.jms.MessageListener#onMessage(javax.jms.Message)}
 * <li>{@code javax.jms.MessageListener#onMessage(javax.jms.Message, javax.jms.Session)}
 * </ul>
 *
 * @author Ivan Senic
 */
public class JmsListenerRemoteServerSensor extends RemoteServerSensor implements ServerAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ServerAdapterProvider getServerAdapterProvider() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerRequestAdapter<TextMap> getServerRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// message is first parameter
		Object message = parameters[0];
		JmsMessage jmsMessage = new JmsMessage(message, CACHE);
		return new MQRequestAdapter(jmsMessage, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getServerResponseAdapter(Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		if (exception) {
			return new ThrowableAwareResponseAdapter(result.getClass().getSimpleName());
		} else {
			return EmptyResponseAdapter.INSTANCE;
		}
	}

}
