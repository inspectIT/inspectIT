package info.novatec.inspectit.rcp.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.LoginStatus;
import info.novatec.inspectit.rcp.wizard.page.CmrLoginWizardPage;

/**
 * Wizard for logging into a CMR.
 * 
 * @author Clemens Geibel
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 */

public class CmrLoginWizard extends Wizard implements INewWizard {

	/**
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link CmrLoginWizardPage}.
	 */
	private CmrLoginWizardPage cmrLoginWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            .
	 */
	public CmrLoginWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.setWindowTitle("CMR Login");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		cmrLoginWizardPage = new CmrLoginWizardPage("CMR Login");
		addPage(cmrLoginWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc} Tries to log into the CMR and to get and set the list with
	 * grantedPermissions.
	 */
	@Override
	public boolean performFinish() {
		cmrRepositoryDefinition.refreshLoginStatus();
		if (cmrRepositoryDefinition.getLoginStatus().equals(LoginStatus.LOGGEDOUT)) {
			String email = cmrLoginWizardPage.getMailBox().getText();
			String password = cmrLoginWizardPage.getPasswordBox().getText();

			boolean loggedin = cmrRepositoryDefinition.login(email, password);

			if (loggedin) {
				MessageDialog.openInformation(null, "Successfully authenticated at selected CMR",
						"You are now logged in.");
				if (cmrLoginWizardPage.shouldStayLoggedIn()) {
					cmrRepositoryDefinition.stayLoggedIn(cmrLoginWizardPage.getMailBox().getText(),	cmrLoginWizardPage.getPasswordBox().getText());
				}
			} else {
				MessageDialog.openError(null, "Login failed",
						"E-Mail or Password is incorrect or you are locked by admin!");
			}
			return loggedin;
		} else {
			MessageDialog.openError(null, "Error", "You are already logged in.");
			return false;
		}

	}

}
