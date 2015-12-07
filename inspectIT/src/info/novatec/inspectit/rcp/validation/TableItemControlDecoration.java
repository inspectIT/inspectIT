package info.novatec.inspectit.rcp.validation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class to help with displaying control decorations on the table rows.
 *
 * @author Ivan Senic, Alexander Wert
 * @param <T>
 *            type of the data object in the corresponding table item.
 */
public class TableItemControlDecoration<T> extends AbstractItemControlDecoration<TableItem, TableEditor, T> {
	/**
	 * Constructor.
	 *
	 * @param tableItem
	 *            TableItem to create decoration for.
	 */
	public TableItemControlDecoration(TableItem tableItem) {
		super(tableItem, tableItem.getParent());

		TableEditor tableEditor = new TableEditor(tableItem.getParent());
		tableEditor.horizontalAlignment = SWT.LEFT;
		tableEditor.verticalAlignment = SWT.BOTTOM;
		tableEditor.setEditor(getControl(), tableItem, 0);
		initItemEditor(tableEditor);
	}
}
