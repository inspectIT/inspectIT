package rocks.inspectit.ui.rcp.wizard;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.storage.label.management.AbstractLabelManagementAction;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.wizard.page.ManageLabelWizardPage;

/**
 * Manage Labels wizard.
 * 
 * @author Ivan Senic
 * 
 */
public class ManageLabelWizard extends Wizard implements INewWizard {

	/**
	 * CMR to manage labels for.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Page.
	 */
	private ManageLabelWizardPage manageLabelsPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository to manage labels for.
	 */
	public ManageLabelWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setWindowTitle("Manage Labels");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_LABEL));
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
		manageLabelsPage = new ManageLabelWizardPage(cmrRepositoryDefinition);
		addPage(manageLabelsPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		List<AbstractLabelManagementAction> actions = manageLabelsPage.getManagementActions();
		if (!actions.isEmpty()) {
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				try {
					cmrRepositoryDefinition.getStorageService().executeLabelManagementActions(actions);
				} catch (BusinessException e) {
					InspectIT.getDefault().createErrorDialog("There was an exception trying to execute label management operation.", e, -1);
				}
			} else {
				InspectIT.getDefault().createInfoDialog("Can not execute label management operation, selected CMR repository is offline.", -1);
			}
		}
		return true;
	}

	/**
	 * Gets {@link #shouldRefreshStorages}.
	 * 
	 * @return {@link #shouldRefreshStorages}
	 */
	public boolean isShouldRefreshStorages() {
		return manageLabelsPage.isShouldRefreshStorages();
	}

}
