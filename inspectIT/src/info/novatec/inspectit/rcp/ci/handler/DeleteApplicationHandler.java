package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.model.ci.ApplicationLeaf;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Delete the profile handler.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteApplicationHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (selection.isEmpty()) {
			return null;
		}

		final int size = selection.size();
		MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
		confirmDelete.setText("Confirm Delete");
		confirmDelete.setMessage("Are you sure you want to delete the " + size + " selected application" + ((size > 1) ? "s" : "") + "?");
		boolean confirmed = SWT.OK == confirmDelete.open();

		if (confirmed) {
			Job deleteApplicationsJob = new Job("Delete Application(s) Job") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					List<Status> statuses = new ArrayList<>();
					for (Iterator<?> it = selection.iterator(); it.hasNext();) {
						Object selected = it.next();
						if (selected instanceof ApplicationLeaf) {
							ApplicationLeaf applicationLeaf = (ApplicationLeaf) selected;
							IApplicationDefinition application = applicationLeaf.getApplication();
							CmrRepositoryDefinition repositoryDefinition = applicationLeaf.getCmrRepositoryDefinition();

							if (null == repositoryDefinition) {
								statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error deleting application " + application.getApplicationName() + " from the CMR."));
							} else {

								try {
									if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
										repositoryDefinition.getBusinessContextMangementService().deleteApplicationDefinition(application);
										InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationDeleted(application, repositoryDefinition);

									}
								} catch (Exception e) {
									statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error deleting application " + application.getApplicationName() + " from the CMR.", e));
								}
							}
						}
					}

					if (CollectionUtils.isNotEmpty(statuses)) {
						if (1 == statuses.size()) {
							return statuses.iterator().next();
						} else {
							return new MultiStatus(InspectIT.ID, IStatus.OK, statuses.toArray(new Status[statuses.size()]), "Delete of several applications failed.", null);
						}
					} else {
						return Status.OK_STATUS;
					}

				}
			};
			deleteApplicationsJob.setUser(true);
			deleteApplicationsJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADDRESSBOOK));
			deleteApplicationsJob.schedule();
		}

		return null;
	}

}
