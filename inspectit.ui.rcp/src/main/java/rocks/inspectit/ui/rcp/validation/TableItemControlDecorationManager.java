package rocks.inspectit.ui.rcp.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TableItem;

/**
 * Manager for the {@link TableItemControlDecoration}s.
 *
 * @author Ivan Senic
 *
 */
public class TableItemControlDecorationManager {

	/**
	 * {@link TableEditor}s to handle the validation decoration on table rows.
	 */
	private final List<TableItemControlDecoration> tableItemControlDecorations = new ArrayList<>();

	/**
	 * Shows the error decoration data.
	 *
	 * @param tableViewer
	 *            table viewer
	 * @param data
	 *            Data bounded to the table item.
	 * @param message
	 *            Message to display.
	 */
	public void showTableItemControlDecoration(TableViewer tableViewer, Object data, String message) {
		if (null == tableViewer) {
			return;
		}

		// first check if we have it, if so shown
		for (TableItemControlDecoration decoration : tableItemControlDecorations) {
			if (data == decoration.getData()) { // NOPMD == on purpose
				decoration.show();
				decoration.setDescriptionText(message);
				return;
			}
		}

		// if not find appropriate table item to place it
		for (TableItem tableItem : tableViewer.getTable().getItems()) {
			if (tableItem.getData() == data) { // NOPMD == on purpose
				TableItemControlDecoration decoration = new TableItemControlDecoration(tableItem);
				decoration.show();
				decoration.setDescriptionText(message);

				tableItemControlDecorations.add(decoration);
				return;
			}
		}
	}

	/**
	 * Hides the error decoration for the sensor assignment.
	 *
	 * @param tableViewer
	 *            table viewer
	 * @param data
	 *            Data bounded to the table item.
	 */
	public void hideTableItemControlDecoration(TableViewer tableViewer, Object data) {
		if (null == tableViewer) {
			return;
		}

		// remove if it's there
		for (TableItemControlDecoration decoration : tableItemControlDecorations) {
			if (data == decoration.getData()) { // NOPMD == on purpose
				decoration.hide();
				return;
			}
		}
	}

	/**
	 * Class to help with displaying control decorations on the table rows.
	 *
	 * @author Ivan Senic, Alexander Wert
	 */
	private class TableItemControlDecoration extends AbstractItemControlDecoration<TableItem, TableEditor> {

		/**
		 * Constructor.
		 *
		 * @param tableItem
		 *            TableItem to create decoration for.
		 */
		TableItemControlDecoration(TableItem tableItem) {
			super(tableItem, tableItem.getParent());

			TableEditor tableEditor = new TableEditor(tableItem.getParent());
			tableEditor.horizontalAlignment = SWT.LEFT;
			tableEditor.verticalAlignment = SWT.BOTTOM;
			tableEditor.setEditor(getControl(), tableItem, 0);
			initItemEditor(tableEditor);
			tableItem.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					// in any case hide, dispose and remove
					tableItemControlDecorations.remove(TableItemControlDecoration.this);
					hide();
					dispose();
				}
			});
		}
	}
}