package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;

/**
 * Client adapter provider can deliver {@link AsyncClientRequestAdapter} based on the method
 * invocation object, parameters and the result.
 *
 * @author Ivan Senic
 *
 */
public interface AsyncClientAdapterProvider {

	/**
	 * Returns {@link AsyncClientRequestAdapter} to handle the request firing. Implementor should
	 * used passed object and parameters in order to create correct requestAdapter.
	 *
	 * @param object
	 *            Object on with method was invoked.
	 * @param parameters
	 *            Method invocation parameters.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 * @return {@link AsyncClientRequestAdapter}.
	 */
	AsyncClientRequestAdapter<?> getAsyncClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc);

}
