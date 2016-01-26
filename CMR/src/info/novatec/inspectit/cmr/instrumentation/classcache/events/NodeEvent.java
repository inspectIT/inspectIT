package info.novatec.inspectit.cmr.instrumentation.classcache.events;

import info.novatec.inspectit.instrumentation.classcache.ImmutableType;

/**
 * Event describing the change in the class cache.
 * 
 * @author Ivan Senic
 * 
 */
public class NodeEvent {

	/**
	 * Enumeration describing the event type.
	 */
	public enum NodeEventType {
		/** the type was not yet available in the structure. */
		NEW,

		/** an available type was changed. */
		CHANGED
	}

	/**
	 * Enumeration describing the event details.
	 */
	public enum NodeEventDetails {
		/** Added a new type, that is not yet initialized. */
		NOT_INITIALIZED,

		/** Added a hash to the type. */
		HASH_ADDED,

		/** Added a new type or initialized a existing type. */
		INITIALIZED,

		/** We understand the method belonging to a type. */
		METHOD_CHANGED_OR_ADDED,

		/** Modifiers of the type changed. */
		MODIFIERS_CHANGED
	}

	/**
	 * Type that change has been made on.
	 */
	public ImmutableType type; // NOCHK NOPMD

	/**
	 * {@link NodeEventType}.
	 */
	public NodeEventType eventType; // NOCHK NOPMD

	/**
	 * {@link NodeEventDetails}.
	 */
	public NodeEventDetails eventDetails; // NOCHK NOPMD

	/**
	 * Stores additional information: - in case of HASH_ADDED the newly added hash is stored.
	 */
	public Object additionalInformation; // NOCHK NOPMD

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            Type that change has been made on.
	 * @param eventType
	 *            {@link NodeEventType}.
	 * @param eventDetails
	 *            {@link NodeEventDetails}.
	 * @param additionalInformation
	 *            Additional information: - in case of HASH_ADDED the newly added hash is stored.
	 */
	public NodeEvent(ImmutableType type, NodeEventType eventType, NodeEventDetails eventDetails, Object additionalInformation) {
		this.type = type;
		this.eventType = eventType;
		this.eventDetails = eventDetails;
		this.additionalInformation = additionalInformation;
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param type
	 *            Type that change has been made on.
	 * @param eventType
	 *            {@link NodeEventType}.
	 * @param eventDetails
	 *            {@link NodeEventDetails}.
	 */
	public NodeEvent(ImmutableType type, NodeEventType eventType, NodeEventDetails eventDetails) {
		this(type, eventType, eventDetails, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalInformation == null) ? 0 : additionalInformation.hashCode());
		result = prime * result + ((eventDetails == null) ? 0 : eventDetails.hashCode());
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		NodeEvent other = (NodeEvent) obj;
		if (additionalInformation == null) {
			if (other.additionalInformation != null) {
				return false;
			}
		} else if (!additionalInformation.equals(other.additionalInformation)) {
			return false;
		}
		if (eventDetails != other.eventDetails) {
			return false;
		}
		if (eventType != other.eventType) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NodeEvent [type=" + type + ", event=" + eventType + ", details=" + eventDetails + ", additionalInformation=" + additionalInformation + "]";
	}
}
