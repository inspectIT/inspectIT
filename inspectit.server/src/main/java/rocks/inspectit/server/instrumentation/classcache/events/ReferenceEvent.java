package rocks.inspectit.server.instrumentation.classcache.events;

import rocks.inspectit.shared.all.instrumentation.classcache.Type;

/**
 * Reference event describes new connection between two types.
 *
 * @author Ivan Senic
 *
 */
public class ReferenceEvent {

	/**
	 * Enumeration describing the type of reference.
	 *
	 * @author Ivan Senic
	 *
	 */
	public enum ReferenceType {
		/**
		 * Reference made from class type to super class type.
		 */
		SUPERCLASS,

		/**
		 * Reference made from interface type to super interface type.
		 */
		SUPERINTERFACE,

		/**
		 * Reference made from class type to realizing interface type.
		 */
		REALIZE_INTERFACE,

		/**
		 * Reference made from type with annotations to annotation type.
		 */
		ANNOTATION;
	}

	/**
	 * {@link Type} that is referring.
	 */
	private final Type from;

	/**
	 * {@link Type} that is referred to.
	 */
	private final Type to;

	/**
	 * {@link ReferenceType}.
	 */
	private final ReferenceType referenceType;

	/**
	 * DEfault constructor.
	 *
	 * @param from
	 *            {@link Type} that is referring.
	 * @param to
	 *            {@link Type} that is referred to.
	 * @param referenceType
	 *            {@link ReferenceType}.
	 */
	public ReferenceEvent(Type from, Type to, ReferenceType referenceType) {
		this.from = from;
		this.to = to;
		this.referenceType = referenceType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((referenceType == null) ? 0 : referenceType.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		ReferenceEvent other = (ReferenceEvent) obj;
		if (referenceType != other.referenceType) {
			return false;
		}
		if (from == null) {
			if (other.from != null) {
				return false;
			}
		} else if (!from.equals(other.from)) {
			return false;
		}
		if (to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!to.equals(other.to)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ReferenceEvent [from=" + from + ", to=" + to + ", eventType=" + referenceType + "]";
	}

}
