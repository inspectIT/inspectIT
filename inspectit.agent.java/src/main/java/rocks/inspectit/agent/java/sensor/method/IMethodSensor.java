package rocks.inspectit.agent.java.sensor.method;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.ISensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;

/**
 * Every method sensor installs a hook into the target class which can be retrieved later with the
 * {@link #getHook()} method.
 *
 * @author Patrice Bouillet
 *
 */
public interface IMethodSensor extends ISensor {

	/**
	 * Returns the proper method hook.
	 *
	 * @return The {@link IMethodHook} implementation of the corresponding sensor.
	 */
	IHook getHook();

	/**
	 * Returns the {@link MethodSensorTypeConfig} for this sensor.
	 *
	 * @return Returns the {@link MethodSensorTypeConfig} for this sensor.
	 */
	MethodSensorTypeConfig getSensorTypeConfig();

}
