package rocks.inspectit.ui.rcp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a thread safe list that is designed for storing lists of listeners. The
 * implementation is optimized for minimal memory footprint, frequent reads and infrequent writes.
 * Modification of the list is synchronized and relatively expensive, while accessing the listeners
 * is very fast. Readers are given access to the underlying array data structure for reading, with
 * the trust that they will not modify the underlying array.
 * <p>
 * <a name="same">A listener list handles the <i>same</i> listener being added multiple times, and
 * tolerates removal of listeners that are the same as other listeners in the list. For this
 * purpose, listeners can be compared with each other using either equality or identity, as
 * specified in the list constructor.</a>
 * <p>
 * <b>IMPORTANT:</b> The class is licensed under the Eclipse Public License v1.0 as it includes the
 * code from the {@link org.eclipse.core.runtime.ListenerList} class belonging to the Eclipse Rich
 * Client Platform. EPL v1.0 license can be found <a
 * href="https://www.eclipse.org/legal/epl-v10.html">here</a>.
 * <p>
 * Please relate to the LICENSEEXCEPTIONS.txt file for more information about license exceptions
 * that apply regarding to InspectIT and Eclipse RCP and/or EPL Components.
 * 
 * @param <E>
 *            Generic parameter that defines the type of this listener list.
 */
@SuppressWarnings("unchecked")
public class ListenerList<E> implements Iterable<E> {

	/**
	 * The empty array singleton instance.
	 */
	private final E[] emptyArray = (E[]) new Object[0];

	/**
	 * The available comparison modes.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum Mode {
		/**
		 * Mode constant (value 0) indicating that listeners should be considered the <a
		 * href="#same">same</a> if they are equal.
		 */
		EQUALITY,

		/**
		 * Mode constant (value 1) indicating that listeners should be considered the <a
		 * href="#same">same</a> if they are identical.
		 */
		IDENTITY;
	}

	/**
	 * Indicates the comparison mode used to determine if two listeners are equivalent.
	 */
	private final boolean identity;

	/**
	 * The list of listeners. Initially empty but initialized to an array of size capacity the first
	 * time a listener is added. Maintains invariant: listeners != null.
	 */
	private volatile E[] listeners = emptyArray;

	/**
	 * Creates a listener list in which listeners are compared using equality.
	 */
	public ListenerList() {
		this(Mode.EQUALITY);
	}

	/**
	 * Creates a listener list using the provided comparison mode.
	 * 
	 * @param mode
	 *            The mode used to determine if listeners are the <a href="#same">same</a>.
	 */
	public ListenerList(Mode mode) {
		this.identity = Mode.IDENTITY.equals(mode);
	}

	/**
	 * Adds a listener to this list. This method has no effect if the <a href="#same">same</a>
	 * listener is already registered.
	 * 
	 * @param listener
	 *            the non-<code>null</code> listener to add
	 */
	public synchronized void add(E listener) {
		// This method is synchronized to protect against multiple threads
		// adding or removing listeners concurrently. This does not block
		// concurrent readers.
		if (listener == null) {
			throw new IllegalArgumentException();
		}

		// check for duplicates
		final int oldSize = listeners.length;
		for (int i = 0; i < oldSize; ++i) {
			E listener2 = listeners[i];
			if (identity ? listener == listener2 : listener.equals(listener2)) { // NOPMD
				return;
			}
		}

		// Thread safety: create new array to avoid affecting concurrent readers
		E[] newListeners = (E[]) new Object[oldSize + 1];
		System.arraycopy(listeners, 0, newListeners, 0, oldSize);
		newListeners[oldSize] = listener;

		// atomic assignment
		this.listeners = newListeners;
	}

	/**
	 * Returns an array containing all the registered listeners. The resulting array is unaffected
	 * by subsequent adds or removes. If there are no listeners registered, the result is an empty
	 * array. Use this method when notifying listeners, so that any modifications to the listener
	 * list during the notification will have no effect on the notification itself.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned array.
	 * 
	 * @return the list of registered listeners
	 */
	public E[] getListeners() {
		return listeners;
	}

	/**
	 * Returns whether this listener list is empty.
	 * 
	 * @return <code>true</code> if there are no registered listeners, and <code>false</code>
	 *         otherwise
	 */
	public boolean isEmpty() {
		return listeners.length == 0;
	}

	/**
	 * Removes a listener from this list. Has no effect if the <a href="#same">same</a> listener was
	 * not already registered.
	 * 
	 * @param listener
	 *            the non-<code>null</code> listener to remove
	 */
	public synchronized void remove(E listener) {
		// This method is synchronized to protect against multiple threads
		// adding or removing listeners concurrently. This does not block
		// concurrent readers.
		if (listener == null) {
			throw new IllegalArgumentException();
		}

		int oldSize = listeners.length;
		for (int i = 0; i < oldSize; ++i) {
			E listener2 = listeners[i];
			if (identity ? listener == listener2 : listener.equals(listener2)) { // NOPMD
				if (oldSize == 1) {
					listeners = emptyArray;
				} else {
					// Thread safety: create new array to avoid affecting
					// concurrent readers
					E[] newListeners = (E[]) new Object[oldSize - 1];
					System.arraycopy(listeners, 0, newListeners, 0, i);
					System.arraycopy(listeners, i + 1, newListeners, i, oldSize - i - 1);
					// atomic assignment to field
					this.listeners = newListeners;
				}
				return;
			}
		}
	}

	/**
	 * Returns the number of registered listeners.
	 * 
	 * @return the number of registered listeners
	 */
	public int size() {
		return listeners.length;
	}

	/**
	 * Removes all listeners from this list.
	 */
	public synchronized void clear() {
		listeners = emptyArray;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr(listeners);
	}

	/**
	 * The iterator implementation for this listener list.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class Itr implements Iterator<E> {

		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		private int cursor = 0;

		/**
		 * The listeners for this iterator.
		 */
		private E[] listeners;

		/**
		 * The reference to the generic listeners array is passed here because in the meantime,
		 * while iterating over this list, some new objects could be added which is NOT reflected.
		 * 
		 * @param listeners
		 *            The listeners.
		 */
		public Itr(E[] listeners) {
			this.listeners = listeners;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			return cursor != listeners.length;
		}

		/**
		 * {@inheritDoc}
		 */
		public E next() {
			try {
				E next = listeners[cursor];
				cursor++;
				return next;
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException(); // NOPMD
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}