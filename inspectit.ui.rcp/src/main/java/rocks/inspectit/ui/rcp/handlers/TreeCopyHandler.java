package rocks.inspectit.ui.rcp.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.editor.tree.TreeSubView;
import rocks.inspectit.ui.rcp.util.ClipboardUtil;

/**
 * The handler to execute a copy command on our tree sub views.
 *
 * @author Patrice Bouillet
 *
 */
public class TreeCopyHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		TreeSubView subView = (TreeSubView) rootEditor.getActiveSubView();

		List<Integer> visibleColumnOrder = subView.getColumnOrder();
		StringBuilder sb = new StringBuilder();

		// columns first
		List<String> columnNames = subView.getColumnNames();
		for (Integer index : visibleColumnOrder) {
			sb.append(columnNames.get(index.intValue()));
			sb.append('\t');
		}
		sb.append(System.getProperty("line.separator"));

		// then each object
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			List<String> columnValues = subView.getTreeInputController().getColumnValues(object);
			for (Integer index : visibleColumnOrder) {
				sb.append(columnValues.get(index.intValue()));
				sb.append('\t');
			}
			sb.append(System.getProperty("line.separator"));
		}

		ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), sb.toString());

		return null;
	}
}
