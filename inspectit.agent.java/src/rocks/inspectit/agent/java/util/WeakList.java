package info.novatec.inspectit.util;

import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This list is used to store elements as weak references. Especially useful to save a list of
 * {@link ClassLoader} objects in a JEE environment. Objects are added and removed as in any other
 * list, but can turn to become null. Calling {@link #removeAllNullElements()} is the only way to
 * get rid of the garbage collected elements. {@link #getHardReferences()} returns an
 * {@link ArrayList} suppressing all the references in this list which are already removed by the
 * garbage collector.
 * 
 * @param <T>
 *            The class contained in the list.
 * 
 * @author Patrice Bouillet
 * 
 */
public class WeakList<T> extends AbstractList<T> {

	/**
	 * Stores the weak references to the object.
	 */
	private List<SoftReference<T>> refs = new ArrayList<SoftReference<T>>();

	/**
	 * Returns the hard reference.
	 * 
	 * @param o
	 *            The object.
	 * @return The reference to the object.
	 */
	private T getHardReference(SoftReference<T> o) {
		if (null != o) {
			return o.get();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public T get(int index) {
		return getHardReference(refs.get(index));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(T o) {
		return refs.add(new SoftReference<T>(o));
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(int index, T o) {
		refs.add(index, new SoftReference<T>(o));
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		refs.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public T remove(int index) {
		return getHardReference(refs.remove(index));
	}

	/**
	 * {@inheritDoc}
	 */
	public T set(int index, T element) {
		return getHardReference(refs.set(index, new SoftReference<T>(element)));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object o) {
		for (int i = 0; i < size(); i++) {
			if (o == get(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return refs.size();
	}

	/**
	 * Returns a list of hard references. Skips all weak references but doesn't delete them if there
	 * are any. The list returned (especially the indices) aren't the same as the ones from this
	 * list.
	 * 
	 * @return An {@link ArrayList} containing all the hard references of this weak list.
	 */
	public List<T> getHardReferences() {
		List<T> result = new ArrayList<T>();

		for (int i = 0; i < size(); i++) {
			T tmp = get(i);

			if (null != tmp) {
				result.add(tmp);
			}
		}

		return result;
	}

	/**
	 * Calling this method removes all the garbage collected elements in this list which appear to
	 * be null now.<br>
	 * TODO: call this method periodically!
	 */
	public void removeAllNullElements() {
		for (int i = size() - 1; i >= 0; i--) {
			if (get(i) == null) {
				remove(i);
			}
		}
	}

}
