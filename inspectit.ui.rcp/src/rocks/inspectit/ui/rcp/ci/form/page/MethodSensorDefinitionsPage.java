package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.SensorAssignmentMasterBlock;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Page for method sensor definitions.
 * 
 * @author Ivan Senic
 * 
 */
public class MethodSensorDefinitionsPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = MethodSensorDefinitionsPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Sensor Definitions";

	/**
	 * Method sensor master block.
	 */
	private SensorAssignmentMasterBlock sensorAssignmentMasterBlock;

	/**
	 * Default constructor.
	 * 
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public MethodSensorDefinitionsPage(FormEditor editor) {
		super(editor, ID, TITLE);
		this.sensorAssignmentMasterBlock = new SensorAssignmentMasterBlock(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		sensorAssignmentMasterBlock.createContent(managedForm);
	}

}
