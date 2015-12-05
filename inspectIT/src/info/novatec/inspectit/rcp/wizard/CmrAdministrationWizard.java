package info.novatec.inspectit.rcp.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.page.CmrAdministrationWizardPage;

/**
 * Wizard for managing users on the CMR.
 * 
 * @author Lucca Hellriegel
 */
public class CmrAdministrationWizard extends Wizard implements INewWizard {

	
	
	/**
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link CmrLoginWizardPage}.
	 */
	private CmrAdministrationWizardPage cmrAdministrationWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition 
	 *            
	 */
	public CmrAdministrationWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.setWindowTitle("CMR Administration");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		cmrAdministrationWizardPage = new CmrAdministrationWizardPage("CMR Administration", cmrRepositoryDefinition);
		addPage(cmrAdministrationWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc} Closes the wizard.
	 */
	@Override
	public boolean performFinish() {
		return false;

	}

}
