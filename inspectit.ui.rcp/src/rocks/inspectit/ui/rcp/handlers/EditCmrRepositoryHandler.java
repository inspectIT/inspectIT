package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.view.impl.RepositoryManagerView;
import rocks.inspectit.ui.rcp.wizard.EditCmrRepositoryWizard;

/**
 * Rename the CMR name and description handler.
 * 
 * @author Ivan Senic
 * 
 */
public class EditCmrRepositoryHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		Object selectedElement = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();
		if (selectedElement instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedElement).getCmrRepositoryDefinition();
		} else {
			return null;
		}

		EditCmrRepositoryWizard editWizard = new EditCmrRepositoryWizard(cmrRepositoryDefinition);
		WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), editWizard);
		if (WizardDialog.OK == wizardDialog.open()) {
			// update view if we have OK from the wizard
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(RepositoryManagerView.VIEW_ID);
			if (viewPart instanceof RepositoryManagerView) {
				((RepositoryManagerView) viewPart).refresh();
			}
		}

		return null;
	}
}
