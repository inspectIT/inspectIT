package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;

/**
 * Classes which add additional instructions to the constructor of type {@link Throwable}, to the
 * <code>throw</code> statement and to the <code>catch</code> block catching type {@link Throwable}
 * have to implement this interface.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IExceptionSensorHook extends IConstructorHook {

	/**
	 * This method is executed when an object of type {@link Throwable} is thrown in a method body
	 * with the <code>throw</code> statement. This method is wrapping the thrower method with a
	 * try-catch block and gets the needed information. After that the {@link Throwable} object is
	 * thrown from that method to propagate along the normal exceptional path.
	 * 
	 * @param coreService
	 *            The core service.
	 * @param id
	 *            The method id where the {@link Throwable} object was thrown.
	 * @param sensorTypeId
	 *            The sensor type id of the {@link IExceptionSensor}.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param exceptionObject
	 *            The exception type that occured in the method body.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} containing all information about the method
	 *            where the {@link Throwable} object was thrown.
	 */
	void dispatchOnThrowInBody(ICoreService coreService, long id, long sensorTypeId, Object object, Object exceptionObject, Object[] parameters, RegisteredSensorConfig rsc);

	/**
	 * This method is executed just before a handler (appropriate catch block) for the thrown
	 * {@link Throwable} object is executed.
	 * 
	 * @param coreService
	 *            The core service.
	 * @param id
	 *            The method id where the {@link Throwable} object was handled.
	 * @param sensorTypeId
	 *            The sensor type id of the {@link IExceptionSensor}.
	 * @param exceptionObject
	 *            The {@link Throwable} object itself.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} containing all information about the method
	 *            where the {@link Throwable} object was thrown.
	 */
	void dispatchBeforeCatchBody(ICoreService coreService, long id, long sensorTypeId, Object exceptionObject, RegisteredSensorConfig rsc);

}
