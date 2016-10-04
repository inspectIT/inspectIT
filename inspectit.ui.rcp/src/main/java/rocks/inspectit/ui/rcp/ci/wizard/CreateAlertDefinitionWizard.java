package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.List;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertDetailsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.AlertSourceDefinitionWizardPage;

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
	protected void notifyListener(AlertingDefinition alertDefinition) throws BusinessException {
		AlertingDefinition newAlertingDefinition = cmrRepositoryDefinition.getConfigurationInterfaceService().createAlertingDefinition(alertDefinition);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().alertDefinitionCreated(newAlertingDefinition, cmrRepositoryDefinition);
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
