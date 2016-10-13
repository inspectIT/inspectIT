package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Collection;

import javax.management.MBeanServer;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.sensor.jmx.IMBeanServerListener;

/**
 * @author Ivan Senic
 *
 */
public class MBeanServerInterceptorHook implements ISpecialHook {

	/**
	 * Method that hooks is instrumenting for adding a server.
	 */
	private static final Object ADD_METHOD = "addMBeanServer";

	/**
	 * Method that hooks is instrumenting for removing a server.
	 */
	private static final Object REMOVE_METHOD = "removeMBeanServer";

	/**
	 * All the listeners to delegate the information to.
	 */
	private final Collection<IMBeanServerListener> mBeanServerListeners;

	/**
	 * Default constructor.
	 *
	 * @param mBeanServerListeners
	 *            All the listeners to delegate the information to.
	 */
	public MBeanServerInterceptorHook(Collection<IMBeanServerListener> mBeanServerListeners) {
		this.mBeanServerListeners = mBeanServerListeners;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		String methodName = ssc.getTargetMethodName();
		MBeanServer server = getServerFromParameters(parameters);
		if (ADD_METHOD.equals(methodName)) {
			mbeanServerAdded(server);
		} else if (REMOVE_METHOD.equals(methodName)) {
			mbeanServerRemoved(server);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}

	/**
	 * Check if the parameter array has exactly size 1 and that object in the array is instanceOf
	 * {@link MBeanServer}. If so returns the server object, otherwise returns <code>null</code>.
	 *
	 * @param params
	 *            Method parameters
	 * @return Returns the server object is found in parameters, otherwise returns
	 *         <code>null</code>.
	 */
	private MBeanServer getServerFromParameters(Object[] params) {
		if ((null != params) && (params.length == 1)) {
			Object p = params[0];
			if (p instanceof MBeanServer) {
				return (MBeanServer) p;
			}
		}
		return null;
	}

	/**
	 * Informs {@link #mBeanServerListeners} that server has been added.
	 *
	 * @param server
	 *            {@link MBeanServer}
	 */
	private void mbeanServerAdded(MBeanServer server) {
		if (null == server) {
			return;
		}

		if (CollectionUtils.isNotEmpty(mBeanServerListeners)) {
			for (IMBeanServerListener listener : mBeanServerListeners) {
				listener.mbeanServerAdded(server);
			}
		}
	}

	/**
	 * Informs {@link #mBeanServerListeners} that server has been removed.
	 *
	 * @param server
	 *            {@link MBeanServer}
	 */
	public void mbeanServerRemoved(MBeanServer server) {
		if (null == server) {
			return;
		}

		if (CollectionUtils.isNotEmpty(mBeanServerListeners)) {
			for (IMBeanServerListener listener : mBeanServerListeners) {
				listener.mbeanServerRemoved(server);
			}
		}
	}

}
