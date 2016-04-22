package rocks.inspectit.ui.rcp.ci.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.dialog.EditNameDescriptionDialog;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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
			if (Window.OK == dialog.open()) {
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
