package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

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
	public static final String DEFAULT_PARAM = "info.novatec.inspectit.rcp.ci.setDefaultProfile.default";

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
				} catch (final Exception e) {
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							InspectIT.getDefault().createErrorDialog("Update of the profile default state failed.", e, -1);
						}
					});
					return null;
				}

				// notify listeners
				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileEdited(profile, true);
			}
		}

		return null;
	}

}
