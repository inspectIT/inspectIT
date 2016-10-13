package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Map;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * Class loading delegation sensor.
 *
 * @author Ivan Senic
 *
 */
public class ClassLoadingDelegationSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * Hook to use.
	 */
	ISpecialHook hook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHook getHook() {
		return hook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		hook = new ClassLoadingDelegationHook();
	}

}
