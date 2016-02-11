package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.sensor.ISensorConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.SensorAssignmentMasterBlock.RemoveSelection;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.validation.IControlValidationListener;
import info.novatec.inspectit.rcp.validation.InputValidatorControlDecoration;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;
import info.novatec.inspectit.rcp.validation.validator.FqnWildcardValidator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * The abstract class for all the class sensor assignments.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractClassSensorAssignmentDetailsPage implements IDetailsPage, IControlValidationListener {

	/**
	 * Listener in the master block to inform about the changes in the input editing.
	 */
	private final ISensorAssignmentUpdateListener masterBlockListener;

	/**
	 * If the data can be edited.
	 */
	private boolean canEdit;

	/**
	 * Marker for updating the widgets contents when the selection changes, so that mark dirty is
	 * only fired when changes occur as result of user interaction.
	 */
	private boolean updateInProgress;

	/**
	 * If input is valid.
	 */
	private boolean valid;

	/**
	 * List of {@link ValidationControlDecoration}s.
	 */
	private List<ValidationControlDecoration<?>> validationControlDecorations = new ArrayList<>();

	/**
	 * Managed for part belongs to.
	 */
	protected IManagedForm managedForm;

	/**
	 * Listener that marks dirty on any event.
	 */
	protected Listener markDirtyListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (!updateInProgress) {
				commitToInput();

				AbstractClassSensorAssignment<?> input = getInput();
				if (null != masterBlockListener && null != input) {
					masterBlockListener.sensorAssignmentUpdated(input, true, isValid(), validationControlDecorations);
				}
			}

		}
	};

	/**
	 * Selection for the interface assignment.
	 */
	private Button interfaceButton;

	/**
	 * Selection for the super-class assignment.
	 */
	private Button superclassButton;

	/**
	 * Text box for the FQN of the class/interface.
	 */
	private Text classText;

	/**
	 * Button for searching the classes. Disabled at the moment.
	 */
	private Button classSearchButton;

	/**
	 * Text box for the FQN of the annotation (if any).
	 */
	private Text annotationText;

	/**
	 * Button for searching the annotations. Disabled at the moment.
	 */
	private Button annotationSearchButton;

	/**
	 * {@link FormText} to display title with image and heading text.
	 */
	private FormText title;

	/**
	 * Constructor.
	 * 
	 * @param masterBlockListener
	 *            listener to inform the master block on changes to the input
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public AbstractClassSensorAssignmentDetailsPage(ISensorAssignmentUpdateListener masterBlockListener, boolean canEdit) {
		this.masterBlockListener = masterBlockListener;
		this.canEdit = canEdit;
	}

	/**
	 * @return Returns currently displayed {@link MethodSensorAssignment} or <code>null</code> if
	 *         one does not exists.
	 */
	protected abstract AbstractClassSensorAssignment<?> getInput();

	/**
	 * Sets the input from the given selection.
	 * 
	 * @param selection
	 *            selection
	 */
	protected abstract void setInput(ISelection selection);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();

		// section
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Class definition");
		section.marginWidth = 10;
		section.marginHeight = 5;
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		section.setLayoutData(td);

		// main composite
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(7, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.horizontalSpacing = 10;
		mainComposite.setLayout(layout);
		section.setClient(mainComposite);

		addClassContents(mainComposite);

		if (!isCanEdit()) {
			setEnabled(mainComposite, false);
		}
	}

	/**
	 * Adds content related to class.
	 * 
	 * @param mainComposite
	 *            Composite to create on.
	 */
	protected void addClassContents(Composite mainComposite) {
		FormToolkit toolkit = managedForm.getToolkit();

		// interface & super class
		toolkit.createLabel(mainComposite, "");
		interfaceButton = toolkit.createButton(mainComposite, "Interface", SWT.CHECK);
		interfaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		superclassButton = toolkit.createButton(mainComposite, "Superclass", SWT.CHECK);
		superclassButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		SelectionListener interfaceSuperclassSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (interfaceButton.equals(e.widget) && interfaceButton.getSelection()) {
					superclassButton.setSelection(false);
				} else if (superclassButton.equals(e.widget) && superclassButton.getSelection()) {
					interfaceButton.setSelection(false);
				}
			}
		};
		interfaceButton.addSelectionListener(interfaceSuperclassSelectionListener);
		superclassButton.addSelectionListener(interfaceSuperclassSelectionListener);
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(mainComposite, toolkit,
				"Selecting Interface option all classes implementing specified interface will be instrumented, while selecting Superclass all subclass of specified class will be instrumented.");

		// class name
		toolkit.createLabel(mainComposite, "Fully Qualified Name:");
		classText = toolkit.createText(mainComposite, "", SWT.BORDER);
		classText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		classSearchButton = toolkit.createButton(mainComposite, "", SWT.PUSH);
		classSearchButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SEARCH));
		classSearchButton.setToolTipText("Search for class");
		classSearchButton.setEnabled(false);
		createInfoLabel(
				mainComposite,
				toolkit,
				"The class/interface that should be monitored including the package name using the standard Java notation (e.g. info.novatec.inspectit.MyTestClass). The wildcard * can be used to match any length of characters.");

		// class validation
		ValidationControlDecoration<Text> classValidationDecoration = new InputValidatorControlDecoration(classText, this, new FqnWildcardValidator(false, false));
		classValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(classValidationDecoration);

		// annotation
		toolkit.createLabel(mainComposite, "Annotation:");
		annotationText = toolkit.createText(mainComposite, "", SWT.BORDER);
		annotationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		annotationSearchButton = toolkit.createButton(mainComposite, "", SWT.PUSH);
		annotationSearchButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SEARCH));
		annotationSearchButton.setToolTipText("Search for annotation class");
		annotationSearchButton.setEnabled(false);
		createInfoLabel(
				mainComposite,
				toolkit,
				"Sensor assignment can include the special additional option, where an annotation can be specified and used as additional instrumentation filter. If the annotation is added to the additional options part, the agent will instrument the methods/constructors based on the annotation target:\n\n1. If the annotation target is Class, then all methods from the classes that have the specified annotation will be instrumented.\n2. If the annotation target is Method, then only methods that have the specified annotation will be instrumented.");

		// annotation validation
		ValidationControlDecoration<Text> annotationValidationDecoration = new InputValidatorControlDecoration(annotationText, null, this, new FqnWildcardValidator(true, false));
		annotationValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(annotationValidationDecoration);

		// dirty listener
		interfaceButton.addListener(SWT.Selection, getMarkDirtyListener());
		superclassButton.addListener(SWT.Selection, getMarkDirtyListener());
		classText.addListener(SWT.Modify, getMarkDirtyListener());
		annotationText.addListener(SWT.Modify, getMarkDirtyListener());
	}

	/**
	 * Gets {@link #markDirtyListener}.
	 * 
	 * @return {@link #markDirtyListener}
	 */
	protected Listener getMarkDirtyListener() {
		return markDirtyListener;
	}

	/**
	 * Validates all {@link ValidationControlDecoration} on the page and returns the current valid
	 * state.
	 * 
	 * 
	 * @param executeValidation
	 *            if each {@link #validationControlDecorations} should execute new validation before
	 *            calculating
	 * @return If the all data in the controls are valid.
	 */
	protected boolean checkValid(boolean executeValidation) {
		boolean valid = true;
		for (ValidationControlDecoration<?> decoration : validationControlDecorations) {
			if (executeValidation) {
				decoration.executeValidation();
			}

			if (!decoration.isValid()) {
				valid = false;
				if (!executeValidation) {
					// if we don't need to execute validation of all decorations, then as soon as we
					// find the first invalid we can break out
					break;
				}
			}
		}
		this.valid = valid;
		return valid;
	}

	/**
	 * Gets {@link #valid}.
	 * 
	 * @return {@link #valid}
	 */
	protected boolean isValid() {
		return valid;
	}

	/**
	 * Updates the display state with validation.
	 */
	protected final void update() {
		updateInProgress = true;
		updateFromInput();
		checkValid(true);

		// inform upper part so it receives validation notification
		AbstractClassSensorAssignment<?> input = getInput();
		if (null != input) {
			masterBlockListener.sensorAssignmentUpdated(input, false, isValid(), validationControlDecorations);
		}
		updateInProgress = false;
	}

	/**
	 * Updates controls from the input.
	 */
	protected void updateFromInput() {
		interfaceButton.setSelection(false);
		superclassButton.setSelection(false);
		AbstractClassSensorAssignment<?> assignment = getInput();
		if (null != assignment) {
			updateTitle(assignment.getSensorConfigClass());
			interfaceButton.setSelection(assignment.isInterf());
			superclassButton.setSelection(assignment.isSuperclass());
			classText.setText(getEmptyIfNull(assignment.getClassName()));
			annotationText.setText(getEmptyIfNull(assignment.getAnnotation()));
		} else {
			classText.setText("");
			annotationText.setText("");
		}
	}

	/**
	 * Commits changes in page to input.
	 */
	protected void commitToInput() {
		AbstractClassSensorAssignment<?> assignment = getInput();
		if (null != assignment) {
			assignment.setInterf(interfaceButton.getSelection());
			assignment.setSuperclass(superclassButton.getSelection());
			assignment.setClassName(classText.getText());
			if (StringUtils.isNotBlank(annotationText.getText())) {
				assignment.setAnnotation(annotationText.getText());
			} else {
				assignment.setAnnotation(null);
			}
		}
	}

	/**
	 * @param string
	 *            String
	 * @return Returns "" string if given is <code>null</code>, otherwise returs original
	 *         {@link String}.
	 */
	protected String getEmptyIfNull(String string) {
		if (StringUtils.isNotEmpty(string)) {
			return string;
		} else {
			return "";
		}
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 * 
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param text
	 *            Information text.
	 */
	protected void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

	/**
	 * Create title part based on the sensor configuration being assigned.
	 * 
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @return {@link FormText}
	 */
	protected FormText createTitle(Composite parent, FormToolkit toolkit) {
		title = toolkit.createFormText(parent, false);
		title.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		title.setFont("header", JFaceResources.getBannerFont());
		return title;
	}

	/**
	 * Updates title based on the sensor configuration class.
	 * 
	 * @param sensorConfigClass
	 *            Sensor configuration class.
	 */
	private void updateTitle(Class<? extends ISensorConfig> sensorConfigClass) {
		String titleText = TextFormatter.getSensorConfigName(sensorConfigClass);
		Image titleImage = ImageFormatter.getSensorConfigImage(sensorConfigClass);
		title.setText("<form><p> <img href=\"titleImage\"/> <span color=\"header\" font=\"header\">" + titleText + "</span></p></form>", true, false);
		title.setImage("titleImage", titleImage);
	}

	/**
	 * Adds the {@link ValidationControlDecoration} to the list of the decorations. This list is
	 * used for validating if the complete input on the page is correct.
	 * 
	 * @param validationControlDecoration
	 *            {@link ValidationControlDecoration}.
	 */
	protected void addValidationControlDecoration(ValidationControlDecoration<?> validationControlDecoration) {
		validationControlDecorations.add(validationControlDecoration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		// just call is valid
		checkValid(false);
	}

	/**
	 * Gets {@link #canEdit}.
	 * 
	 * @return {@link #canEdit}
	 */
	protected boolean isCanEdit() {
		return canEdit;
	}

	/**
	 * Helper method to enable/disable all children of a composite.
	 * 
	 * @param composite
	 *            Composite
	 * @param enabled
	 *            true or false
	 */
	protected void setEnabled(Composite composite, boolean enabled) {
		for (Control child : composite.getChildren()) {
			if (child instanceof Composite) {
				setEnabled((Composite) child, enabled);
			} else if (child instanceof Label) {
				// don't disable labels, so we have tool-tip always
				continue;
			} else {
				child.setEnabled(enabled);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void selectionChanged(IFormPart part, ISelection selection) {
		if (selection instanceof RemoveSelection) {
			boolean currentlyEdited = false;
			for (Object element : ((RemoveSelection) selection).toList()) {
				if (element == getInput()) {
					currentlyEdited = true;
					break;
				}
			}
			if (currentlyEdited) {
				setInput(StructuredSelection.EMPTY);
				update();
			}
		} else {
			setInput(selection);
			update();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The details page never reports dirty state.
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		classText.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
