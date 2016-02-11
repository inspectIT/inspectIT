package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.StorageCompressionWizardPage;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Wizard for downloading storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DownloadStorageWizard extends Wizard implements INewWizard {

	/**
	 * List of storages to be downloaded provided by {@link IStorageDataProvider}s.
	 */
	private Collection<IStorageDataProvider> storageDataProviders;

	/**
	 * Wizard page.
	 */
	private StorageCompressionWizardPage storageCompressionWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param storageDataProviders
	 *            List of storages to be downloaded provided by {@link IStorageDataProvider}s.
	 */
	public DownloadStorageWizard(Collection<IStorageDataProvider> storageDataProviders) {
		Assert.isTrue(CollectionUtils.isNotEmpty(storageDataProviders));
		this.storageDataProviders = storageDataProviders;
		if (storageDataProviders.size() == 1) {
			this.setWindowTitle("Download Storage");
		} else {
			this.setWindowTitle("Download " + storageDataProviders.size() + " Storages");
		}
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_DOWNLOAD));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		String title = getWindowTitle();
		long totalSize = 0;
		for (IStorageDataProvider storageDataProvider : storageDataProviders) {
			totalSize += storageDataProvider.getStorageData().getDiskSize();
		}
		StringBuilder message = new StringBuilder("Options for downloading ");
		if (storageDataProviders.size() == 1) {
			StorageData storageData = storageDataProviders.iterator().next().getStorageData();
			message.append("the storage '");
			message.append(storageData.getName());
			message.append("' (size: ");
			message.append(NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()));
			message.append(')');
		} else {
			message.append(storageDataProviders.size());
			message.append(" storages (total size: ");
			message.append(NumberFormatter.formatBytesToMBytes(totalSize));
			message.append(')');
		}
		storageCompressionWizardPage = new StorageCompressionWizardPage(title, message.toString());
		addPage(storageCompressionWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final boolean compress = storageCompressionWizardPage.isCompressBefore();
		new DownloadStorageJob(storageDataProviders, compress).schedule();
		return true;
	}

	/**
	 * A job for downloading a one or more storages. If an exception is caught in the Job, the Job
	 * will exit with Warnings status and provide a Throwable as the reason for not succeeding.
	 * Remaining storages will still be downloaded.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class DownloadStorageJob extends Job {

		/**
		 * Should download be compressed.
		 */
		private boolean compress;

		/**
		 * Collection of storages to download.
		 */
		private Collection<IStorageDataProvider> storageDataProviders;

		/**
		 * Default constructor.
		 * 
		 * @param storageDataProviders
		 *            Collection of storages to download.
		 * @param compress
		 *            Should download be compressed.
		 */
		public DownloadStorageJob(Collection<IStorageDataProvider> storageDataProviders, boolean compress) {
			super("Download Storages");
			this.compress = compress;
			this.storageDataProviders = storageDataProviders;
			setUser(true);
			setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_DOWNLOADED));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor subMonitor = SubMonitor.convert(monitor);
			List<Status> connectedStatuses = new ArrayList<>();

			// calculate how much work we have based on storage sizes
			int totalSize = 0;
			for (IStorageDataProvider storageDataProvider : storageDataProviders) {
				totalSize += (int) (storageDataProvider.getStorageData().getDiskSize() / 1000);
			}
			subMonitor.setWorkRemaining(totalSize);

			for (IStorageDataProvider storageDataProvider : storageDataProviders) {
				StorageData storageData = storageDataProvider.getStorageData();
				CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();

				if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
					connectedStatuses.add(new Status(IStatus.WARNING, InspectIT.ID, "Storage '" + storageData.getName() + "'can not be downloaded because the CMR it is located on is offline."));
					continue;
				}

				try {
					InspectIT.getDefault().getInspectITStorageManager()
							.fullyDownloadStorage(storageData, cmrRepositoryDefinition, compress, subMonitor.newChild((int) (storageData.getDiskSize() / 1000)));
				} catch (BusinessException | SerializationException | IOException e) {
					connectedStatuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Storage '" + storageData.getName() + "'was not downloaded due to the exception", e));
					continue;
				}
			}

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IViewPart storageManagerView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
					if (storageManagerView instanceof StorageManagerView) {
						((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
					}
				}
			});
			monitor.done();
			if (CollectionUtils.isNotEmpty(connectedStatuses)) {
				if (1 == connectedStatuses.size()) {
					return connectedStatuses.iterator().next();
				} else {
					return new MultiStatus(InspectIT.ID, IStatus.OK, connectedStatuses.toArray(new Status[connectedStatuses.size()]), "Download of several storages failed.", null);
				}
			} else {
				return Status.OK_STATUS;
			}
		}

	}

}
