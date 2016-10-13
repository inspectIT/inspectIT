package rocks.inspectit.shared.all.instrumentation.config;

/**
 * All special instrumentation types that we support at the moment.
 *
 * @author Ivan Senic
 *
 */
public enum SpecialInstrumentationType {

	/**
	 * Special instrumentation for the class loading delegation.
	 */
	CLASS_LOADING_DELEGATION,

	/**
	 * Special instrumentation for the javax.management.MBeanServerFactory to intercept adding of
	 * MBean servers.
	 */
	MBEAN_SERVER_ADD,

	/**
	 * Special instrumentation for the javax.management.MBeanServerFactory to intercept removing of
	 * MBean servers.
	 */
	MBEAN_SERVER_REMOVE;

}
