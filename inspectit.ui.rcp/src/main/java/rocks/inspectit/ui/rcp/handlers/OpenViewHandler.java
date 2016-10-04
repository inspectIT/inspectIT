package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.root.FormRootEditor;
import rocks.inspectit.ui.rcp.editor.root.RootEditorInput;

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
	public static final String COMMAND = "rocks.inspectit.ui.rcp.commands.openView";

	/**
	 * The input definition id to look up.
	 */
	public static final String INPUT = COMMAND + ".input";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the view
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		// Get the input definition out of the context
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		InputDefinition inputDefinition = (InputDefinition) context.getVariable(INPUT);

		// open the view if the input definition is set
		if (null != inputDefinition) {
			if (inputDefinition.getIdDefinition().getAlertId() != null) {
				InputDialog dialog = new InputDialog(InspectIT.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), "Alert Information", "Enter identifier of the Alert of interest:", "",
						null);
				if (dialog.open() == Window.OK) {
					String alertId = dialog.getValue();
					inputDefinition.getIdDefinition().setAlertId(alertId);
					inputDefinition.getEditorPropertiesData().setViewName(alertId);
				}
			}

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
