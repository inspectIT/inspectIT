package rocks.inspectit.server.instrumentation.classcache.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure to hold a list of change events. Note that the structure is not synchronized and
 * can only be used in non parallel access.
 *
 * @author Stefan Siegl
 */
public class Events {

	/**
	 * List of node changes.
	 */
	private final List<NodeEvent> nodeEvents = new ArrayList<>(0);

	/**
	 * List of reference changes.
	 */
	private final List<ReferenceEvent> referenceEvents = new ArrayList<>(0);

	/**
	 * adds a node event to the cache.
	 *
	 * @param nodeEvent
	 *            the node event to add.
	 */
	public void addEvent(NodeEvent nodeEvent) {
		nodeEvents.add(nodeEvent);
	}

	/**
	 * adds a reference to the events.
	 *
	 * @param referenceEvent
	 *            the reference event to add.
	 */
	public void addEvent(ReferenceEvent referenceEvent) {
		referenceEvents.add(referenceEvent);
	}

	/**
	 * Gets {@link #nodeEvents}.
	 *
	 * @return {@link #nodeEvents}
	 */
	public List<NodeEvent> getNodeEvents() {
		return nodeEvents;
	}

	/**
	 * Gets {@link #referenceEvents}.
	 *
	 * @return {@link #referenceEvents}
	 */
	public List<ReferenceEvent> getReferenceEvents() {
		return referenceEvents;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeEvents == null) ? 0 : nodeEvents.hashCode());
		result = prime * result + ((referenceEvents == null) ? 0 : referenceEvents.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Events other = (Events) obj;
		if (nodeEvents == null) {
			if (other.nodeEvents != null) {
				return false;
			}
		} else if (!nodeEvents.equals(other.nodeEvents)) {
			return false;
		}
		if (referenceEvents == null) {
			if (other.referenceEvents != null) {
				return false;
			}
		} else if (!referenceEvents.equals(other.referenceEvents)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "EventCache [nodeEvents=" + nodeEvents + ", referenceEvents=" + referenceEvents + "]";
	}

}
