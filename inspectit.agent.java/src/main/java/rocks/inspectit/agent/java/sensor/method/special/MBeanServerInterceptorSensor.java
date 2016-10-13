package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.sensor.jmx.IMBeanServerListener;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;

/**
 * Sensor that intercepts adding and removing of the MBean servers and delegate this information to
 * the available {@link IMBeanServerListener}s.
 *
 * @author Ivan Senic
 *
 */
public class MBeanServerInterceptorSensor extends AbstractMethodSensor {

	/**
	 * All the listeners to delegate the information to.
	 */
	@Autowired(required = false)
	private Collection<IMBeanServerListener> mBeanServerListeners;

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
		if (CollectionUtils.isNotEmpty(mBeanServerListeners)) {
			hook = new MBeanServerInterceptorHook(mBeanServerListeners);
		} else {
			hook = new MBeanServerInterceptorHook(Collections.<IMBeanServerListener> emptyList());
		}
	}

}
