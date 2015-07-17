package info.novatec.inspectit.agent.hooking;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;

/**
 * Classes which add a hook into a method before and after it is called, have to implement this
 * interface.
 * 
 */
public interface IMethodHook extends IHook {

	/**
	 * This method is executed before something else in the original method body will be executed.
	 * 
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc);

	/**
	 * This method will be called before the original method will return. It is the first of two
	 * after body calls. It is important that a hook, implementing this method, just adds time or
	 * memory critical settings. Everything else, including computing or adding values to the value
	 * storage has to be added to the
	 * {@link #secondAfterBody(ICoreService, int, String, Object, Object[], Object, RegisteredSensorConfig)}
	 * implementation.
	 * 
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result
	 *            The return value
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc);

	/**
	 * This method will be called before the original method will return. It is the second of two
	 * after body calls. This method can be used to save or compute some values.
	 * 
	 * @param coreService
	 *            The reference to the core service which holds the data objects etc.
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result
	 *            The return value
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc);
}
