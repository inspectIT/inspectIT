package rocks.inspectit.ui.rcp.ci.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.view.BusinessContextManagerViewPart;
import rocks.inspectit.ui.rcp.dialog.EditNameDescriptionDialog;
import rocks.inspectit.ui.rcp.provider.IApplicationProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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

					// set selection to the edited element after editing
					IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
					if (part instanceof BusinessContextManagerViewPart) {
						((BusinessContextManagerViewPart) part).selectApplicationDefinition(applicationDefinition, repositoryDefinition);
					}
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the application state failed.", e);
				}
			}
		}

		return null;
	}

}
