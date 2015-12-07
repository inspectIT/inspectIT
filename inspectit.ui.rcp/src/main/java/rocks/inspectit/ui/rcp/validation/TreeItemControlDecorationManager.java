package rocks.inspectit.ui.rcp.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * Manager for the {@link TreeItemControlDecoration}s.
 *
 * @author Ivan Senic, Alexander Wert
 *
 */
public class TreeItemControlDecorationManager {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TreeItemControlDecorationManager.class);

	/**
	 * List of decorations.
	 */
	private final List<TreeItemControlDecoration> treeItemControlDecorations = new ArrayList<>();

	/**
	 * Shows the error decoration data.
	 *
	 * @param treeViewer
	 *            tree viewer
	 * @param data
	 *            Data bounded to the tree item.
	 * @param message
	 *            Message to display.
	 */
	public void showTreeItemControlDecoration(TreeViewer treeViewer, Object data, String message) {
		if (null == treeViewer) {
			return;
		}

		// first check if we have it, if so shown
		for (TreeItemControlDecoration decoration : treeItemControlDecorations) {
			if (data == decoration.getData()) { // NOPMD == on purpose
				decoration.show();
				decoration.setDescriptionText(message);
				return;
			}
		}

		// if not find appropriate table item to place it
		for (TreeItem treeItem : getAllTreeItems(treeViewer)) {
			if (treeItem.getData() == data) { // NOPMD == on purpose
				TreeItemControlDecoration decoration = new TreeItemControlDecoration(treeItem);
				decoration.show();
				decoration.setDescriptionText(message);
				treeItemControlDecorations.add(decoration);
				return;
			}
		}
	}

	/**
	 * Returns all tree items (also the nested ones) of the tree associated with the
	 * {@link #treeViewer}.
	 *
	 * @param treeViewer
	 *            the tree view to retrieve the items for
	 *
	 * @return List of all tree items.
	 */
	private List<TreeItem> getAllTreeItems(TreeViewer treeViewer) {
		List<TreeItem> treeItems = new ArrayList<>();
		for (TreeItem item : treeViewer.getTree().getItems()) {
			treeItems.add(item);
		}

		int i = 0;
		while (i < treeItems.size()) {
			TreeItem parent = treeItems.get(i);
			for (TreeItem item : parent.getItems()) {
				treeItems.add(item);
			}
			i++;
		}
		return treeItems;
	}

	/**
	 * Hides the error decoration for the sensor assignment.
	 *
	 * @param treeViewer
	 *            tree viewer
	 * @param data
	 *            Data bounded to the tree item.
	 */
	public void hideTreeItemControlDecoration(TreeViewer treeViewer, Object data) {
		if (null == treeViewer) {
			return;
		}

		// remove if it's there
		for (TreeItemControlDecoration decoration : treeItemControlDecorations) {
			if (data == decoration.getData()) { // NOPMD == on purpose
				decoration.hide();
				return;
			}
		}
	}

	/**
	 * Class to help with displaying control decorations on the tree items.
	 *
	 * @author Alexander Wert
	 */
	public class TreeItemControlDecoration extends AbstractItemControlDecoration<TreeItem, TreeEditor> {

		/**
		 * Parent decoration to show message for the child error.
		 */
		private TreeItemControlDecoration parentControlDecoration;

		/**
		 * Valid state of this decoration.
		 */
		private boolean valid;

		/**
		 * Collapse listener on tree items.
		 */
		private final Listener collapseListener;

		/**
		 * Expand listener on tree items.
		 */
		private final Listener expandListener;

		/**
		 * Constructor.
		 *
		 * @param treeItem
		 *            TreeItem to create decoration for.
		 */
		public TreeItemControlDecoration(final TreeItem treeItem) {
			super(treeItem, treeItem.getParent());

			TreeEditor treeEditor = new TreeEditor(treeItem.getParent());
			treeEditor.horizontalAlignment = SWT.LEFT;
			treeEditor.verticalAlignment = SWT.BOTTOM;
			treeEditor.setEditor(getControl(), treeItem, 0);
			initItemEditor(treeEditor);

			// hide and dispose decoration on disposal of the corresponding tree item
			treeItem.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					// in any case hide, dispose and remove
					treeItemControlDecorations.remove(TreeItemControlDecoration.this);
					hide();
					dispose();
				}

			});

			//
			collapseListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					TreeItem parent = treeItem.getParentItem();
					while (null != parent) {
						if (ObjectUtils.equals(parent, event.item)) {
							if (isVisible()) {
								TreeItemControlDecoration.super.hide();
							}
							break;
						}
						parent = parent.getParentItem();
					}
					if (ObjectUtils.equals(treeItem, event.item)) {
						SafeExecutor.asyncExec(new Runnable() {
							@Override
							public void run() {
								update();
							}
						});
					}
				}
			};
			expandListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					TreeItem parent = treeItem.getParentItem();
					while (null != parent) {
						if (ObjectUtils.equals(parent, event.item)) {
							if (!valid) {
								TreeItemControlDecoration.super.show();
							}
							SafeExecutor.asyncExec(new Runnable() {
								@Override
								public void run() {
									update();
								}
							});
							break;
						}
						parent = parent.getParentItem();
					}
					if (ObjectUtils.equals(treeItem, event.item)) {
						SafeExecutor.asyncExec(new Runnable() {
							@Override
							public void run() {
								update();
							}
						});
					}
				}
			};
			treeItem.getParent().addListener(SWT.Collapse, collapseListener);
			treeItem.getParent().addListener(SWT.Expand, expandListener);

			TreeItem parent = treeItem.getParentItem();
			if (null != parent) {
				parentControlDecoration = new TreeItemControlDecoration(parent);
				parentControlDecoration.setDescriptionText("One of the child items has an error.");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void hide() {
			valid = true;
			try {
				super.hide();
			} catch (Exception exception) {
				// ignore exception on purpose
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Ignoring Exception on hiding TreeItemControlDecoration.");
				}
			}
			if (null != parentControlDecoration) {
				parentControlDecoration.hide();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void show() {
			valid = false;
			super.show();
			if (null != parentControlDecoration) {
				parentControlDecoration.show();
			}
		}

		/**
		 * Updates the decoration.
		 */
		public void updateDecoration() {
			update();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			getItem().getParent().removeListener(SWT.Collapse, collapseListener);
			getItem().getParent().removeListener(SWT.Expand, expandListener);
			super.dispose();
		}

	}
}
