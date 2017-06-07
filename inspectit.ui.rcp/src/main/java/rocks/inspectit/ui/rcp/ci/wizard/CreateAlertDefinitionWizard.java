package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertDetailsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertSourceDefinitionWizardPage;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;

/**
 * Wizard for creating alert definitions.
 *
 * @author Alexander Wert
 *
 */
public class CreateAlertDefinitionWizard extends AbstractAlertDefinitionWizard {
	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Alert Definition";

	/**
	 * Constructor.
	 */
	public CreateAlertDefinitionWizard() {
		super(TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void callServiceAndNotify(final AlertingDefinition alertDefinition) throws BusinessException {

		ProgressDialog<Void> dialog = new ProgressDialog<Void>("Saving alert definition to the CMR..", IProgressMonitor.UNKNOWN) {
			@Override
			public Void execute(IProgressMonitor monitor) throws BusinessException {
				AlertingDefinition createdAlertingDefinition = cmrRepositoryDefinition.getConfigurationInterfaceService().createAlertingDefinition(alertDefinition);

				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionCreated(createdAlertingDefinition, cmrRepositoryDefinition);

				return null;
			}
		};

		dialog.start(true, false);

		if (!dialog.wasSuccessful()) {
			InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to save the alert definition.", dialog.getThrownException(), -1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertSourceDefinitionWizardPage createAlertSourceWizardPage(List<String> existingNames) {
		return new AlertSourceDefinitionWizardPage(cmrRepositoryDefinition.getInfluxDBService(), existingNames);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertDetailsWizardPage createAlertDetailsWizardPage() {
		return new AlertDetailsWizardPage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AlertingDefinition getAlertingDefinitionForFinish() {
		return new AlertingDefinition();
	}

}
