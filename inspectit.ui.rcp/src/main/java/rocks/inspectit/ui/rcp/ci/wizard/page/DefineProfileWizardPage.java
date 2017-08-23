package rocks.inspectit.ui.rcp.ci.wizard.page;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.ExcludeRulesProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.JmxDefinitionProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;

/**
 * Define profile page that adds the profile type option to the name and the description.
 *
 * @author Ivan Senic
 *
 */
public class DefineProfileWizardPage extends DefineNameAndDescriptionWizardPage {

	/**
	 * Type combo.
	 */
	private Combo typeCombo;

	/**
	 * Duplicate profile if profile is being duplicated.
	 */
	private final Profile duplicateProfile;

	/**
	 * Default constructor.
	 *
	 * @param title
	 *            Title of the page.
	 * @param defaultMessage
	 *            Default message for the page.
	 * @param duplicateProfile
	 *            Duplicate profile instance if duplicate action is on.
	 * @param existingNames
	 *            Already existing profile names.
	 */
	public DefineProfileWizardPage(String title, String defaultMessage, Profile duplicateProfile, Collection<String> existingNames) {
		super(title, defaultMessage, existingNames);
		this.duplicateProfile = duplicateProfile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite main = (Composite) super.getControl();
		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Type:");

		typeCombo = new Combo(main, SWT.BORDER | SWT.READ_ONLY);
		typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// sensor assignment
		SensorAssignmentProfileData sensorAssignmentProfileData = new SensorAssignmentProfileData();
		typeCombo.add(sensorAssignmentProfileData.getName());
		typeCombo.setData(sensorAssignmentProfileData.getName(), sensorAssignmentProfileData);
		// exclude rules
		ExcludeRulesProfileData excludeRulesProfileData = new ExcludeRulesProfileData();
		typeCombo.add(excludeRulesProfileData.getName());
		typeCombo.setData(excludeRulesProfileData.getName(), excludeRulesProfileData);
		// jmx definition
		JmxDefinitionProfileData jmxProfileData = new JmxDefinitionProfileData();
		typeCombo.add(jmxProfileData.getName());
		typeCombo.setData(jmxProfileData.getName(), jmxProfileData);

		// select sensor as default if not duplicate, otherwise select correct profile data type
		if (null == duplicateProfile) {
			typeCombo.select(0);
		} else {
			AbstractProfileData<?> profileData = duplicateProfile.getProfileData();
			typeCombo.setData(profileData.getName(), profileData);
			typeCombo.select(typeCombo.indexOf(profileData.getName()));
			typeCombo.setEnabled(false);
		}
	}

	/**
	 * Returns selected profile data object.
	 *
	 * @return Returns selected profile data object.
	 */
	public AbstractProfileData<?> getProfileData() {
		return (AbstractProfileData<?>) typeCombo.getData(typeCombo.getText());
	}

}
