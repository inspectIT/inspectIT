package info.novatec.inspectit.rcp.editor.table;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * Abstract class for editing support in the table.
 * 
 * @param <E>
 *            Element being edited.
 * @param <T>
 *            Type of the value expected from editing.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractTableEditingSupport<E, T> extends EditingSupport {

	/**
	 * Table viewer to edit.
	 */
	private TableViewer viewer;

	/**
	 * Default constructor.
	 * 
	 * @param viewer
	 *            Table viewer to edit.
	 */
	public AbstractTableEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	/**
	 * Get the value to set to the editor.
	 * 
	 * @param element
	 *            Element
	 * @return Value to set to the editor
	 */
	protected abstract T getValueImpl(E element);

	/**
	 * Sets the new value on the given element.
	 * 
	 * @param element
	 *            Element
	 * @param value
	 *            Value
	 */
	protected abstract void setValueImpl(E element, T value);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(viewer.getTable());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object getValue(Object element) {
		return getValueImpl((E) element);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		setValueImpl((E) element, (T) value);
		viewer.update(element, null);
	}

}
