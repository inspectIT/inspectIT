package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertDetailsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertSourceDefinitionWizardPage;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Wizard for creating new alert definitions.
 *
 * @author Alexander Wert
 *
 */
public class AlertDefinitionWizard extends Wizard implements INewWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Alert Definition";

	/**
	 * {@link CmrRepositoryDefinition} to create alert definitions on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Source definition wizard page.
	 */
	private AlertSourceDefinitionWizardPage alertSourcePage;

	/**
	 * Alerting details wizard page.
	 */
	private AlertDetailsWizardPage alertdetailsPage;

	/**
	 * Initial {@link AlertingDefinition} instance. If set with the constructor, then this wizard is
	 * used to edit this instance.
	 */
	private final AlertingDefinition initialAlertDefinition;

	/**
	 * Default Constructor.
	 *
	 * If this constructor is called, the a new {@link AlertingDefinition} is created by this
	 * wizard.
	 */
	public AlertDefinitionWizard() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * If this constructor is called, then the passed {@link AlertingDefinition} instance is edited
	 * in this wizard.
	 *
	 * @param initialAlertDefinition
	 *            {@link AlertingDefinition} instance to be edited.
	 */
	public AlertDefinitionWizard(AlertingDefinition initialAlertDefinition) {
		this.setWindowTitle(TITLE);
		this.initialAlertDefinition = initialAlertDefinition;
		if (null != initialAlertDefinition) {
			this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_EDIT));
		} else {
			this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_ADD));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			List<AlertingDefinition> alertDefinitions = cmrRepositoryDefinition.getConfigurationInterfaceService().getAlertingDefinitions();
			List<String> existingNames = new ArrayList<>();
			for (AlertingDefinition alertDef : alertDefinitions) {
				existingNames.add(alertDef.getName());
			}

			if (null != initialAlertDefinition) { // editing mode
				existingNames.remove(initialAlertDefinition.getName());
				alertSourcePage = new AlertSourceDefinitionWizardPage(cmrRepositoryDefinition.getInfluxDBService(), existingNames, initialAlertDefinition.getName(),
						initialAlertDefinition.getMeasurement(), initialAlertDefinition.getField(), initialAlertDefinition.getTags());
				alertdetailsPage = new AlertDetailsWizardPage(initialAlertDefinition.getThreshold(), initialAlertDefinition.getThresholdType().equals(ThresholdType.LOWER_THRESHOLD),
						initialAlertDefinition.getTimeRange(TimeUnit.MINUTES), initialAlertDefinition.getNotificationEmailAddresses());
			} else { // new alerting definition creation mode
				alertSourcePage = new AlertSourceDefinitionWizardPage(cmrRepositoryDefinition.getInfluxDBService(), existingNames);
				alertdetailsPage = new AlertDetailsWizardPage();
			}

			addPage(alertSourcePage);
			addPage(alertdetailsPage);
		} else {
			InspectIT.getDefault().createErrorDialog("Application can not be created. Selected CMR repository is currently not available.", -1);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFinish() {
		return alertSourcePage.isPageComplete() && alertdetailsPage.isPageComplete();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		try {
			AlertingDefinition alertDefinition = null != initialAlertDefinition ? initialAlertDefinition : new AlertingDefinition();
			alertDefinition.setName(alertSourcePage.getAlertingDefinitionName());
			alertDefinition.setMeasurement(alertSourcePage.getMeasurement());
			alertDefinition.setField(alertSourcePage.getField());
			alertDefinition.replaceTags(alertSourcePage.getTags());
			alertDefinition.setThreshold(alertdetailsPage.getThreshold());
			alertDefinition.setThresholdType(alertdetailsPage.getThresholdType());
			alertDefinition.setTimeRange(alertdetailsPage.getTimerange(), TimeUnit.MINUTES);
			alertDefinition.replaceNotificationEmailAddresses(alertdetailsPage.getEmailAddresses());

			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				if (null != initialAlertDefinition) {
					AlertingDefinition updatedAlertingDefinition = cmrRepositoryDefinition.getConfigurationInterfaceService().updateAlertingDefinition(alertDefinition);
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionUpdated(updatedAlertingDefinition, cmrRepositoryDefinition);
				} else {
					AlertingDefinition newAlertingDefinition = cmrRepositoryDefinition.getConfigurationInterfaceService().createAlertingDefinition(alertDefinition);
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionCreated(newAlertingDefinition, cmrRepositoryDefinition);
				}
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
