package rocks.inspectit.ui.rcp.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;
import rocks.inspectit.ui.rcp.wizard.page.DefineNewStorageWizzardPage;

/**
 * Wizard for creating and opening the storage.
 *
 * @author Ivan Senic
 *
 */
public class CreateStorageWizard extends Wizard implements INewWizard {

	/**
	 * New storage page.
	 */
	private DefineNewStorageWizzardPage defineNewStoragePage;

	/**
	 * Selected CMR repository.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 */
	public CreateStorageWizard() {
		super();
		this.setWindowTitle("Create Storage Wizard");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_STORAGE));
	}

	/**
	 * This constructor will set provided {@link CmrRepositoryDefinition} as the initially selected
	 * repository to create storage to. Force open, means that the option if the storage will be
	 * opened or not, will not be available for the user.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to create storage on.
	 */
	public CreateStorageWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this();
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
		defineNewStoragePage = new DefineNewStorageWizzardPage(cmrRepositoryDefinition, false);
		addPage(defineNewStoragePage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final CmrRepositoryDefinition cmrRepositoryDefinition = defineNewStoragePage.getSelectedRepository();
		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			final StorageData storageData = defineNewStoragePage.getStorageData();

			ProgressDialog<StorageData> dialog = new ProgressDialog<StorageData>("Creating storage..", IProgressMonitor.UNKNOWN) {
				@Override
				public StorageData execute(IProgressMonitor monitor) throws BusinessException {
					return cmrRepositoryDefinition.getStorageService().createAndOpenStorage(storageData);
				}
			};

			dialog.start(true, false);

			if (dialog.wasSuccessful()) {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
				if (viewPart instanceof StorageManagerView) {
					((StorageManagerView) viewPart).refresh();
				}
			} else {
				InspectIT.getDefault().createErrorDialog("Storage can not be created.", dialog.getThrownException(), -1);
				return false;
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Storage can not be created. Selected CMR repository is currently not available.", -1);
			return false;
		}
		return true;
	}

}
