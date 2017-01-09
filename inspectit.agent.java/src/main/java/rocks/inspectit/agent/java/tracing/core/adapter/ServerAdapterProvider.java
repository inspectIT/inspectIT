package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;

/**
 * Server adapter provider can deliver {@link ServerRequestAdapter} and {@link ResponseAdapter}
 * based on the method invocation object, parameters and the result.
 *
 * @author Ivan Senic
 *
 */
public interface ServerAdapterProvider {

	/**
	 * Returns {@link ServerRequestAdapter} to handle the request when the one is available on the
	 * start of the instrumented method. Implementor should used passed object and parameters in
	 * order to create correct requestAdapter.
	 *
	 * @param object
	 *            Object on with method was invoked.
	 * @param parameters
	 *            Method invocation parameters.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 * @return {@link ServerRequestAdapter}.
	 */
	ServerRequestAdapter<?> getServerRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc);

	/**
	 * Returns {@link ResponseAdapter} to handle the request. Implementor should used passed object
	 * and parameters in order to create correct requestAdapter.
	 *
	 * @param object
	 *            Object on with method was invoked.
	 * @param parameters
	 *            Method invocation parameters.
	 * @param result
	 *            Result of the method invocation.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 * @return {@link ClientRequestAdapter}.
	 */
	ResponseAdapter getServerResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc);

}
