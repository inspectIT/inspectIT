package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.provider.IEnvironmentProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the environment edit.
 * 
 * @author Ivan Senic
 * 
 */
public class EditEnvironmentHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		Object selected = selection.getFirstElement();
		if (selected instanceof IEnvironmentProvider) {
			IEnvironmentProvider environmentProvider = (IEnvironmentProvider) selected;
			Environment environment = environmentProvider.getEnvironment();
			CmrRepositoryDefinition repositoryDefinition = environmentProvider.getCmrRepositoryDefinition();

			EditNameDescriptionDialog dialog = new EditNameDescriptionDialog(HandlerUtil.getActiveShell(event), environment.getName(), environment.getDescription(), "Edit Environment",
					"Enter new environment name and/or description");
			if (Dialog.OK == dialog.open()) {
				environment.setName(dialog.getName());
				if (StringUtils.isNotBlank(dialog.getDescription())) {
					environment.setDescription(dialog.getDescription());
				}

				try {
					Environment updated = repositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);

					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentUpdated(updated, repositoryDefinition);
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the environment state failed.", e);
				}
			}
		}

		return null;
	}

}
