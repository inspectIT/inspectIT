package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertDetailsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertSourceDefinitionWizardPage;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.provider.IAlertDefinitionProvider;

/**
 * Wizard for editing alert definitions.
 *
 * @author Alexander Wert
 *
 */
public class EditAlertDefinitionWizard extends AbstractAlertDefinitionWizard {
	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Edit Alert Definition";

	/**
	 * Constructor.
	 */
	public EditAlertDefinitionWizard() {
		super(TITLE);
	}

	/**
	 * Initial {@link AlertingDefinition} instance. If set with the constructor, then this wizard is
	 * used to edit this instance.
	 */
	private AlertingDefinition initialAlertDefinition;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof IAlertDefinitionProvider) {
			IAlertDefinitionProvider alertDefinitionProvider = (IAlertDefinitionProvider) structuredSelection.getFirstElement();
			cmrRepositoryDefinition = alertDefinitionProvider.getCmrRepositoryDefinition();
			initialAlertDefinition = alertDefinitionProvider.getAlertDefinition();
			this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_EDIT));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void callServiceAndNotify(final AlertingDefinition alertDefinition) throws BusinessException {
		ProgressDialog<Void> dialog = new ProgressDialog<Void>("Saving alert definition to the CMR..", IProgressMonitor.UNKNOWN) {
			@Override
			public Void execute(IProgressMonitor monitor) throws BusinessException {
				AlertingDefinition updatedAlertingDefinition = cmrRepositoryDefinition.getConfigurationInterfaceService().updateAlertingDefinition(alertDefinition);

				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionUpdated(updatedAlertingDefinition, cmrRepositoryDefinition);

				return null;
			}
		};

		dialog.start(true, false);

		if (!dialog.wasSuccessful()) {
			InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to edit the alert definition.", dialog.getThrownException(), -1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertSourceDefinitionWizardPage createAlertSourceWizardPage(List<String> existingNames) {
		existingNames.remove(initialAlertDefinition.getName());
		return new AlertSourceDefinitionWizardPage(cmrRepositoryDefinition.getInfluxDBService(), existingNames, initialAlertDefinition.getName(), initialAlertDefinition.getMeasurement(),
				initialAlertDefinition.getField(), initialAlertDefinition.getTags());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertDetailsWizardPage createAlertDetailsWizardPage() {
		return new AlertDetailsWizardPage(initialAlertDefinition.getThreshold(), initialAlertDefinition.getThresholdType().equals(ThresholdType.LOWER_THRESHOLD),
				initialAlertDefinition.getTimeRange(TimeUnit.MINUTES), initialAlertDefinition.getNotificationEmailAddresses());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertingDefinition getAlertingDefinitionForFinish() {
		return initialAlertDefinition;
	}
}
