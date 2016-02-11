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
 * Handler for making profiles default or not.
 * 
 * @author Ivan Senic
 * 
 */
public class SetDefaultProfileHandler extends AbstractHandler implements IHandler {

	/**
	 * Active parameter. If profile should be activated or deactivated.
	 */
	public static final String DEFAULT_PARAM = "rocks.inspectit.ui.rcp.ci.setDefaultProfile.default";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(DEFAULT_PARAM);
		if (StringUtils.isEmpty(param)) {
			return null;
		}
		Boolean isDefault = Boolean.parseBoolean(param);

		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		Object selected = selection.getFirstElement();
		if (selected instanceof IProfileProvider) {
			IProfileProvider profileProvider = (IProfileProvider) selected;
			Profile profile = profileProvider.getProfile();
			CmrRepositoryDefinition repositoryDefinition = profileProvider.getCmrRepositoryDefinition();
			if (profile.isDefaultProfile() != isDefault.booleanValue()) {
				profile.setDefaultProfile(isDefault.booleanValue());
				try {
					profile = repositoryDefinition.getConfigurationInterfaceService().updateProfile(profile);

					// notify listeners
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileUpdated(profile, repositoryDefinition, true);
				} catch (final BusinessException e) {
					throw new ExecutionException("Update of the profile default state failed.", e);
				}
			}
		}

		return null;
	}

}
