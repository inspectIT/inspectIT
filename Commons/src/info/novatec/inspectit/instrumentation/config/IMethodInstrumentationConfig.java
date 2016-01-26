package info.novatec.inspectit.instrumentation.config;

import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

import java.util.Collection;
import java.util.List;

/**
 * Instrumentation configuration for one method.
 *
 * @author Ivan Senic
 *
 */
public interface IMethodInstrumentationConfig {

	/**
	 * If {@link MethodInstrumentationConfig} has any {@link IMethodInstrumentationPoint}s.
	 *
	 * @return If {@link MethodInstrumentationConfig} has any {@link IMethodInstrumentationPoint}s.
	 */
	boolean hasInstrumentationPoints();

	/**
	 * Returns all {@link IMethodInstrumentationPoint}s that exists for this method.
	 *
	 * @return Returns all {@link IMethodInstrumentationPoint}s that exists for this method.
	 */
	Collection<IMethodInstrumentationPoint> getAllInstrumentationPoints();

	/**
	 * Returns target class FQN of a method.
	 *
	 * @return Returns target class FQN of a method.
	 */
	String getTargetClassFqn();

	/**
	 * Returns method name.
	 *
	 * @return Returns method name.
	 */
	String getTargetMethodName();

	/**
	 * Returns method return type.
	 *
	 * @return Returns method return type.
	 */
	String getReturnType();

	/**
	 * Returns method parameter types.
	 *
	 * @return Returns method parameter types.
	 */
	List<String> getParameterTypes();

}
