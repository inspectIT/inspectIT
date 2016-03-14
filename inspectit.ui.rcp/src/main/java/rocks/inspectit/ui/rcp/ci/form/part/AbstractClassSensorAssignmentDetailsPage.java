package rocks.inspectit.ui.rcp.ci.form.part;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.InputValidatorControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.validator.FqnWildcardValidator;

/**
 * The abstract class for all the class sensor assignments.
 *
 * @param <E>
 *            type of element being edited
 * @author Ivan Senic
 *
 */
public abstract class AbstractClassSensorAssignmentDetailsPage<E extends AbstractClassSensorAssignment<?>> extends AbstractDetailsPage<E> {

	/**
	 * If the data can be edited.
	 */
	private final boolean canEdit;

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
	 * @param detailsModifiedListener
	 *            listener to inform the master block on changes to the input
	 * @param validationManager
	 *            Validation manager of the master view.
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public AbstractClassSensorAssignmentDetailsPage(IDetailsModifiedListener<E> detailsModifiedListener, AbstractValidationManager<E> validationManager, boolean canEdit) {
		super(detailsModifiedListener, validationManager);
		this.canEdit = canEdit;
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
				"The class/interface that should be monitored including the package name using the standard Java notation (e.g. rocks.inspectit.shared.cs.MyTestClass). The wildcard * can be used to match any length of characters.");

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
	 * Updates controls from the input.
	 */
	@Override
	protected void updateFromInput() {
		interfaceButton.setSelection(false);
		superclassButton.setSelection(false);
		AbstractClassSensorAssignment<?> assignment = getInput();
		if (null != assignment) {
			updateTitle(assignment.getSensorConfigClass());
			interfaceButton.setSelection(assignment.isInterf());
			superclassButton.setSelection(assignment.isSuperclass());
			classText.setText(StringUtils.defaultIfEmpty(assignment.getClassName(), StringUtils.EMPTY));
			annotationText.setText(StringUtils.defaultIfEmpty(assignment.getAnnotation(), StringUtils.EMPTY));
		} else {
			classText.setText("");
			annotationText.setText("");
		}
	}

	/**
	 * Commits changes in page to input.
	 */
	@Override
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
	public void setFocus() {
		classText.setFocus();
	}

}
