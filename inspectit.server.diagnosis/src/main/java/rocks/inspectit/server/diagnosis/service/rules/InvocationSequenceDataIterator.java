package rocks.inspectit.server.diagnosis.service.rules;

import java.util.Iterator;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Iterator for the invocation tree of an {@link InvocationSequenceData}.
 *
 * This iterator visits the {@link InvocationSequenceData} elements in the same order as the
 * corresponding methods have been originally called in the calling tree.
 *
 * @author Alexander Wert
 *
 */
public class InvocationSequenceDataIterator implements Iterator<InvocationSequenceData> {

	/**
	 * Next element in the iteration.
	 */
	private InvocationSequenceData nextElement;

	/**
	 * Current depth in the calling tree.
	 */
	private int depth = -1;

	/**
	 * Next calculated depth.
	 */
	private int nextDepth = 0;

	/**
	 * The {@link InvocationSequenceData} element determining the iteration end.
	 */
	private InvocationSequenceData iterationEnd = null;

	/**
	 * Constructor.
	 * 
	 * @param startFrom
	 *            the {@link InvocationSequenceData} element to start the iteration from.
	 */
	public InvocationSequenceDataIterator(InvocationSequenceData startFrom) {
		this(startFrom, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param startFrom
	 *            the {@link InvocationSequenceData} element to start the iteration from.
	 * @param onlySubTree
	 *            if true, only the sub-tree of the given {@link InvocationSequenceData} element is
	 *            iterated. If false, parent items (located after the given
	 *            {@link InvocationSequenceData} element) are iterated as well.
	 */
	public InvocationSequenceDataIterator(InvocationSequenceData startFrom, boolean onlySubTree) {
		if (null == startFrom) {
			throw new IllegalArgumentException("Cannot iterate on a null invocation sequence.");
		}

		this.nextElement = startFrom;

		if (onlySubTree) {
			iterationEnd = startFrom.getParentSequence();
		}

		while (null != startFrom.getParentSequence()) {
			nextDepth++;
			startFrom = startFrom.getParentSequence();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return null != nextElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvocationSequenceData next() {
		InvocationSequenceData result = nextElement;
		depth = nextDepth;
		nextElement = findNext(nextElement, null);
		return result;
	}

	/**
	 * Returns the depth in the calling tree of the element returned by the last call to the
	 * {@link #next()} method.
	 * 
	 * @return Returns the depth in the calling tree of the element returned by the last call to the
	 *         {@link #next()} method.
	 */
	public int currentDepth() {
		return depth;
	}

	/**
	 * Determines the next element for iteration.
	 * 
	 * @param current
	 *            current element in iteration.
	 * @param child
	 *            the previous child element in the children list of the current
	 *            {@link InvocationSequenceData} element.
	 * @return Returns the next element for iteration.
	 */
	private InvocationSequenceData findNext(InvocationSequenceData current, InvocationSequenceData child) {
		if (iterationEnd == current) { // NOPMD no equals on purpose
			return null;
		}

		List<InvocationSequenceData> nestedSequences = current.getNestedSequences();

		if (nestedSequences.isEmpty()) {
			nextDepth--;
			return findNext(current.getParentSequence(), current);
		} else {
			// int childIndex = nestedSequences.indexOf(child);
			int childIndex = -1;
			if (null != child) {
				for (InvocationSequenceData childCandidate : nestedSequences) {
					childIndex++;
					if (childCandidate == child) { // NOPMD no equals on purpose
						break;
					}
				}
				if (childIndex >= nestedSequences.size()) {
					throw new IllegalStateException("Parent list does not contain this child invocation sequence.");
				}
			}

			int nextIndex = childIndex + 1;
			if (nextIndex < nestedSequences.size()) {
				nextDepth++;
				return nestedSequences.get(nextIndex);
			} else {
				nextDepth--;
				return findNext(current.getParentSequence(), current);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Modifications of invocation sequences through this iterator are not allowed.");
	}

}