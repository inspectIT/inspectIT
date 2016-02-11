package info.novatec.inspectit.rcp.editor.viewers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.ResultComparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Viewer comparator uses provided comparators to sort specific columns.
 * 
 * @author Patrice Boulliet
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type for which comparator is created.
 */
public abstract class AbstractViewerComparator<T extends DefaultData> extends ViewerComparator {

	/**
	 * The available sort states.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	protected enum SortState {
		/** State that won't sort. */
		NONE(SWT.NONE),
		/** State that sorts upwards. */
		UP(SWT.UP),
		/** State that sorts downwards. */
		DOWN(SWT.DOWN);

		/**
		 * The swt direction.
		 */
		private int swtDirection;

		/**
		 * Constructor to accept the swt direction.
		 * 
		 * @param swtDirection
		 *            The swt direction.
		 */
		private SortState(int swtDirection) {
			this.swtDirection = swtDirection;
		}

		/**
		 * Gets {@link #swtDirection}.
		 * 
		 * @return {@link #swtDirection}
		 */
		public int getSwtDirection() {
			return swtDirection;
		}

	}

	/**
	 * Current comparator provider.
	 */
	private ResultComparator<T> comparator;

	/**
	 * Default sort state.
	 */
	private SortState sortState = SortState.UP;

	/**
	 * Toggles the sorting of the column.
	 * 
	 * @param id
	 *            The comparator provider.
	 */
	protected void toggleSortColumn(ResultComparator<T> id) {
		if (comparator == id) { // NOPMD
			switch (sortState) {
			case NONE:
				sortState = SortState.UP;
				comparator.setAscending(true);
				break;
			case UP:
				sortState = SortState.DOWN;
				comparator.setAscending(false);
				break;
			case DOWN:
				sortState = SortState.NONE;
				break;
			default:
				break;
			}
		} else {
			comparator = id;
			sortState = SortState.UP;
			comparator.setAscending(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (null == comparator) {
			return 0;
		}

		// just return 0 if we don't want to sort
		if (SortState.NONE.equals(sortState)) {
			return 0;
		}

		T e1 = (T) o1;
		T e2 = (T) o2;

		return comparator.compare(e1, e2);
	}

	/**
	 * Gets {@link #sortState}.
	 * 
	 * @return {@link #sortState}
	 */
	protected SortState getSortState() {
		return sortState;
	}

}
