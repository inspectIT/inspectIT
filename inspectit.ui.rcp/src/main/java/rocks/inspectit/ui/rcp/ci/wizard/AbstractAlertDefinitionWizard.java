package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertDetailsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertSourceDefinitionWizardPage;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Abstract Wizard for creating/editing new alert definitions.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractAlertDefinitionWizard extends Wizard implements INewWizard {

	/**
	 * {@link CmrRepositoryDefinition} to create alert definitions on.
	 */
	protected CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Source definition wizard page.
	 */
	private AlertSourceDefinitionWizardPage alertSourcePage;

	/**
	 * Alerting details wizard page.
	 */
	private AlertDetailsWizardPage alertdetailsPage;

	/**
	 * Default Constructor.
	 *
	 * @param title
	 *            The wizard title.
	 *
	 */
	public AbstractAlertDefinitionWizard(String title) {
		this.setWindowTitle(title);
	}

	/**
	 * Notifies corresponding listener about alert definition creation / update.
	 *
	 * @param alertDefinition
	 *            Edited {@link AlertingDefinition} instance.
	 * @throws BusinessException
	 *             Thrown if notification fails.
	 */
	protected abstract void callServiceAndNotify(AlertingDefinition alertDefinition) throws BusinessException;

	/**
	 * Creates alert source wizard page.
	 *
	 * @param existingNames
	 *            Existing alert definition names.
	 * @return {@link AlertSourceDefinitionWizardPage} instance.
	 */
	protected abstract AlertSourceDefinitionWizardPage createAlertSourceWizardPage(List<String> existingNames);

	/**
	 * Creates alert details wizard page.
	 *
	 * @return {@link AlertDetailsWizardPage} instance.
	 */
	protected abstract AlertDetailsWizardPage createAlertDetailsWizardPage();

	/**
	 * Retrieves the {@link AlertingDefinition} instance for finishing the wizard.
	 *
	 * @return {@link AlertingDefinition} instance.
	 */
	protected abstract AlertingDefinition getAlertingDefinitionForFinish();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
			this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_ADD));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void addPages() {
		ProgressDialog<List<AlertingDefinition>> dialog = new ProgressDialog<List<AlertingDefinition>>("Loading alerting definitions..", IProgressMonitor.UNKNOWN) {
			@Override
			public List<AlertingDefinition> execute(IProgressMonitor monitor) throws BusinessException {
				return cmrRepositoryDefinition.getConfigurationInterfaceService().getAlertingDefinitions();
			}
		};

		List<AlertingDefinition> alertDefinitions = null;

		dialog.start(true, false);

		if (dialog.wasSuccessful()) {
			alertDefinitions = dialog.getResult();
		} else {
			InspectIT.getDefault().log(IStatus.WARNING, "Error while fetching alert definitions", dialog.getThrownException());
			alertDefinitions = Collections.EMPTY_LIST;
		}

		List<String> existingNames = new ArrayList<>();
		for (AlertingDefinition alertDef : alertDefinitions) {
			existingNames.add(alertDef.getName());
		}

		alertSourcePage = createAlertSourceWizardPage(existingNames);
		alertdetailsPage = createAlertDetailsWizardPage();

		addPage(alertSourcePage);
		addPage(alertdetailsPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		try {
			AlertingDefinition alertDefinition = getAlertingDefinitionForFinish();
			alertDefinition.setName(alertSourcePage.getAlertingDefinitionName());
			alertDefinition.setMeasurement(alertSourcePage.getMeasurement());
			alertDefinition.setField(alertSourcePage.getField());
			alertDefinition.replaceTags(alertSourcePage.getTags());
			alertDefinition.setThreshold(alertdetailsPage.getThreshold());
			alertDefinition.setThresholdType(alertdetailsPage.getThresholdType());
			alertDefinition.setTimeRange(alertdetailsPage.getTimerange(), TimeUnit.MINUTES);
			alertDefinition.replaceNotificationEmailAddresses(alertdetailsPage.getEmailAddresses());

			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				callServiceAndNotify(alertDefinition);
			} else {
				InspectIT.getDefault().createErrorDialog("Alert definition can not be created. Selected CMR repository is currently not available.", -1);
				return false;
			}
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("Alert definition can not be created.", e, -1);
			return false;
		}

		return true;
	}

}
