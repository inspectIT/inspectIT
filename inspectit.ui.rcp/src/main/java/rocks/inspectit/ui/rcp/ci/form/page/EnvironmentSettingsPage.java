package rocks.inspectit.ui.rcp.ci.form.page;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.EUMSettingsPart;
import rocks.inspectit.ui.rcp.ci.form.part.EnvironmentSettingsPart;
import rocks.inspectit.ui.rcp.ci.form.part.JmxSensorOptionsPart;
import rocks.inspectit.ui.rcp.ci.form.part.LoggingSensorOptionsPart;
import rocks.inspectit.ui.rcp.ci.form.part.PlatformSensorSelectionPart;
import rocks.inspectit.ui.rcp.ci.form.part.ProfileSelectionPart;
import rocks.inspectit.ui.rcp.ci.form.part.SensorOptionsPart;

/**
 * Page for the environment general settings.
 *
 * @author Ivan Senic
 *
 */
public class EnvironmentSettingsPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = EnvironmentSettingsPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Environment Settings";

	/**
	 * Default constructor.
	 *
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public EnvironmentSettingsPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TOOL));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		// body
		Composite body = form.getBody();
		body.setLayout(new GridLayout(2, true));

		Composite left = toolkit.createComposite(body);
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		left.setLayout(new GridLayout(1, true));

		Composite right = toolkit.createComposite(body);
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout(1, true);
		gl.verticalSpacing = 20;
		right.setLayout(gl);

		ProfileSelectionPart profileSelectionPart = new ProfileSelectionPart(this, left, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		profileSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		managedForm.addPart(profileSelectionPart);

		EnvironmentSettingsPart environmentSettingsPart = new EnvironmentSettingsPart(this, right, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		environmentSettingsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(environmentSettingsPart);

		EUMSettingsPart eumSettingsPart = new EUMSettingsPart(this, right, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		eumSettingsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(eumSettingsPart);

		PlatformSensorSelectionPart platformSensorSelectionPart = new PlatformSensorSelectionPart(this, right, toolkit,
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		platformSensorSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(platformSensorSelectionPart);

		SensorOptionsPart sensorOptionsPart = new SensorOptionsPart(this, right, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		sensorOptionsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(sensorOptionsPart);

		JmxSensorOptionsPart jmxSensorOptionsPart = new JmxSensorOptionsPart(this, right, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		jmxSensorOptionsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(jmxSensorOptionsPart);

		LoggingSensorOptionsPart loggingSensorOptionsPart = new LoggingSensorOptionsPart(this, right, toolkit,
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		loggingSensorOptionsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(loggingSensorOptionsPart);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Manually set focus to form body, otherwise is the tool-bar in focus.
	 */
	@Override
	public void setFocus() {
		getManagedForm().getForm().getBody().setFocus();
	}

}
