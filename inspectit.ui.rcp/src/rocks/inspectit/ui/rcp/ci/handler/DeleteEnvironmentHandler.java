package rocks.inspectit.ui.rcp.ci.handler;

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

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Handler for the environment deletion.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteEnvironmentHandler extends AbstractHandler implements IHandler {

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
		confirmDelete.setMessage("Are you sure you want to delete the " + size + " selected environment" + ((size > 1) ? "s" : "") + "?");
		boolean confirmed = SWT.OK == confirmDelete.open();

		if (confirmed) {
			Job deleteEnvironmentsJob = new Job("Delete Environment(s) Job") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					List<Status> statuses = new ArrayList<>();
					for (Iterator<?> it = selection.iterator(); it.hasNext();) {
						Object selected = it.next();
						if (selected instanceof IEnvironmentProvider) {
							CmrRepositoryDefinition repositoryDefinition = ((IEnvironmentProvider) selected).getCmrRepositoryDefinition();
							Environment environment = ((IEnvironmentProvider) selected).getEnvironment();

							try {
								if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									repositoryDefinition.getConfigurationInterfaceService().deleteEnvironment(environment);

									InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentDeleted(environment, repositoryDefinition);
								}
							} catch (BusinessException e) {
								statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error deleting environment " + environment.getName() + " from the CMR.", e));
							}
						}
					}

					if (CollectionUtils.isNotEmpty(statuses)) {
						if (1 == statuses.size()) {
							return statuses.iterator().next();
						} else {
							return new MultiStatus(InspectIT.ID, IStatus.OK, statuses.toArray(new Status[statuses.size()]), "Delete of several environments failed.", null);
						}
					} else {
						return Status.OK_STATUS;
					}
				}
			};
			deleteEnvironmentsJob.setUser(true);
			deleteEnvironmentsJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_BLOCK));
			deleteEnvironmentsJob.schedule();
		}

		return null;
	}
}
