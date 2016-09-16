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
	 * Functional instrumentation for allowing the agent to interact with the client.
	 */
	EUM_SERVLET_OR_FILTER_INSPECTION;

}
