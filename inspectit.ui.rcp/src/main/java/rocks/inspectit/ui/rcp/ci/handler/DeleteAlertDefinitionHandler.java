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

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.provider.IAlertDefinitionProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Handler that deletes {@link AlertingDefinition} instances.
 *
 * @author Alexander Wert
 *
 */
public class DeleteAlertDefinitionHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (selection.isEmpty()) {
			return null;
		}

		final int size = selection.size();
		MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
		confirmDelete.setText("Confirm Delete");
		confirmDelete.setMessage("Are you sure you want to delete the " + size + " selected alert definition" + ((size > 1) ? "s" : "") + "?");
		boolean confirmed = SWT.OK == confirmDelete.open();

		if (confirmed) {
			Job deleteAlertDefinitionJob = new Job("Delete Alert Definition(s) Job") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					List<Status> statuses = new ArrayList<>();
					for (Iterator<?> it = selection.iterator(); it.hasNext();) {
						Object selected = it.next();
						if (selected instanceof IAlertDefinitionProvider) {
							IAlertDefinitionProvider alertDefinitionProvider = (IAlertDefinitionProvider) selected;
							AlertingDefinition alertDef = alertDefinitionProvider.getAlertDefinition();
							CmrRepositoryDefinition repositoryDefinition = alertDefinitionProvider.getCmrRepositoryDefinition();

							try {
								if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									repositoryDefinition.getConfigurationInterfaceService().deleteAlertingDefinition(alertDef);
									InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionDeleted(alertDef, repositoryDefinition);
								} else {
									statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error deleting alert definition " + alertDef.getName() + " from the CMR. Repository is offline!"));
								}
							} catch (Exception e) {
								statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error deleting alert definition " + alertDef.getName() + " from the CMR.", e));
							}
						}
					}

					if (CollectionUtils.isNotEmpty(statuses)) {
						if (1 == statuses.size()) {
							return statuses.iterator().next();
						} else {
							return new MultiStatus(InspectIT.ID, IStatus.OK, statuses.toArray(new Status[statuses.size()]), "Delete of several alert definitions failed.", null);
						}
					} else {
						return Status.OK_STATUS;
					}
				}
			};
			deleteAlertDefinitionJob.setUser(true);
			deleteAlertDefinitionJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ALARM));
			deleteAlertDefinitionJob.schedule();
		}

		return null;
	}

}
