package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.wizard.page.AddStorageLabelWizardPage;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for adding label to storage.
 * 
 * @author Ivan Senic
 * 
 */
public class AddStorageLabelWizard extends Wizard implements INewWizard {

	/**
	 * {@link StorageData} to add label.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition} where storage is located.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Page for wizard.
	 */
	private AddStorageLabelWizardPage addStorageLabelWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param storageDataProvider
	 *            Selected provider.
	 */
	public AddStorageLabelWizard(IStorageDataProvider storageDataProvider) {
		super();
		Assert.isNotNull(storageDataProvider);
		this.setWindowTitle("Add Storage Label Wizard");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_LABEL));
		this.storageData = storageDataProvider.getStorageData();
		this.cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		addStorageLabelWizardPage = new AddStorageLabelWizardPage(storageData, cmrRepositoryDefinition);
		addPage(addStorageLabelWizardPage);
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
	public boolean performFinish() {
		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			List<AbstractStorageLabel<?>> labelsToAdd = addStorageLabelWizardPage.getLabelsToAdd();
			try {
				StorageData updatedStorageData = cmrRepositoryDefinition.getStorageService().addLabelsToStorage(storageData, labelsToAdd, true);
				try {
					InspectIT.getDefault().getInspectITStorageManager().storageRemotelyUpdated(updatedStorageData);
				} catch (Exception e) {
					InspectIT.getDefault().createErrorDialog("Error occurred trying to save local storage data to disk.", e, -1);
				}
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog("Adding label to storage failed.", e, -1);
				return false;
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Adding label to storage failed. Selected CMR repository is currently not available.", -1);
			return false;
		}
		return true;
	}
}
