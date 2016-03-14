package rocks.inspectit.ui.rcp.validation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/**
 * Abstract Class to help with displaying control decorations on items of views like table, tree,
 * etc.
 *
 * @author Alexander Wert
 * @param <I>
 *            Item type
 * @param <C>
 *            Control Editor type
 */
public abstract class AbstractItemControlDecoration<I extends Item, C extends ControlEditor> extends ControlDecoration {

	/**
	 * {@link Item} to create decoration for.
	 */
	private final I item;

	/**
	 * Internal {@link ControlEditor} to show decoration.
	 */
	private C itemEditor;

	/**
	 * Constructor.
	 *
	 * @param item
	 *            TreeItem to create decoration for.
	 * @param parent
	 *            The parent composite where to draw the decoration.
	 */
	public AbstractItemControlDecoration(I item, Composite parent) {
		super(new Composite(parent, SWT.NONE), SWT.BOTTOM);
		Assert.isNotNull(item);
		this.item = item;
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public Object getData() {
		return getItem().getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		Control c = getControl();
		if (!getItem().isDisposed()) {
			getItemEditor().dispose();
		}

		super.dispose();

		// we need to dispose the composite that we have created
		if (null != c) {
			c.dispose();
		}
	}

	/**
	 * Gets {@link #item}.
	 *
	 * @return {@link #item}
	 */
	public I getItem() {
		return item;
	}

	/**
	 * Initializes the control editor.
	 *
	 * @param itemEditor
	 *            the editor to initialize
	 */
	protected void initItemEditor(C itemEditor) {
		this.setItemEditor(itemEditor);
		setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
		hide();
	}

	/**
	 * Gets {@link #itemEditor}.
	 *
	 * @return {@link #itemEditor}
	 */
	public C getItemEditor() {
		return itemEditor;
	}

	/**
	 * Sets {@link #itemEditor}.
	 *
	 * @param itemEditor
	 *            New value for {@link #itemEditor}
	 */
	public void setItemEditor(C itemEditor) {
		this.itemEditor = itemEditor;
	}
}
