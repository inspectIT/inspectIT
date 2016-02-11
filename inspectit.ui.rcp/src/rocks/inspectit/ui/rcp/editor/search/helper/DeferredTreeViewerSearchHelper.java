package rocks.inspectit.ui.rcp.editor.search.helper;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.TreeColumn;

import rocks.inspectit.ui.rcp.editor.tree.DeferredTreeViewer;
import rocks.inspectit.ui.rcp.editor.tree.input.TreeInputController;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Search helper for {@link DeferredTreeViewer}.
 * 
 * @author Ivan Senic
 * @see AbstractSearchHelper
 * 
 */
public class DeferredTreeViewerSearchHelper extends AbstractSearchHelper {

	/**
	 * {@link DeferredTreeViewer}.
	 */
	private final DeferredTreeViewer treeViewer;

	/**
	 * {@link TreeInputController}.
	 */
	private final TreeInputController treeInputController;

	/**
	 * Default constructor.
	 * 
	 * @param treeViewer
	 *            {@link DeferredTreeViewer}.
	 * @param treeInputController
	 *            {@link TreeInputController}.
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}. Needed for
	 *            {@link rocks.inspectit.ui.rcp.editor.search.factory.SearchFactory}.
	 */
	public DeferredTreeViewerSearchHelper(DeferredTreeViewer treeViewer, TreeInputController treeInputController, RepositoryDefinition repositoryDefinition) {
		super(repositoryDefinition);
		this.treeViewer = treeViewer;
		this.treeInputController = treeInputController;
		for (TreeColumn treeColumn : treeViewer.getTree().getColumns()) {
			treeColumn.addSelectionListener(getColumnSortingListener());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectElement(Object element) {
		treeViewer.expandToObjectAndSelect(element, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getAllObjects() {
		return treeInputController.getObjectsToSearch(treeViewer.getInput());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StructuredViewer getViewer() {
		return treeViewer;
	}

}
