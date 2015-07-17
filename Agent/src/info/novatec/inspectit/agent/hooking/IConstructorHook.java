package info.novatec.inspectit.agent.hooking;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;

/**
 * Classes which add a hook into a constructor have to implement this interface.
 * 
 */
public interface IConstructorHook extends IHook {

	/**
	 * The bytecode is inserted before a constructor in the super class or this class is called.
	 * Therefore, the inserted bytecode is subject to constraints described in Section 4.8.2 of The
	 * Java Virtual Machine Specification (2nd ed). For example, it cannot access instance fields or
	 * methods although it may assign a value to an instance field directly declared in this class.
	 * Accessing static fields and methods is allowed.
	 * 
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param parameters
	 *            The array of parameters.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc);

	/**
	 * The bytecode is inserted after the constructor calls.
	 * 
	 * @param coreService
	 *            The core service.
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The array of parameters.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 */
	void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc);

}
