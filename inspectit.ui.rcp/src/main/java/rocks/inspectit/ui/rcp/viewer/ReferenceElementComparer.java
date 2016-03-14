package rocks.inspectit.ui.rcp.viewer;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * {@link IElementComparer} that compares elements on the reference basis.
 *
 * @author Ivan Senic
 *
 */
public final class ReferenceElementComparer implements IElementComparer {

	/**
	 * Static instance for all to use.
	 */
	public static final ReferenceElementComparer INSTANCE = new ReferenceElementComparer();

	/**
	 * Private constructor. Usage via {@link #INSTANCE}.
	 */
	private ReferenceElementComparer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object a, Object b) { // NOPMD
		return a == b; // NOPMD reference compare on purpose
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode(Object element) {
		return System.identityHashCode(element);
	}

}
