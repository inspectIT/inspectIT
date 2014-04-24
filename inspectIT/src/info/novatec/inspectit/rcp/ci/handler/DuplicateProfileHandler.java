package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.ci.wizard.CreateProfileWizard;
import info.novatec.inspectit.rcp.provider.IProfileProvider;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the duplicate profile.
 * 
 * @author Ivan Senic
 * 
 */
public class DuplicateProfileHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (selection.isEmpty()) {
			return null;
		}

		Object selected = selection.getFirstElement();
		if (selected instanceof IProfileProvider) {
			Profile profile = ((IProfileProvider) selected).getProfile();

			CreateProfileWizard createProfileWizard = new CreateProfileWizard(profile);
			createProfileWizard.init(HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench(), selection);
			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), createProfileWizard);
			wizardDialog.open();
		}

		return null;
	}
}
