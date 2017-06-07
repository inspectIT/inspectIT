package rocks.inspectit.ui.rcp.ci.wizard;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.job.OpenEnvironmentJob;
import rocks.inspectit.ui.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Wizard for creating new environment.
 *
 * @author Ivan Senic
 *
 */
public class CreateEnvironmentWizard extends Wizard implements INewWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Environment";

	/**
	 * Wizard default message.
	 */
	private static final String MESSAGE = "Define the information for the new Environment";

	/**
	 * {@link CmrRepositoryDefinition} to create environment on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link DefineNameAndDescriptionWizardPage}.
	 */
	private DefineNameAndDescriptionWizardPage defineNameAndDescriptionWizardPage;

	/**
	 * The workbench for wizard.
	 */
	private IWorkbench workbench;

	/**
	 * Default constructor.
	 */
	public CreateEnvironmentWizard() {
		this.setWindowTitle(TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		defineNameAndDescriptionWizardPage = new DefineNameAndDescriptionWizardPage(TITLE, MESSAGE);
		addPage(defineNameAndDescriptionWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		String name = defineNameAndDescriptionWizardPage.getName();
		String description = defineNameAndDescriptionWizardPage.getDescription();
		final Environment environment = new Environment();
		environment.setName(name);
		if (StringUtils.isNotBlank(description)) {
			environment.setDescription(description);
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			ProgressDialog<Environment> dialog = new ProgressDialog<Environment>("Creating new environment..", IProgressMonitor.UNKNOWN) {
				@Override
				public Environment execute(IProgressMonitor monitor) throws BusinessException {
					return cmrRepositoryDefinition.getConfigurationInterfaceService().createEnvironment(environment);
				}

			};

			dialog.start(true, false);

			if (dialog.wasSuccessful()) {
				Environment created = dialog.getResult();

				// open created one
				new OpenEnvironmentJob(cmrRepositoryDefinition, created.getId(), workbench.getActiveWorkbenchWindow().getActivePage()).schedule();

				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentCreated(created, cmrRepositoryDefinition);
			} else {
				InspectIT.getDefault().createErrorDialog("Environment can not be created.", dialog.getThrownException(), -1);
				return false;
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Environment can not be created. Selected CMR repository is currently not available.", -1);
			return false;
		}

		return true;
	}

}
