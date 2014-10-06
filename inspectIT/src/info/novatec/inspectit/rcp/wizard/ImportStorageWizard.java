package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.ImportStorageInfoPage;
import info.novatec.inspectit.rcp.wizard.page.ImportStorageSelectPage;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.util.ObjectUtils;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Wizard for importing the storages.
 * 
 * @author Ivan Senic
 * 
 */
public class ImportStorageWizard extends Wizard implements INewWizard {

	/**
	 * {@link ImportStorageSelectPage}.
	 */
	private ImportStorageSelectPage importStorageSelectPage;

	/**
	 * {@link ImportStorageInfoPage}.
	 */
	private ImportStorageInfoPage importStorageInfoPage;

	/**
	 * Default constructor.
	 */
	public ImportStorageWizard() {
		this.setWindowTitle("Import Storage");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_IMPORT));
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
		importStorageSelectPage = new ImportStorageSelectPage();
		addPage(importStorageSelectPage);
		importStorageInfoPage = new ImportStorageInfoPage();
		addPage(importStorageInfoPage);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (ObjectUtils.equals(page, importStorageSelectPage)) {
			importStorageInfoPage.setFileName(importStorageSelectPage.getFileName());
			importStorageInfoPage.setImportLocally(importStorageSelectPage.isImportLocally());
			importStorageInfoPage.setCmrRepositoryDefinition(importStorageSelectPage.getCmrRepositoryDefinition());
			importStorageInfoPage.update();
		}
		return super.getNextPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, importStorageInfoPage)) {
			importStorageInfoPage.reset();
		}
		return super.getPreviousPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage().equals(importStorageSelectPage)) {
			return false;
		} else {
			if (!importStorageSelectPage.isPageComplete()) {
				return false;
			}
			if (!importStorageInfoPage.isPageComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final String fileName = importStorageSelectPage.getFileName();
		final CmrRepositoryDefinition cmrRepositoryDefinition = importStorageSelectPage.getCmrRepositoryDefinition();
		boolean importLocally = importStorageSelectPage.isImportLocally();
		if (importLocally) {
			Job importStorageJob = new Job("Import Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Importing data..", IProgressMonitor.UNKNOWN);
					try {
						InspectIT.getDefault().getInspectITStorageManager().unzipStorageData(fileName);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
								}
								InspectIT.getDefault().createInfoDialog("Storage successfully imported.", -1);
							}
						});
					} catch (BusinessException | SerializationException | IOException e) {
						return new Status(Status.ERROR, InspectIT.ID, "Exception occurred trying to import the storage via file.", e);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			importStorageJob.setUser(true);
			importStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_IMPORT));
			importStorageJob.schedule();
		} else {
			Job importStorageJob = new Job("Import Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					SubMonitor subMonitor = SubMonitor.convert(monitor);
					subMonitor.setWorkRemaining(10); // 9 units for uploading, 1 for unpacking
					InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
					try {
						storageManager.uploadZippedStorage(fileName, cmrRepositoryDefinition, subMonitor.newChild(9));
					} catch (final Exception e) {
						return new Status(Status.ERROR, InspectIT.ID, "Storage data was not successfully uploaded to the CMR. Import failed.", e);
					}

					IProgressMonitor unpackMonitor = subMonitor.newChild(1);
					unpackMonitor.setTaskName("Unpacking data..");
					try {
						IStorageData storageData = storageManager.getStorageDataFromZip(fileName);
						cmrRepositoryDefinition.getStorageService().unpackUploadedStorage(storageData);
					} catch (final BusinessException e) {
						return new Status(Status.ERROR, InspectIT.ID, "Storage data was not successfully unpacked on the CMR. Import failed.", e);
					}
					unpackMonitor.done();

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
							if (storageManagerView instanceof StorageManagerView) {
								((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
							}
							InspectIT.getDefault().createInfoDialog("Storage data was successfully imported to the CMR.", -1);
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			importStorageJob.setUser(true);
			importStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_IMPORT));
			importStorageJob.schedule();
		}

		return true;
	}
}
