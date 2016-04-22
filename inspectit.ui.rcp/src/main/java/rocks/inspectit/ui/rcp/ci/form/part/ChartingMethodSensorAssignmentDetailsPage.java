package rocks.inspectit.ui.rcp.ci.form.part;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ChartingMethodSensorAssignment;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

/**
 * Details page for the {@link ChartingMethodSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class ChartingMethodSensorAssignmentDetailsPage extends MethodSensorAssignmentDetailsPage {

	/**
	 * Element being displayed.
	 */
	private ChartingMethodSensorAssignment assignment;

	/**
	 * Selection for if charting should be active.
	 */
	private Button chartingButton;

	/**
	 * Composite for the sensor options.
	 */
	private Composite sensorOptionsComposite;

	/**
	 * Constructor.
	 *
	 * @param detailsModifiedListener
	 *            listener to inform the master block on changes to the input
	 * @param validationManager
	 *            validation manager of the master part
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public ChartingMethodSensorAssignmentDetailsPage(IDetailsModifiedListener<AbstractClassSensorAssignment<?>> detailsModifiedListener,
			AbstractValidationManager<AbstractClassSensorAssignment<?>> validationManager, boolean canEdit) {
		super(detailsModifiedListener, validationManager, canEdit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		this.createContents(parent, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createContents(Composite parent, boolean finish) {
		TableWrapLayout parentLayout = new TableWrapLayout();
		parentLayout.topMargin = 5;
		parentLayout.leftMargin = 5;
		parentLayout.rightMargin = 2;
		parentLayout.bottomMargin = 2;
		parentLayout.numColumns = 2;
		parentLayout.makeColumnsEqualWidth = true;
		parent.setLayout(parentLayout);

		FormToolkit toolkit = managedForm.getToolkit();

		// abstract method definition
		super.createContents(parent, false);

		// special sensor definitions
		// section
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		section.setText("Sensor specific options");
		section.marginWidth = 10;
		section.marginHeight = 5;
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		section.setLayoutData(td);

		// main composite
		sensorOptionsComposite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(7, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		sensorOptionsComposite.setLayout(layout);
		section.setClient(sensorOptionsComposite);

		// charting
		toolkit.createLabel(sensorOptionsComposite, "Charting:");
		chartingButton = toolkit.createButton(sensorOptionsComposite, "Yes", SWT.CHECK);
		chartingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		createInfoLabel(sensorOptionsComposite, toolkit,
				"With the charting option it is possible to define what data should be considered as the long-term data available for charting in inspectIT User interface. This data is additionally saved to the database, thus even when the CMR is shutdown or buffer is cleared the data will be available via charts.");

		// listener
		chartingButton.addListener(SWT.Selection, getMarkDirtyListener());

		if (finish) {
			// create help to correctly layout
			TableWrapData twd = new TableWrapData();
			twd.grabHorizontal = true;
			toolkit.createLabel(parent, "", SWT.NONE).setLayoutData(twd);
		}

		if (!isCanEdit()) {
			setEnabled(sensorOptionsComposite, false);
		}
	}

	/**
	 * Updates the display state.
	 */
	@Override
	protected void updateFromInput() {
		super.updateFromInput();
		chartingButton.setSelection(false);
		if (null != assignment) {
			chartingButton.setSelection(assignment.isCharting());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void commitToInput() {
		super.commitToInput();
		assignment.setCharting(chartingButton.getSelection());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ChartingMethodSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setInput(ISelection selection) {
		super.setInput(selection);
		if (!selection.isEmpty()) {
			assignment = (ChartingMethodSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
		} else {
			assignment = null; // NOPMD
		}
	}

	/**
	 * Gets {@link #sensorOptionsComposite}.
	 *
	 * @return {@link #sensorOptionsComposite}
	 */
	protected Composite getSensorOptionsComposite() {
		return sensorOptionsComposite;
	}

}
