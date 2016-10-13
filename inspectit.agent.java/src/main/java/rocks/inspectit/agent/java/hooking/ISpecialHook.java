package rocks.inspectit.agent.java.hooking;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;

/**
 * Hook that can be implemented by the special sensors, thus the ones that are not collecting
 * monitoring data but rather deal with special instrumentation needed for the correct work of
 * inspectIT.
 *
 * @author Ivan Senic
 *
 */
public interface ISpecialHook extends IHook {

	/**
	 * This method is executed before something else in the original method body will be executed.
	 * <p>
	 * If this method returns non-null value the original method will not be executed and returned
	 * result will be result of the original method. Otherwise the execution of the original method
	 * will be normal.
	 *
	 * @param methodId
	 *            The unique method id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param ssc
	 *            The {@link SpecialSensorConfig} object which holds all the information of the
	 *            executed method.
	 * @return Result to use for the original method or <code>null</code> to continue normal
	 *         execution of the original method.
	 */
	Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc);

	/**
	 * This method will be called before the original method will return.
	 * <p>
	 * If this method returns non-null value the returned result will be result of the original
	 * method.
	 * <p>
	 * Only called if the original method is not throwing an exception.
	 *
	 * @param methodId
	 *            The unique method id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result
	 *            The return value
	 * @param ssc
	 *            The {@link SpecialSensorConfig} object which holds all the information of the
	 *            executed method.
	 * @return Result to use for the original method or <code>null</code> to use original result of
	 *         the method.
	 */
	Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc);

}
