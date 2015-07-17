package info.novatec.inspectit.rcp.editor.table;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.viewers.AbstractViewerComparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Abstract class for all table views that need remote sorting. Implementing classes should
 * implement method {@link #sortRemotely(ResultComparator)} which will be called when the sorting
 * column is selected. In this method implementing classes should refresh the result.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type to compare on.
 */
public abstract class RemoteTableViewerComparator<T extends DefaultData> extends AbstractViewerComparator<T> {

	/**
	 * Adds a column to this comparator so it can be used to sort by.
	 * 
	 * @param column
	 *            The {@link TableColumn} implementation. comparatorProvider The id of the
	 *            {@link TableColumn} (user-defined).
	 * @param comparator
	 *            Comparator that will be used for the given column.
	 */
	public final void addColumn(final TableColumn column, final ResultComparator<T> comparator) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleSortColumn(comparator);
				final SortState sortState = getSortState();

				Table table = column.getParent();
				table.setSortColumn(column);
				table.setSortDirection(sortState.getSwtDirection());

				try {
					BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
						@Override
						public void run() {
							if (sortState != SortState.NONE) {
								sortRemotely(comparator);
							} else {
								sortRemotely(null);
							}
						}
					});
				} catch (Exception exception) {
					InspectIT.getDefault().createErrorDialog("Exception occurred trying to remotely sort on the selected column.", exception, -1);
				}
			}
		});
	}

	/**
	 * Implementing classes should call the remote service and refresh the input of the table by
	 * using the given {@link ResultComparator}. Progress can be reported to given monitor.
	 * 
	 * @param resultComparator
	 *            Result comparator that should be used in the remote call. <code>null</code> can
	 *            also be passed, meaning that no sorting or default sorting should be used.
	 */
	protected abstract void sortRemotely(ResultComparator<T> resultComparator);

	/**
	 * {@inheritDoc}
	 * <P>
	 * No sorting on the UI.
	 */
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		return 0;
	}
}
