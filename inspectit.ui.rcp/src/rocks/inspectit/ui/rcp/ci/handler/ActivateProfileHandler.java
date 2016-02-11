package rocks.inspectit.ui.rcp.ci.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Handler for activating and deactivating profile.
 * 
 * @author Ivan Senic
 * 
 */
public class ActivateProfileHandler extends AbstractHandler implements IHandler {

	/**
	 * Active parameter. If profile should be activated or deactivated.
	 */
	public static final String ACTIVE_PARAM = "rocks.inspectit.ui.rcp.ci.activateProfile.active";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(ACTIVE_PARAM);
		if (StringUtils.isEmpty(param)) {
			return null;
		}
		Boolean active = Boolean.parseBoolean(param);

		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		Object selected = selection.getFirstElement();
		if (selected instanceof IProfileProvider) {
			IProfileProvider profileProvider = (IProfileProvider) selected;
			Profile profile = profileProvider.getProfile();
			CmrRepositoryDefinition repositoryDefinition = profileProvider.getCmrRepositoryDefinition();
			if (profile.isActive() != active.booleanValue()) {
				profile.setActive(active.booleanValue());
				try {
					profile = repositoryDefinition.getConfigurationInterfaceService().updateProfile(profile);

					// notify listeners
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileUpdated(profile, repositoryDefinition, true);
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the profile active state failed.", e);
				}
			}
		}

		return null;
	}

}
