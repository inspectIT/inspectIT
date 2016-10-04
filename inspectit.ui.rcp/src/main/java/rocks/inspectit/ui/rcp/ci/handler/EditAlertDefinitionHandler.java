package rocks.inspectit.ui.rcp.ci.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.ci.wizard.AlertDefinitionWizard;
import rocks.inspectit.ui.rcp.provider.IAlertDefinitionProvider;

/**
 * Handler that opens the AlertDefinitionWizard for editing.
 *
 * @author Alexander Wert
 *
 */
public class EditAlertDefinitionHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		openEditWizard(selection, HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench(), HandlerUtil.getActiveShell(event));

		return null;
	}

	/**
	 * Opens {@link AlertDefinitionWizard} for the passed selection of {@link AlertingDefinition}.
	 * 
	 * @param selection
	 *            Selection containing an {@link AlertingDefinition} instance. If the first element
	 *            in the selection is not an instance of {@link AlertingDefinition}, then this
	 *            method does nothing.
	 * @param workbench
	 *            Active workbench.
	 * @param shell
	 *            Active shell.
	 */
	public static void openEditWizard(StructuredSelection selection, IWorkbench workbench, Shell shell) {
		Object selected = selection.getFirstElement();
		if (selected instanceof IAlertDefinitionProvider) {
			IAlertDefinitionProvider alertDefinitionProvider = (IAlertDefinitionProvider) selected;
			AlertingDefinition alertDefinition = alertDefinitionProvider.getAlertDefinition();

			AlertDefinitionWizard wizard = new AlertDefinitionWizard(alertDefinition);
			wizard.init(workbench, selection);
			WizardDialog wizardDialog = new WizardDialog(shell, wizard);
			wizardDialog.open();
		}
	}

}
