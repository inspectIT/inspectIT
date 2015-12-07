package info.novatec.inspectit.rcp.validation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to help with displaying control decorations on the tree items.
 *
 * @author Alexander Wert
 * @param <T>
 *            type of the data object in the corresponding tree item.
 */
public class TreeItemControlDecoration<T> extends ControlDecoration {
	/**
	 * TreeItem to create decoration for.
	 */
	private final TreeItem treeItem;

	/**
	 * Internal {@link TreeEditor} to show decoration.
	 */
	private final TreeEditor treeEditor;

	/**
	 * Constructor.
	 *
	 * @param treeItem
	 *            TreeItem to create decoration for.
	 */
	public TreeItemControlDecoration(TreeItem treeItem) {
		super(new Composite(treeItem.getParent(), SWT.NONE), SWT.BOTTOM);
		Assert.isNotNull(treeItem);

		this.treeItem = treeItem;
		treeEditor = new TreeEditor(treeItem.getParent());
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.verticalAlignment = SWT.BOTTOM;
		treeEditor.setEditor(getControl(), treeItem, 0);

		setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
		hide();
	}

	/**
	 * Sets the dispose listener for the tree item.
	 *
	 * @param disposeListener
	 *            listener to be notified on item disposal
	 */
	public void setDisposeListener(DisposeListener disposeListener) {
		getTreeItem().addDisposeListener(disposeListener);
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	@SuppressWarnings("unchecked")
	public T getData() {
		return (T) getTreeItem().getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		Control c = getControl();
		if (!getTreeItem().isDisposed()) {
			treeEditor.dispose();
		}

		super.dispose();

		// we need to dispose the composite that we have created
		if (null != c) {
			c.dispose();
		}
	}

	/**
	 * Gets {@link #treeItem}.
	 *
	 * @return {@link #treeItem}
	 */
	public TreeItem getTreeItem() {
		return treeItem;
	}
}
