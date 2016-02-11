package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Tries to close the list of storages given through the storage leaf.
 * 
 * @author Ivan Senic
 * 
 */
public class CloseStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object firstElement = ((StructuredSelection) selection).getFirstElement();
			if (firstElement instanceof IStorageDataProvider) {
				StorageData storageData = ((IStorageDataProvider) firstElement).getStorageData();
				CmrRepositoryDefinition cmrRepositoryDefinition = ((IStorageDataProvider) firstElement).getCmrRepositoryDefinition();

				MessageBox confirmFinalization = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
				confirmFinalization.setText("Confirm Finalization");
				confirmFinalization
						.setMessage("Are you sure you want to finalize the selected storage? Writing will not be possible after finalization. Note that finalization process will wait for all ongoing writing tasks to be finished.");

				if (SWT.OK == confirmFinalization.open()) {
					FinalizeStorageJob finalizeStorageJob = new FinalizeStorageJob(storageData, cmrRepositoryDefinition);
					finalizeStorageJob.schedule();
				}
			}
		}
		return null;
	}

	/**
	 * Finalize storage job class. Starts the finalization and provides information about the amount
	 * of tasks left before finalization can be done.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	protected static class FinalizeStorageJob extends Job {

		/**
		 * Amount of milliseconds job will check for the amount of writing tasks left.
		 */
		private static final long TASKS_CHECK_SLEEP_TIME = 1000;

		/**
		 * Storage to finalize.
		 */
		private StorageData storageData;

		/**
		 * CMR where storage is located.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * 
		 * @param storageData
		 *            Storage to finalize.
		 * @param cmrRepositoryDefinition
		 *            CMR where storage is located.
		 */
		public FinalizeStorageJob(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
			super("Finalizing storage " + storageData);
			this.storageData = storageData;
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			setUser(true);
			setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_FINALIZE));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// cancel if CMR is not online
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
				return Status.CANCEL_STATUS;
			}

			// get the number of tasks
			int totalTasks = (int) cmrRepositoryDefinition.getStorageService().getStorageQueuedWriteTaskCount(storageData);

			String taskName;
			if (totalTasks > 0) {
				taskName = "Waiting for " + totalTasks + " writing tasks to finish and finalizing storage '" + storageData.getName() + "'";
			} else {
				taskName = "Finalizing storage '" + storageData + "'";
			}

			monitor.beginTask(taskName, totalTasks + 1);

			Job executeFinalization = new Job("Execute finalization") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						cmrRepositoryDefinition.getStorageService().closeStorage(storageData);
						return Status.OK_STATUS;
					} catch (final BusinessException e) {
						return new Status(IStatus.ERROR, InspectIT.ID, "Selected storage " + storageData + " could not be finalized.", e);
					}
				}
			};
			executeFinalization.setUser(false);
			executeFinalization.schedule();

			// regulate the processed task count
			while (executeFinalization.getState() != Job.NONE) {
				try {
					Thread.sleep(TASKS_CHECK_SLEEP_TIME);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				if (executeFinalization.getState() == Job.NONE) {
					monitor.worked(totalTasks);
				} else {
					int newLeftTasks = (int) cmrRepositoryDefinition.getStorageService().getStorageQueuedWriteTaskCount(storageData);
					monitor.worked(totalTasks - newLeftTasks);
					totalTasks = newLeftTasks;
				}
			}

			// add one task for finalization
			monitor.worked(1);
			monitor.done();

			// refresh the storage manager
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
					if (viewPart instanceof StorageManagerView) {
						((StorageManagerView) viewPart).refresh(cmrRepositoryDefinition);
					}
				}
			});

			return Status.OK_STATUS;
		}
	}

}
