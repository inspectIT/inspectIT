package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.util.ArrayList;

/**
 * Set implemented by {@link ArrayList}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of elements in the set.
 */
public class ArraySet<E> extends ArrayList<E> implements UpdateableSet<E> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -871764893452300085L;

	/**
	 * No-arg constructor.
	 */
	public ArraySet() {
	}

	/**
	 * @param initialCapacity
	 *            Initial capacity or array list backing the set.
	 */
	public ArraySet(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Maintaining set definition of add.
	 */
	@Override
	public boolean add(E e) {
		if (super.contains(e)) {
			return false;
		}
		return super.add(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addOrUpdate(E e) {
		int i = super.indexOf(e);
		if (i >= 0) {
			super.set(i, e);
		} else {
			super.add(e);
		}
	}
}
