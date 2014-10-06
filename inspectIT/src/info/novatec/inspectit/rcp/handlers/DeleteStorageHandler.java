package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Handler for deleting a Storage. Handler will inform the user about the users who have mounted the
 * storage that is about to be deleted.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Iterator<?> it = ((StructuredSelection) selection).iterator();
			final List<IStorageDataProvider> storagesToDelete = new ArrayList<IStorageDataProvider>();
			Set<AbstractStorageLabel<String>> exploredBySet = new HashSet<AbstractStorageLabel<String>>();
			boolean confirmed = false;
			while (it.hasNext()) {
				Object nextObject = it.next();
				if (nextObject instanceof IStorageDataProvider) {
					storagesToDelete.add((IStorageDataProvider) nextObject);
					exploredBySet.addAll(((IStorageDataProvider) nextObject).getStorageData().getLabels(new ExploredByLabelType()));
				}
			}

			if (!storagesToDelete.isEmpty()) {
				StringBuffer confirmText = new StringBuffer(90);
				final boolean plural = storagesToDelete.size() > 1;
				if (!plural) {
					confirmText.append("Are you sure you want to delete the selected storage? ");
					if (!exploredBySet.isEmpty()) {
						confirmText.append("Note that the storage was (and still could be) explored by following users: ");
					}
				} else {
					confirmText.append("Are you sure you want to  delete the " + storagesToDelete.size() + " selected storages? ");
					if (!exploredBySet.isEmpty()) {
						confirmText.append("Note that the storages were (and still could be) explored by following users: ");
					}
				}
				if (!exploredBySet.isEmpty()) {
					for (AbstractStorageLabel<String> exploredByLabel : exploredBySet) {
						confirmText.append("\n * ");
						confirmText.append(exploredByLabel.getValue());
					}
				}

				MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
				confirmDelete.setText("Confirm Delete");
				confirmDelete.setMessage(confirmText.toString());
				confirmed = SWT.OK == confirmDelete.open();

				if (confirmed) {
					Job deleteStorageJob = new Job("Delete Storage Job") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							List<Status> statuses = new ArrayList<>();

							final Set<CmrRepositoryDefinition> involvedCmrSet = new HashSet<CmrRepositoryDefinition>();
							for (final IStorageDataProvider storageDataProvider : storagesToDelete) {
								if (storageDataProvider.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
									involvedCmrSet.add(storageDataProvider.getCmrRepositoryDefinition());
									try {
										storageDataProvider.getCmrRepositoryDefinition().getStorageService().deleteStorage(storageDataProvider.getStorageData());
										InspectIT.getDefault().getInspectITStorageManager().storageRemotelyDeleted(storageDataProvider.getStorageData());
									} catch (final BusinessException e) {
										String name = storageDataProvider.getStorageData().getName();
										statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Storage '" + name + "' could not be successfully deleted from CMR.", e));
									} catch (final SerializationException | IOException e) {
										String name = storageDataProvider.getStorageData().getName();
										statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Local data for storage '" + name + "' was not cleared successfully.", e));
									}
								} else {
									String name = storageDataProvider.getStorageData().getName();
									statuses.add(new Status(IStatus.WARNING, InspectIT.ID, "Storage '" + name + "' can not be deleted, because CMR where it is located is offline."));
								}
							}

							if (CollectionUtils.isNotEmpty(statuses)) {
								if (1 == statuses.size()) {
									return statuses.iterator().next();
								} else {
									return new MultiStatus(InspectIT.ID, IStatus.OK, statuses.toArray(new Status[statuses.size()]), "Delete of several storages failed.", null);
								}
							} else {
								return Status.OK_STATUS;
							}
						}
					};
					deleteStorageJob.setUser(true);
					deleteStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE));
					deleteStorageJob.schedule();
				}
			}
		}
		return null;
	}

}
