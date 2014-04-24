package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.job.OpenEnvironmentJob;
import info.novatec.inspectit.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

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
		Environment environment = new Environment();
		environment.setName(name);
		if (StringUtils.isNotBlank(description)) {
			environment.setDescription(description);
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				Environment created = cmrRepositoryDefinition.getConfigurationInterfaceService().createEnvironment(environment);

				// open created one
				new OpenEnvironmentJob(cmrRepositoryDefinition, created.getId(), workbench.getActiveWorkbenchWindow().getActivePage()).schedule();

				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentCreated(created, cmrRepositoryDefinition);
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog("Environment can not be created.", e, -1);
				return false;
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Environment can not be created. Selected CMR repository is currently not available.", -1);
			return false;
		}

		return true;
	}

}
