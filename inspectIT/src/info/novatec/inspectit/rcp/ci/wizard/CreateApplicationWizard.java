package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.job.OpenApplicationDefinitionJob;
import info.novatec.inspectit.rcp.ci.view.BusinessContextManagerViewPart;
import info.novatec.inspectit.rcp.ci.wizard.page.NameDescriptionInsertBeforeWizardPage;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for creating new {@link ApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class CreateApplicationWizard extends Wizard implements INewWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Application";

	/**
	 * Wizard default message.
	 */
	private static final String MESSAGE = "Define the information for the new application";

	/**
	 * {@link CmrRepositoryDefinition} to create environment on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link NameDescriptionInsertBeforeWizardPage}.
	 */
	private NameDescriptionInsertBeforeWizardPage newApplicationWizardPage;

	/**
	 * The workbench for wizard.
	 */
	private IWorkbench workbench;

	/**
	 * Default constructor.
	 */
	public CreateApplicationWizard() {
		this.setWindowTitle(TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			List<ApplicationDefinition> applications = cmrRepositoryDefinition.getConfigurationInterfaceService().getApplicationDefinitions();

			String[] items = new String[applications.size()];
			int i = 0;
			for (ApplicationDefinition app : applications) {
				items[i] = app.getApplicationName();
				i++;
			}
			newApplicationWizardPage = new NameDescriptionInsertBeforeWizardPage(TITLE, MESSAGE, items, BusinessContextManagerViewPart.APP_ORDER_INFO_TOOLTIP);
			addPage(newApplicationWizardPage);
		} else {
			InspectIT.getDefault().createErrorDialog("Application can not be created. Selected CMR repository is currently not available.", -1);
		}

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
		String name = newApplicationWizardPage.getName();
		String description = newApplicationWizardPage.getDescription();
		int insertBeforeIndex = newApplicationWizardPage.getInsertedBeforeIndex();

		ApplicationDefinition newApplication = new ApplicationDefinition(name);
		if (StringUtils.isNotBlank(description)) {
			newApplication.setDescription(description);
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				newApplication = cmrRepositoryDefinition.getConfigurationInterfaceService().addApplicationDefinition(newApplication, insertBeforeIndex);
				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationCreated(newApplication, insertBeforeIndex, cmrRepositoryDefinition);
				new OpenApplicationDefinitionJob(cmrRepositoryDefinition, newApplication.getId(), workbench.getActiveWorkbenchWindow().getActivePage()).schedule();
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog("Application can not be created.", e, -1);
				return false;
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Application can not be created. Selected CMR repository is currently not available.", -1);
			return false;
		}

		return true;
	}
}
