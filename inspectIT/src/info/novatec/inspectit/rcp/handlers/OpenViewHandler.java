package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.root.RootEditorInput;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The open view handler which takes care of opening a view by retrieving the
 * {@link InputDefinition}.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenViewHandler extends AbstractHandler {

	/**
	 * The corresponding command id.
	 */
	public static final String COMMAND = "info.novatec.inspectit.rcp.commands.openView";

	/**
	 * The input definition id to look up.
	 */
	public static final String INPUT = COMMAND + ".input";

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the view
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		// Get the input definition out of the context
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		InputDefinition inputDefinition = (InputDefinition) context.getVariable(INPUT);

		// open the view if the input definition is set
		if (null != inputDefinition) {
			RootEditorInput input = new RootEditorInput(inputDefinition);
			try {
				page.openEditor(input, FormRootEditor.ID);
			} catch (PartInitException e) {
				throw new ExecutionException("Exception occurred trying to open the editor.", e);
			}
		}

		return null;
	}
}
