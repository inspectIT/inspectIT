package info.novatec.inspectit.agent.hooking;

import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;

/**
 * Hook supplier where all components can get hooks and sensor configs by id.
 *
 * @author Ivan Senic
 *
 */
public interface IHookSupplier {

	/**
	 * Returns the {@link MethodSensorTypeConfig} for the given ID if one exists.
	 *
	 * @param id
	 *            Id of the {@link MethodSensorTypeConfig}.
	 * @return {@link MethodSensorTypeConfig} for the given ID if one exists or null otherwise
	 */
	MethodSensorTypeConfig getMethodSensorTypeConfig(long id);

	/**
	 * Returns the method hook for the sensor id.
	 *
	 * @param id
	 *            Id of the {@link MethodSensorTypeConfig} (sensor id).
	 * @return {@link IMethodHook} or {@link IConstructorHook} if it exists.
	 */
	IHook getMethodHook(long id);

	/**
	 * Gets the invocation sensor type config.
	 *
	 * @return Gets the invocation sensor type config.
	 */
	MethodSensorTypeConfig getInvocationSequenceSensorTypeConfig();

	/**
	 * Gets the exception sensor type config.
	 *
	 * @return Gets the exception sensor type config.
	 */
	MethodSensorTypeConfig getExceptionSensorTypeConfig();

}