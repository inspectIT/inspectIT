package info.novatec.inspectit.util;

/**
 * Simple Pair of two objects.
 *
 * TODO: remove when commons lang are updated to version 3.0+
 *
 * @author Alexander Wert
 *
 * @param <A>
 *            Type of first object.
 * @param <B>Type
 *            of second object.
 */
public class Pair<A, B> {
	/**
	 * First object.
	 */
	private A first;
	/**
	 * Second object.
	 */
	private B second;

	/**
	 * Constructor.
	 *
	 * @param first
	 *            First object.
	 * @param second
	 *            Second object.
	 */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Gets {@link #first}.
	 *
	 * @return {@link #first}
	 */
	public A getFirst() {
		return first;
	}

	/**
	 * Sets {@link #first}.
	 *
	 * @param first
	 *            New value for {@link #first}
	 */
	public void setFirst(A first) {
		this.first = first;
	}

	/**
	 * Gets {@link #second}.
	 *
	 * @return {@link #second}
	 */
	public B getSecond() {
		return second;
	}

	/**
	 * Sets {@link #second}.
	 *
	 * @param second
	 *            New value for {@link #second}
	 */
	public void setSecond(B second) {
		this.second = second;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getFirst() == null) ? 0 : getFirst().hashCode());
		result = prime * result + ((getSecond() == null) ? 0 : getSecond().hashCode());
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
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (getFirst() == null) {
			if (other.getFirst() != null) {
				return false;
			}
		} else if (!getFirst().equals(other.getFirst())) {
			return false;
		}
		if (getSecond() == null) {
			if (other.getSecond() != null) {
				return false;
			}
		} else if (!getSecond().equals(other.getSecond())) {
			return false;
		}
		return true;
	}
}
