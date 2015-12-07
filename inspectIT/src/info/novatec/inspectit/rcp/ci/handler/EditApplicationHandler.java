package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.provider.IApplicationProvider;
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
 * Handler for editing application.
 *
 * @author Alexander Wert
 *
 */
public class EditApplicationHandler extends AbstractHandler implements IHandler {

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
		if (selected instanceof IApplicationProvider) {
			IApplicationProvider applicationProvider = (IApplicationProvider) selected;
			ApplicationDefinition applicationDefinition = applicationProvider.getApplication();
			CmrRepositoryDefinition repositoryDefinition = applicationProvider.getCmrRepositoryDefinition();
			String[] existingApplicationNames = new String[applicationProvider.getParentList().size()];
			int i = 0;
			for (IApplicationProvider appProvider : applicationProvider.getParentList()) {
				existingApplicationNames[i] = appProvider.getApplication().getApplicationName();
				i++;
			}
			EditNameDescriptionDialog dialog = new EditNameDescriptionDialog(HandlerUtil.getActiveShell(event), applicationDefinition.getApplicationName(), applicationDefinition.getDescription(),
					"Edit Application", "Enter new application name and/or description", existingApplicationNames);
			if (Dialog.OK == dialog.open()) {
				applicationDefinition.setApplicationName(dialog.getName());
				if (StringUtils.isNotBlank(dialog.getDescription())) {
					applicationDefinition.setDescription(dialog.getDescription());
				} else {
					applicationDefinition.setDescription("");
				}

				try {
					applicationDefinition = repositoryDefinition.getConfigurationInterfaceService().updateApplicationDefinition(applicationDefinition);

					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationUpdated(applicationDefinition, repositoryDefinition);
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the application state failed.", e);
				}
			}
		}

		return null;
	}

}
