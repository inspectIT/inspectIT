package rocks.inspectit.agent.java.hooking;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;

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
	 * {@link #secondAfterBody(ICoreService, int, String, Object, Object[], Object, boolean, RegisteredSensorConfig)}
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
	 *            The return value of the method or exception thrown by method.
	 * @param exception
	 *            If method exited as result of exception. If <code>true</code> then the returnValue
	 *            parameter will be the exception and not the return value of the method execution
	 *            as such does not exist.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc);

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
	 *            The return value of the method or exception thrown by method.
	 * @param exception
	 *            If method exited as result of exception. If <code>true</code> then the returnValue
	 *            parameter will be the exception and not the return value of the method execution
	 *            as such does not exist.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc); // NOCHK:8-params
}
