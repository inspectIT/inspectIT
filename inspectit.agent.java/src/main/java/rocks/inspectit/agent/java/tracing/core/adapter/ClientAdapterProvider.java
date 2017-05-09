package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;

/**
 * Client adapter provider can deliver {@link ClientRequestAdapter} and {@link ResponseAdapter}
 * based on the method invocation object, parameters and the result.
 *
 * @author Ivan Senic
 *
 */
public interface ClientAdapterProvider {

	/**
	 * Returns {@link ClientRequestAdapter} to handle the request. Implementor should used passed
	 * object and parameters in order to create correct requestAdapter.
	 *
	 * @param object
	 *            Object on with method was invoked.
	 * @param parameters
	 *            Method invocation parameters.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 * @return {@link ClientRequestAdapter}.
	 */
	ClientRequestAdapter<?> getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc);

	/**
	 * Returns {@link ResponseAdapter} to handle the request. Implementor should used passed object
	 * and parameters in order to create correct requestAdapter.
	 *
	 * @param object
	 *            Object on with method was invoked.
	 * @param parameters
	 *            Method invocation parameters.
	 * @param result
	 *            The result of method invocation or exception thrown by method.
	 * @param exception
	 *            If method exited as result of exception. If <code>true</code> then the returnValue
	 *            parameter will be the exception and not the return value of the method execution
	 *            as such does not exist.
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 * @return {@link ClientRequestAdapter}.
	 */
	ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc);

}
