package rocks.inspectit.ui.rcp.handlers;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.editor.tree.TreeSubView;

/**
 * Expand handler for trees.
 *
 * @author Ivan Senic
 *
 */
public class TreeExpandHandler extends AbstractHandler implements IHandler {

	/**
	 * Parameter that defines if expand is performed on all elements or just the selected ones.
	 */
	public static final String IS_EXPAND_ALL_PARAMETER = "rocks.inspectit.ui.rcp.commands.expand.isExpandAll";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(IS_EXPAND_ALL_PARAMETER);
		if (StringUtils.isNotEmpty(param)) {
			boolean isExpandAll = Boolean.parseBoolean(param);
			AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
			TreeSubView treeSubView = (TreeSubView) rootEditor.getActiveSubView();
			if (isExpandAll) {
				treeSubView.getTreeViewer().expandAll();
			} else {
				IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					treeSubView.getTreeViewer().expandToLevel(object, AbstractTreeViewer.ALL_LEVELS);
				}
			}
		}
		return null;
	}

}
