package rocks.inspectit.agent.java.sensor.method.remote.server.manual;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.server.RemoteServerSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyResponseAdapter;

/**
 * Remote server sensor that users can manually place on any method. Not depending on any technology
 * and can not receive any tracing data.
 *
 * @author Ivan Senic
 *
 */
public class ManualRemoteServerSensor extends RemoteServerSensor implements ServerAdapterProvider {

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
	public ServerRequestAdapter<?> getServerRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		return EmptyRequestAdapter.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getServerResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		return EmptyResponseAdapter.INSTANCE;
	}

}
