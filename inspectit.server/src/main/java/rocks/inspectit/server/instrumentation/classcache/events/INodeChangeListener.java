package rocks.inspectit.server.instrumentation.classcache.events;

/**
 * Interface for listening on the class cache events.
 * 
 * @author Ivan Senic
 * 
 */
public interface INodeChangeListener {

	/**
	 * Informs a listener that a change has been made on a node in the class cache.
	 * 
	 * @param event
	 *            Event object describing the change.
	 */
	void informNodeChange(NodeEvent event);

	/**
	 * Informs a listener that a change has been made on a reference in the class cache.
	 * 
	 * @param event
	 *            Event object describing the change.
	 */
	void informReferenceChange(ReferenceEvent event);
}
