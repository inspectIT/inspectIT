package rocks.inspectit.ui.rcp.ci.form.part;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.InvocationStartMethodSensorAssignment;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;

/**
 * Details page for the {@link InvocationStartMethodSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class InvocationStartSensorAssignmentDetailsPage extends MethodSensorAssignmentDetailsPage {

	/**
	 * Element being displayed.
	 */
	private InvocationStartMethodSensorAssignment assignment;

	/**
	 * Selection for the invocation to be started or not.
	 */
	private Button startInvocationButton;

	/**
	 * Text box for the minimum time of the invocation to be send to the CMR.
	 */
	private Text minDurationText;

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
	public InvocationStartSensorAssignmentDetailsPage(IDetailsModifiedListener<AbstractClassSensorAssignment<?>> detailsModifiedListener,
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

		// starts invocation
		toolkit.createLabel(sensorOptionsComposite, "Starts invocation:");
		startInvocationButton = toolkit.createButton(sensorOptionsComposite, "Yes", SWT.CHECK);
		toolkit.createLabel(sensorOptionsComposite, "Min duration:", SWT.RIGHT).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		minDurationText = toolkit.createText(sensorOptionsComposite, "", SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd.widthHint = 50;
		minDurationText.setLayoutData(gd);
		toolkit.createLabel(sensorOptionsComposite, "");
		createInfoLabel(sensorOptionsComposite, toolkit,
				"Defines if method should start an invocation. Minimum duration defines the minimum time in milliseconds an invocation has to consume in order to be saved and transmitted to the server.");
		// validation
		// method validation
		final ValidationControlDecoration<Text> minDurationValidationDecoration = new ValidationControlDecoration<Text>(minDurationText, null, this) {
			@Override
			protected boolean validate(Text control) {
				if (StringUtils.isNotEmpty(control.getText())) {
					try {
						return Long.parseLong(control.getText()) > 0;
					} catch (NumberFormatException e) {
						return false;
					}
				} else {
					return true;
				}
			}
		};
		minDurationValidationDecoration.setDescriptionText("Value must be positive amount of milliseconds.");
		minDurationValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(minDurationValidationDecoration);

		// listener
		startInvocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				minDurationText.setEnabled(startInvocationButton.getSelection());
				minDurationValidationDecoration.executeValidation();
			}
		});

		// dirty listener
		startInvocationButton.addListener(SWT.Selection, getMarkDirtyListener());
		minDurationText.addListener(SWT.Modify, getMarkDirtyListener());


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

		startInvocationButton.setSelection(false);
		minDurationText.setEnabled(false);
		minDurationText.setText("");
		if (null != assignment) {
			if (assignment.isStartsInvocation()) {
				startInvocationButton.setSelection(true);
				minDurationText.setEnabled(isCanEdit());
				if (0 != assignment.getMinInvocationDuration()) {
					minDurationText.setText(String.valueOf(assignment.getMinInvocationDuration()));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void commitToInput() {
		super.commitToInput();
		assignment.setStartsInvocation(startInvocationButton.getSelection());
		if (startInvocationButton.getSelection()) {
			String minDuration = minDurationText.getText();
			if (StringUtils.isNotBlank(minDuration)) {
				try {
					assignment.setMinInvocationDuration(Long.parseLong(minDuration));
				} catch (NumberFormatException e) {
					assignment.setMinInvocationDuration(0L);
				}
			} else {
				assignment.setMinInvocationDuration(0L);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected InvocationStartMethodSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setInput(ISelection selection) {
		super.setInput(selection);
		if (!selection.isEmpty()) {
			assignment = (InvocationStartMethodSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
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
