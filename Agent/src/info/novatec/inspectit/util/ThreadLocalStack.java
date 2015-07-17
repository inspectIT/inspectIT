package info.novatec.inspectit.util;

import java.util.LinkedList;

/**
 * The ThreadLocalStack class extends {@link ThreadLocal} to have a {@link LinkedList} to be used as
 * a stack. Extending {@link ThreadLocal} and using the {@link #initialValue()} method helps to keep
 * the variable private to the actual {@link Thread}. A {@link LinkedList} is used because it can be
 * easily used as a FIFO stack.
 * 
 * @param <E>
 *            elements of the linked list.
 * 
 * @author Patrice Bouillet
 */
public class ThreadLocalStack<E> extends ThreadLocal<LinkedList<E>> {

	/**
	 * {@inheritDoc}
	 */
	public LinkedList<E> initialValue() { // NOPMD
		return new LinkedList<E>();
	}

	/**
	 * Pushes the specified value onto the stack.
	 * 
	 * @param value
	 *            the value to push onto the stack.
	 */
	public void push(E value) {
		super.get().addLast(value);
	}

	/**
	 * Returns the last pushed value.
	 * 
	 * @return The last pushed value.
	 */
	public E pop() {
		return super.get().removeLast();
	}

	/**
	 * Returns the last pushed value without removing it.
	 * 
	 * @return The last pushed value.
	 */
	public E getLast() {
		return super.get().getLast();
	}

	/**
	 * Returns the first value pushed onto the stack.
	 * 
	 * @return The first value pushed onto the stack.
	 */
	public E getAndRemoveFirst() {
		return super.get().removeFirst();
	}

}
