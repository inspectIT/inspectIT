package rocks.inspectit.ui.rcp.handlers;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.editor.tree.TreeSubView;

/**
 * Collapse handler for trees.
 * 
 * @author Ivan Senic
 * 
 */
public class TreeCollapseHandler extends AbstractHandler implements IHandler {

	/**
	 * Parameter that defines if collapse is performed on all elements or just the selected ones.
	 */
	public static final String IS_COLLAPSE_ALL_PARAMETER = "rocks.inspectit.ui.rcp.commands.collapse.isCollapseAll";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(IS_COLLAPSE_ALL_PARAMETER);
		if (StringUtils.isNotEmpty(param)) {
			boolean isCollapseAll = Boolean.parseBoolean(param);
			AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
			TreeSubView treeSubView = (TreeSubView) rootEditor.getActiveSubView();
			if (isCollapseAll) {
				treeSubView.getTreeViewer().collapseAll();
			} else {
				IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					treeSubView.getTreeViewer().collapseToLevel(object, TreeViewer.ALL_LEVELS);
				}
			}
		}
		return null;
	}
}
