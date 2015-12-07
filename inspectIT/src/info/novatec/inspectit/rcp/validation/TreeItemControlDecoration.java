package info.novatec.inspectit.rcp.validation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to help with displaying control decorations on the tree items.
 *
 * @author Alexander Wert
 * @param <T>
 *            type of the data object in the corresponding tree item.
 */
public class TreeItemControlDecoration<T> extends AbstractItemControlDecoration<TreeItem, TreeEditor, T> {
	/**
	 * Constructor.
	 *
	 * @param treeItem
	 *            TreeItem to create decoration for.
	 */
	public TreeItemControlDecoration(TreeItem treeItem) {
		super(treeItem, treeItem.getParent());

		TreeEditor treeEditor = new TreeEditor(treeItem.getParent());
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.verticalAlignment = SWT.BOTTOM;
		treeEditor.setEditor(getControl(), treeItem, 0);
		initItemEditor(treeEditor);
	}
}
