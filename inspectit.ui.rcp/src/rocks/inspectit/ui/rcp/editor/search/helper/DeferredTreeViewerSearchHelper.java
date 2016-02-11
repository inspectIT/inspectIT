package info.novatec.inspectit.rcp.editor.search.helper;

import info.novatec.inspectit.rcp.editor.tree.DeferredTreeViewer;
import info.novatec.inspectit.rcp.editor.tree.input.TreeInputController;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.TreeColumn;

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
	 *            {@link info.novatec.inspectit.rcp.editor.search.factory.SearchFactory}.
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
