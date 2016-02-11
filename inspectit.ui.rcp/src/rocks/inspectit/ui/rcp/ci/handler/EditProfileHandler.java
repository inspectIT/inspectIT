package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
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
 * Handler for editing profile.
 * 
 * @author Ivan Senic
 * 
 */
public class EditProfileHandler extends AbstractHandler implements IHandler {

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
			IProfileProvider profileProvider = (IProfileProvider) selected;
			Profile profile = profileProvider.getProfile();
			CmrRepositoryDefinition repositoryDefinition = profileProvider.getCmrRepositoryDefinition();

			EditNameDescriptionDialog dialog = new EditNameDescriptionDialog(HandlerUtil.getActiveShell(event), profile.getName(), profile.getDescription(), "Edit Profile",
					"Enter new profile name and/or description");
			if (Dialog.OK == dialog.open()) {
				profile.setName(dialog.getName());
				if (StringUtils.isNotBlank(dialog.getDescription())) {
					profile.setDescription(dialog.getDescription());
				}

				try {
					Profile updated = repositoryDefinition.getConfigurationInterfaceService().updateProfile(profile);

					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileUpdated(updated, repositoryDefinition, true);
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the profile state failed.", e);
				}
			}
		}

		return null;
	}

}
