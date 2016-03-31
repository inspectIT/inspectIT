package info.novatec.inspectit.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.LoginStatus;

/**
 * Handler for CMR Logout.
 * 
 * @author Clemens Geibel
 *
 */

public class CmrLogoutHandler extends AbstractHandler implements IHandler {

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
			cmrRepositoryDefinition.refreshLoginStatus();
			if (LoginStatus.LOGGEDIN == cmrRepositoryDefinition.getLoginStatus()) {
				cmrRepositoryDefinition.logout();
				
				//Delete stored login data
				PreferencesUtils.saveStringValue(cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "EMAIL", "", false);
				PreferencesUtils.saveStringValue(cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "PW", "", false);

				MessageDialog.openError(null, "Warning", "You logged out successfully.");
			} else {
				MessageDialog.openError(null, "Logout failed", "You are not logged in.");
			}
		} else {
			throw new ExecutionException("No CMR");
		}

		return null;
	}

}
