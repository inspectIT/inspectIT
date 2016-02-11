package rocks.inspectit.ui.rcp.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.model.AgentLeaf;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Handler for deleting the agent from the CMR.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteAgentHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		boolean confirmed = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Confirm Delete",
				"Are you sure you want to permanently delete the selected Agent(s)? Note that all monitoring data related to the Agent(s) will be deleted from the repository database.");
		if (confirmed) {
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object selected = (Object) it.next();
				if (selected instanceof AgentLeaf) {
					AgentLeaf agentLeaf = (AgentLeaf) selected;
					PlatformIdent platformIdent = agentLeaf.getPlatformIdent();
					CmrRepositoryDefinition cmrRepositoryDefinition = agentLeaf.getCmrRepositoryDefinition();

					try {
						cmrRepositoryDefinition.getGlobalDataAccessService().deleteAgent(platformIdent.getId());
						InspectIT.getDefault().getCmrRepositoryManager().repositoryAgentDeleted(cmrRepositoryDefinition, platformIdent);
					} catch (BusinessException e) {
						throw new ExecutionException("Exception occurred trying to delete the Agent from the CMR.", e);
					}
				}
			}

		}
		return null;
	}
}
