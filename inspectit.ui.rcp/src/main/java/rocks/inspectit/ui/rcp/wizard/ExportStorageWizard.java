package rocks.inspectit.ui.rcp.wizard;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.cs.storage.IStorageData;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.storage.InspectITStorageManager;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;
import rocks.inspectit.ui.rcp.wizard.page.ExportStorageWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.StorageCompressionWizardPage;

/**
 * Wizard for exporting the storage.
 *
 * @author Ivan Senic
 *
 */
public class ExportStorageWizard extends Wizard implements INewWizard {

	/**
	 * Storage to export.
	 */
	private IStorageData storageData;

	/**
	 * Cmr repository definition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link ExportStorageWizardPage}.
	 */
	private ExportStorageWizardPage exportStorageWizardPage;

	/**
	 * Wizard page.
	 */
	private StorageCompressionWizardPage storageCompressionWizardPage;

	/**
	 * Default constructor.
	 */
	protected ExportStorageWizard() {
		this.setWindowTitle("Export Storage");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_EXPORT));
	}

	/**
	 * Default constructor.
	 *
	 * @param localStorageData
	 *            Storage to export.
	 */
	public ExportStorageWizard(LocalStorageData localStorageData) {
		this();
		this.storageData = localStorageData;
	}

	/**
	 * Default constructor.
	 *
	 * @param storageData
	 *            Storage to export.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} where storage is located.
	 */
	public ExportStorageWizard(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		this();
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
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
		exportStorageWizardPage = new ExportStorageWizardPage(storageData);
		addPage(exportStorageWizardPage);
		if (storageData instanceof StorageData) {
			StorageData remoteStorageData = (StorageData) storageData;
			if (!InspectIT.getDefault().getInspectITStorageManager().isFullyDownloaded(remoteStorageData)) {
				String title = "Export Storage";
				String message = "Options for exporting the storage '" + storageData.getName() + "' (size: " + NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()) + ")";
				storageCompressionWizardPage = new StorageCompressionWizardPage(title, message);
				addPage(storageCompressionWizardPage);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
		final String fileName = exportStorageWizardPage.getFileName();
		LocalStorageData localStorageData = null;
		if (storageData instanceof LocalStorageData) {
			localStorageData = (LocalStorageData) storageData;
		} else if (storageData instanceof StorageData) {
			localStorageData = storageManager.getLocalDataForStorage((StorageData) storageData);
		}

		if ((null != localStorageData) && localStorageData.isFullyDownloaded()) {
			final LocalStorageData finalLocalStorageData = localStorageData;
			Job exportStorageJob = new Job("Export Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Exporting data..", IProgressMonitor.UNKNOWN);
					try {
						storageManager.zipStorageData(finalLocalStorageData, fileName);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IViewPart storageManagerView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
								}
								InspectIT.getDefault().createInfoDialog("The storage was exported successfully.", -1);
							}
						});
					} catch (Exception e) {
						return new Status(IStatus.ERROR, InspectIT.ID, "Exception occurred trying to export storage.", e);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			exportStorageJob.setUser(true);
			exportStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EXPORT));
			exportStorageJob.schedule();
		} else {
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				final boolean compress = storageCompressionWizardPage.isCompressBefore();
				Job downloadAndExportStorageJob = new Job("Download And Export Storage") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						SubMonitor subMonitor = SubMonitor.convert(monitor);
						try {
							storageManager.zipStorageData((StorageData) storageData, cmrRepositoryDefinition, fileName, compress, subMonitor);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									InspectIT.getDefault().createInfoDialog("The storage was downloaded and exported successfully.", -1);

								}
							});
						} catch (BusinessException | SerializationException | IOException e) {
							return new Status(IStatus.ERROR, InspectIT.ID, "Exception occurred trying to export storage.", e);
						}
						monitor.done();
						return Status.OK_STATUS;

					}
				};
				downloadAndExportStorageJob.setUser(true);
				downloadAndExportStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EXPORT));
				downloadAndExportStorageJob.schedule();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createInfoDialog("The storage could not be downloaded, the CMR Repository is offline. Export will be aborted.", -1);
					}
				});
			}
		}

		return true;
	}
}
