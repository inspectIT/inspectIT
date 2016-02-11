package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.CopyBufferToStorageWizard;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Copy buffer to storage handler.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyBufferToStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			CmrRepositoryDefinition suggestedCmrRepositoryDefinition = null;
			Collection<PlatformIdent> autoSelectedAgents = Collections.emptyList();
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				suggestedCmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
			} else if (selectedObject instanceof ICmrRepositoryAndAgentProvider) {
				suggestedCmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) selectedObject).getCmrRepositoryDefinition();
				autoSelectedAgents = Collections.singletonList(((ICmrRepositoryAndAgentProvider) selectedObject).getPlatformIdent());
			}
			if (null != suggestedCmrRepositoryDefinition) {
				// check if the writing state is OK
				try {
					CmrStatusData cmrStatusData = suggestedCmrRepositoryDefinition.getCmrManagementService().getCmrStatusData();
					if (cmrStatusData.isWarnSpaceLeftActive()) {
						String leftSpace = NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageDataSpaceLeft());
						if (!MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), "Confirm", "For selected CMR there is an active warning about insufficient storage space left. Only "
								+ leftSpace + " are left on the target server, are you sure you want to continue?")) {
							return null;
						}
					}
				} catch (Exception e) { // NOPMD NOCHK
					// ignore because if we can not get the info. we will still respond to user
					// action
				}

				CopyBufferToStorageWizard wizard = new CopyBufferToStorageWizard(suggestedCmrRepositoryDefinition, autoSelectedAgents);
				WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
				dialog.open();
			}
		}

		return null;
	}

}
