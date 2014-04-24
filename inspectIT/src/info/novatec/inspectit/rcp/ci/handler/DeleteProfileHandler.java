package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Delete the profile handler.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteProfileHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (selection.isEmpty()) {
			return null;
		}

		final int size = selection.size();
		MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
		confirmDelete.setText("Confirm Delete");
		confirmDelete.setMessage("Are you sure you want to delete " + size + " the selected profile" + ((size > 1) ? "s" : "") + "?");
		boolean confirmed = SWT.OK == confirmDelete.open();

		if (confirmed) {
			final List<String> ids = new ArrayList<>(size);
			boolean failed = false;
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object selected = it.next();
				if (selected instanceof IProfileProvider) {
					CmrRepositoryDefinition repositoryDefinition = ((IProfileProvider) selected).getCmrRepositoryDefinition();
					Profile profile = ((IProfileProvider) selected).getProfile();

					try {
						if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
							repositoryDefinition.getConfigurationInterfaceService().deleteProfile(profile);
							ids.add(profile.getId());

							// notify listeners
							InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileDeleted(profile);
						}
					} catch (BusinessException e) {
						failed = true;
						InspectIT.getDefault().log(IStatus.WARNING, "Error deleting profile(s) from the CMR", e);
					}
				}

			}

			if (failed) {
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createErrorDialog("Deletion of one " + ((size > 1) ? " or more profiles" : " profile") + " failed.", -1);
					}
				});
			}
		}

		return null;
	}

}
