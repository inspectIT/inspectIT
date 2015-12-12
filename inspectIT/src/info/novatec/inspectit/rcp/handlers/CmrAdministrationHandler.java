package info.novatec.inspectit.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.CmrAdministrationWizard;

/**
 * Handler for managing the users on the CMR.
 * 
 * @author Lucca Hellriegel
 *
 */

public class CmrAdministrationHandler extends AbstractHandler implements IHandler { 
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
			} else if (selectedObject instanceof ICmrRepositoryAndAgentProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) selectedObject).getCmrRepositoryDefinition();
			}
		}

		if (null != cmrRepositoryDefinition) {
			CmrAdministrationWizard cmrAdministrationWizard = new CmrAdministrationWizard(cmrRepositoryDefinition);
			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), cmrAdministrationWizard);
			wizardDialog.open();
		} else {
			throw new ExecutionException("No CMR");
		}

		return null;
	}

}
