package rocks.inspectit.ui.rcp.editor.tree;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.ui.rcp.editor.viewers.AbstractViewerComparator;

/**
 * Local table viewer comparator uses provided comparators to sort specific columns.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type for which comparator is created.
 */
public class TreeViewerComparator<T extends DefaultData> extends AbstractViewerComparator<T> {

	/**
	 * Adds a column to this comparator so it can be used to sort by.
	 * 
	 * @param column
	 *            The {@link TreeColumn} implementation. comparatorProvider The id of the
	 *            {@link TableColumn} (user-defined).
	 * @param comparator
	 *            Comparator that will be used for the given column.
	 */
	public final void addColumn(final TreeColumn column, final ResultComparator<T> comparator) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleSortColumn(comparator);

				Tree tree = column.getParent();
				tree.setSortColumn(column);
				tree.setSortDirection(getSortState().getSwtDirection());
			}
		});
	}

}
