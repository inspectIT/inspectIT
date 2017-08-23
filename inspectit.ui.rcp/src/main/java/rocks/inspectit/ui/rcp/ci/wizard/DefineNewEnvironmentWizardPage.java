package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.Collection;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;

/**
 * Defines environment page, which creates a new environment with name and description.
 * 
 * @author Tobias Angerstein
 *
 */
public class DefineNewEnvironmentWizardPage extends DefineNameAndDescriptionWizardPage {

	/**
	 * Already existing environments.
	 */
	private Collection<Environment> existingEnvironments;

	/**
	 * Default constructor.
	 *
	 * @param title
	 *            Title of the page.
	 * @param defaultMessage
	 *            Default message for the page.
	 * @param existingEnvironments
	 *            existing environments
	 * 
	 */
	public DefineNewEnvironmentWizardPage(String title, String defaultMessage, Collection<Environment> existingEnvironments) {
		super(title, defaultMessage);
		this.existingEnvironments = existingEnvironments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		return !nameBox.getText().isEmpty() && !environmentAlreadyExists(nameBox.getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the name entered", ERROR);
		} else if (environmentAlreadyExists(nameBox.getText())) {
			setMessage("Environment with entered name already exists!", ERROR);
		} else {
			setMessage(defaultMessage);
		}
	}

	/**
	 * Check if environment name already exists.
	 * 
	 * @param environmentName
	 *            environment name
	 * 
	 * @return true if environment already exists
	 */
	private boolean environmentAlreadyExists(String environmentName) {
		for (Environment environment : existingEnvironments) {
			if (environment.getName().equals(environmentName)) {
				return true;
			}
		}
		return false;
	}
}
