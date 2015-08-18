package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.wizard.page.ManageLabelWizardPage;
import info.novatec.inspectit.storage.label.management.AbstractLabelManagementAction;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

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
