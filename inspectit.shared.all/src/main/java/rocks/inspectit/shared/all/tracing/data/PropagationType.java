package rocks.inspectit.shared.all.tracing.data;

/**
 * This enum defines how reference spans are propagated related to cross-process propagation.
 *
 * @author Ivan Senic
 *
 */
public enum PropagationType {

	/**
	 * Process propagation. Means that reference spans are executed in the same process and
	 * inter-process propagation was used.
	 */
	PROCESS,

	/**
	 * Propagation via HTTP or HTTPS.
	 */
	HTTP,

	/**
	 * Propagation via Java Message Service.
	 */
	JMS;

}
