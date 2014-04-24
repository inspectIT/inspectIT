package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.EnvironmentSettingsPart;
import info.novatec.inspectit.rcp.ci.form.part.PlatformSensorSelectionPart;
import info.novatec.inspectit.rcp.ci.form.part.ProfileSelectionPart;
import info.novatec.inspectit.rcp.ci.form.part.SensorOptionsPart;
import info.novatec.inspectit.rcp.handlers.AgentMappingHandler.OpenMappingsJob;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Page for general settings.
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
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		// tool-bar
		final EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();
		Action openMappingAction = new Action("Map this Environment to an Agent", IAction.AS_PUSH_BUTTON) {
			public void run() {
				OpenMappingsJob job = new OpenMappingsJob(input.getCmrRepositoryDefinition(), getSite().getWorkbenchWindow().getActivePage());
				job.schedule();
			};
		};
		openMappingAction.setToolTipText("Map Environment to Agent");
		openMappingAction.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_AGENT));

		form.getToolBarManager().add(openMappingAction);
		form.getToolBarManager().update(false);

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

		ProfileSelectionPart profileSelectionPart = new ProfileSelectionPart(this, left, toolkit, Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		profileSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		managedForm.addPart(profileSelectionPart);
		
		EnvironmentSettingsPart environmentSettingsPart = new EnvironmentSettingsPart(this, right, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		environmentSettingsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(environmentSettingsPart);

		PlatformSensorSelectionPart platformSensorSelectionPart = new PlatformSensorSelectionPart(this, right, toolkit, Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		platformSensorSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(platformSensorSelectionPart);

		SensorOptionsPart sensorOptionsPart = new SensorOptionsPart(this, right, toolkit, Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		sensorOptionsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(sensorOptionsPart);
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
