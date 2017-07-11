package rocks.inspectit.server.diagnosis.results;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a Set which is limited to a defined maximum size.
 *
 * @author Tobias Angerstein
 *
 * @param <E>
 *            Inner Type of set
 */
public class LimitedSet<E> extends HashSet<E> {

	/**
	 * Serialization Id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The defined maximum size.
	 */
	private int maximumSize;

	/**
	 * Constructor.
	 *
	 * @param max
	 *            Sets the maximum number of elements
	 */
	public LimitedSet(int max) {
		this.maximumSize = max;
	}

	@Override
	public boolean add(E value) {
		if (this.size() >= maximumSize) {
			return false;
		} else {
			super.add(value);
			return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> paramCollection) {
		if ((paramCollection.size() + this.size()) <= maximumSize) {
			super.addAll(paramCollection);
			return true;
		}
		return false;
	}

	/**
	 * Provides copy of current set as {@link HashSet}.
	 *
	 * @return {@link HashSet} copy
	 */
	public Set<E> getHashSetCopy() {
		return new HashSet<>(this);
	}
}