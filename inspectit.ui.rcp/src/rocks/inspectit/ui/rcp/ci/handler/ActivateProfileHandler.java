package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

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
	public static final String ACTIVE_PARAM = "info.novatec.inspectit.rcp.ci.activateProfile.active";

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
