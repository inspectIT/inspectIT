package info.novatec.inspectit.rcp.validation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class to help with displaying control decorations on the table rows.
 *
 * @author Ivan Senic, Alexander Wert
 * @param <T>
 *            type of the data object in the corresponding table item.
 */
public class TableItemControlDecoration<T> extends ControlDecoration {

	/**
	 * TableItem to create decoration for.
	 */
	private final TableItem tableItem;

	/**
	 * Internal {@link TableEditor} to show decoration.
	 */
	private final TableEditor tableEditor;

	/**
	 * Constructor.
	 *
	 * @param tableItem
	 *            TableItem to create decoration for.
	 */
	public TableItemControlDecoration(TableItem tableItem) {
		super(new Composite(tableItem.getParent(), SWT.NONE), SWT.BOTTOM);
		Assert.isNotNull(tableItem);

		this.tableItem = tableItem;
		tableEditor = new TableEditor(tableItem.getParent());
		tableEditor.horizontalAlignment = SWT.LEFT;
		tableEditor.verticalAlignment = SWT.BOTTOM;
		tableEditor.setEditor(getControl(), tableItem, 0);

		setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
		hide();
	}

	/**
	 * Sets the dispose listener for the table item.
	 *
	 * @param disposeListener
	 *            listener to be notified on item disposal
	 */
	public void setDisposeListener(DisposeListener disposeListener) {
		tableItem.addDisposeListener(disposeListener);
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	@SuppressWarnings("unchecked")
	public T getData() {
		return (T) tableItem.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		Control c = getControl();
		if (!tableItem.isDisposed()) {
			tableEditor.dispose();
		}

		super.dispose();

		// we need to dispose the composite that we have created
		if (null != c) {
			c.dispose();
		}
	}
}
