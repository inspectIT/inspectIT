package info.novatec.inspectit.rcp.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import info.novatec.inspectit.rcp.wizard.CmrLoginWizard;

/**
 * Handler for logging into a CMR.
 * 
 * @author Clemens Geibel
 *
 */

public class CmrLoginHandler extends AbstractHandler implements IHandler {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		CmrLoginWizard cmrLoginWizard = new CmrLoginWizard();
		WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), cmrLoginWizard);
		wizardDialog.open();
		
		return null;
	}

}
