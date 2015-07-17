package info.novatec.inspectit.rcp.view.listener;

import info.novatec.inspectit.rcp.handlers.OpenViewHandler;
import info.novatec.inspectit.rcp.model.Component;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Double click listener for the explorers trees.
 * 
 * @author Ivan Senic
 * 
 */
public class TreeViewDoubleClickListener implements IDoubleClickListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		TreeSelection selection = (TreeSelection) event.getSelection();
		Object element = selection.getFirstElement();
		if (null != element) {
			if (((Component) element).getInputDefinition() == null) {
				TreeViewer treeViewer = (TreeViewer) event.getViewer();
				TreePath path = selection.getPaths()[0];
				if (null != path) {
					boolean expanded = treeViewer.getExpandedState(path);
					if (expanded) {
						treeViewer.collapseToLevel(path, 1);
					} else {
						treeViewer.expandToLevel(path, 1);
					}
				}
			} else {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(OpenViewHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
				context.addVariable(OpenViewHandler.INPUT, ((Component) element).getInputDefinition());

				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
