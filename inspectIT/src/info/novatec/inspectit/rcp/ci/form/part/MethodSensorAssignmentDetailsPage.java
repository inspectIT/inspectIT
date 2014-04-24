package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;
import info.novatec.inspectit.rcp.validation.validator.FqnWildcardValidator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * The details page for the {@link MethodSensorAssignment}.
 * 
 * @author Ivan Senic
 * 
 */
public class MethodSensorAssignmentDetailsPage extends AbstractClassSensorAssignmentDetailsPage {

	/**
	 * Element being displayed.
	 */
	private MethodSensorAssignment assignment;

	/**
	 * List of parameters.
	 */
	private List<String> parametersList = new ArrayList<>();

	/**
	 * Text box for the method name.
	 */
	private Text methodText;

	/**
	 * Selection for the method.
	 */
	private Button methodButton;

	/**
	 * Selection for the constructor.
	 */
	private Button constructorButton;

	/**
	 * Table viewer for the parameters of the method.
	 */
	private TableViewer parametersTableViewer;

	/**
	 * Selection to include only methods with specified parameters.
	 */
	private Button parametersButton;

	/**
	 * Add parameter button.
	 */
	private Button addParameterButton;

	/**
	 * Remove selected parameter(s) button.
	 */
	private Button removeParameterButton;

	/**
	 * Selection for public modifier.
	 */
	private Button publicButton;

	/**
	 * Selection for protected modifier.
	 */
	private Button protectedButton;

	/**
	 * Selection for private modifier.
	 */
	private Button privateButton;

	/**
	 * Selection for default modifier.
	 */
	private Button defaultButton;

	/**
	 * Constructor.
	 * 
	 * @param masterBlockListener
	 *            listener to inform the master block on changes to the input
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public MethodSensorAssignmentDetailsPage(ISensorAssignmentUpdateListener masterBlockListener, boolean canEdit) {
		super(masterBlockListener, canEdit);
	}

	/**
	 * @return Returns currently displayed {@link MethodSensorAssignment} or <code>null</code> if
	 *         one does not exists.
	 */
	protected MethodSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		this.createContents(parent, true);
	}

	/**
	 * Creates content. Boolean finish defines the content should be finished by adding the
	 * OK/cancel buttons.
	 * 
	 * @param parent
	 *            Parent composite
	 * @param finish
	 *            Defines the content should be finished by adding the OK/cancel buttons.
	 */
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

		// title
		FormText title = createTitle(parent, toolkit);
		TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		twd.colspan = 2;
		title.setLayoutData(twd);

		// section
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Method definition");
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
		addMethodContents(mainComposite);

		if (finish) {
			// create help to correctly layout
			twd = new TableWrapData();
			twd.grabHorizontal = true;
			toolkit.createLabel(parent, "", SWT.NONE).setLayoutData(twd);
		}

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
	protected void addMethodContents(Composite mainComposite) {
		FormToolkit toolkit = managedForm.getToolkit();

		// method name
		// first row
		toolkit.createLabel(mainComposite, "Method:");
		methodButton = toolkit.createButton(mainComposite, "Method name", SWT.RADIO);
		methodButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		methodText = toolkit.createText(mainComposite, "", SWT.BORDER);
		methodText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(mainComposite, toolkit, "The name of the method to be monitored. The wild-card * can be used to match any length of characters.");
		// second row
		toolkit.createLabel(mainComposite, "");
		constructorButton = toolkit.createButton(mainComposite, "Constructor", SWT.RADIO);
		constructorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		toolkit.createLabel(mainComposite, "").setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		// method name validation
		final ValidationControlDecoration<Text> methodValidationDecoration = new ValidationControlDecoration<Text>(methodText, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotEmpty(control.getText());
			}
		};
		methodValidationDecoration.setDescriptionText("Method name must not be empty. Use wildcard * to match any method.");
		methodValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(methodValidationDecoration);
		// listeners
		methodButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				methodText.setEnabled(methodButton.getSelection());
				if (!methodButton.getSelection()) {
					methodText.setText("");
				}
				methodValidationDecoration.executeValidation();
			}
		});

		// parameters
		// first row
		toolkit.createLabel(mainComposite, "Parameters:");
		parametersButton = toolkit.createButton(mainComposite, "Only method/constructor with selected parameters", SWT.CHECK);
		parametersButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(
				mainComposite,
				toolkit,
				"Restriction of the method or constructor. Only the method/constructor with specified fully qualified parameter names is monitored. For primitive type parameters use primitive names like boolean, int, long, etc. Use wildcard * to match any parameter.");
		// second row
		toolkit.createLabel(mainComposite, "");
		// table
		final Table parametersTable = toolkit.createTable(mainComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		GridData parametersGridData = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
		parametersGridData.heightHint = 120;
		parametersTable.setLayoutData(parametersGridData);
		parametersTable.setHeaderVisible(true);
		// table viewer
		parametersTableViewer = new TableViewer(parametersTable);
		parametersTableViewer.setContentProvider(new ArrayContentProvider());
		parametersTableViewer.setInput(parametersList);
		createColumnsForParametersTable();

		Composite parametersComposite = toolkit.createComposite(mainComposite);
		parametersComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		parametersComposite.setLayout(new FillLayout(SWT.VERTICAL));
		addParameterButton = toolkit.createButton(parametersComposite, "", SWT.PUSH);
		addParameterButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		removeParameterButton = toolkit.createButton(parametersComposite, "", SWT.PUSH);
		removeParameterButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		toolkit.createLabel(mainComposite, "");
		// listeners
		parametersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean parametersActive = parametersButton.getSelection();
				parametersTable.setEnabled(parametersActive);
				addParameterButton.setEnabled(parametersActive);
				removeParameterButton.setEnabled(parametersActive);
			}
		});
		addParameterButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				InputDialog inputDialog = new InputDialog(managedForm.getForm().getShell(), "Add Parameter", "Specify fully qualified parameter name:", "", new FqnWildcardValidator(false, true));
				if (inputDialog.open() == Window.OK && StringUtils.isNotBlank(inputDialog.getValue())) {
					parametersList.add(inputDialog.getValue());
					parametersTableViewer.refresh();
					getMarkDirtyListener().handleEvent(event);
				}
			}
		});
		removeParameterButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) parametersTableViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object selectedObject : selection.toArray()) {
						parametersList.remove(selectedObject);
					}
					parametersTableViewer.refresh();
					getMarkDirtyListener().handleEvent(event);
				}
			}
		});
		parametersTableViewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) parametersTableViewer.getSelection();
				if (!selection.isEmpty()) {
					String selected = (String) selection.getFirstElement();
					InputDialog inputDialog = new InputDialog(managedForm.getForm().getShell(), "Edit Parameter", "Specify fully qualified parameter name:", selected, null);
					if (inputDialog.open() == Window.OK && StringUtils.isNotBlank(inputDialog.getValue())) {
						String value = inputDialog.getValue();
						int index = 0;
						for (int i = 0; i < parametersList.size(); i++) {
							// on == because we can have same params in the list
							if (selected == parametersList.get(i)) {
								index = i;
								break;
							}
						}
						parametersList.remove(index);
						parametersList.add(index, value);
						parametersTableViewer.refresh();
						getMarkDirtyListener().handleEvent(event);
					}
				}
			}
		});

		// modifiers
		toolkit.createLabel(mainComposite, "Method visibility:");
		publicButton = toolkit.createButton(mainComposite, "public", SWT.CHECK);
		publicButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		protectedButton = toolkit.createButton(mainComposite, "protected", SWT.CHECK);
		protectedButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		defaultButton = toolkit.createButton(mainComposite, "default", SWT.CHECK);
		defaultButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		privateButton = toolkit.createButton(mainComposite, "private", SWT.CHECK);
		privateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(
				mainComposite,
				toolkit,
				"The additional options of the sensor assignment can include the definition of the method/constructor visibility modifier(s) that should be instrumented. Thus, the methods can be additionally filtered by the visibility modifier that can be: public, protected, private or default");

		ValidationControlDecoration<Button> modifiersControlDecoration = new ValidationControlDecoration<Button>(publicButton, null, this) {
			@Override
			protected boolean validate(Button control) {
				return publicButton.getSelection() || protectedButton.getSelection() || defaultButton.getSelection() || privateButton.getSelection();
			}
		};
		modifiersControlDecoration.setDescriptionText("At least one type of modifiers must be selected.");
		modifiersControlDecoration.registerListener(publicButton, SWT.Selection);
		modifiersControlDecoration.registerListener(protectedButton, SWT.Selection);
		modifiersControlDecoration.registerListener(defaultButton, SWT.Selection);
		modifiersControlDecoration.registerListener(privateButton, SWT.Selection);
		addValidationControlDecoration(modifiersControlDecoration);

		// dirty listener
		methodButton.addListener(SWT.Selection, getMarkDirtyListener());
		methodText.addListener(SWT.Modify, getMarkDirtyListener());
		parametersButton.addListener(SWT.Selection, getMarkDirtyListener());
		publicButton.addListener(SWT.Selection, getMarkDirtyListener());
		protectedButton.addListener(SWT.Selection, getMarkDirtyListener());
		defaultButton.addListener(SWT.Selection, getMarkDirtyListener());
		privateButton.addListener(SWT.Selection, getMarkDirtyListener());
	}

	/**
	 * Column for the table viewer.
	 */
	private void createColumnsForParametersTable() {
		TableViewerColumn paramColumn = new TableViewerColumn(parametersTableViewer, SWT.NONE);
		paramColumn.getColumn().setResizable(true);
		paramColumn.getColumn().setWidth(100);
		paramColumn.getColumn().setText("Parameter FQN");
		paramColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
		paramColumn.getColumn().setToolTipText("Parameter FQN or primitive type name.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateFromInput() {
		super.updateFromInput();
		methodText.setEnabled(false);
		methodText.setText("");
		methodButton.setSelection(false);
		constructorButton.setSelection(false);
		parametersButton.setSelection(false);
		parametersList.clear();
		publicButton.setSelection(false);
		protectedButton.setSelection(false);
		privateButton.setSelection(false);
		defaultButton.setSelection(false);
		MethodSensorAssignment assignment = getInput();
		if (null != assignment) {
			if (assignment.isConstructor()) {
				constructorButton.setSelection(true);
			} else {
				methodText.setEnabled(isCanEdit());
				methodText.setText(getEmptyIfNull(assignment.getMethodName()));
				methodButton.setSelection(true);
			}
			if (null != assignment.getParameters()) {
				parametersButton.setSelection(true);
				parametersTableViewer.getTable().setEnabled(isCanEdit());
				addParameterButton.setEnabled(isCanEdit());
				removeParameterButton.setEnabled(isCanEdit());
				for (String param : assignment.getParameters()) {
					parametersList.add(param);
				}
			} else {
				parametersTableViewer.getTable().setEnabled(false);
				addParameterButton.setEnabled(false);
				removeParameterButton.setEnabled(false);
			}

			publicButton.setSelection(assignment.isPublicModifier());
			protectedButton.setSelection(assignment.isProtectedModifier());
			privateButton.setSelection(assignment.isPrivateModifier());
			defaultButton.setSelection(assignment.isDefaultModifier());
		}
		parametersTableViewer.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void commitToInput() {
		super.commitToInput();
		MethodSensorAssignment assignment = getInput();
		if (null != assignment) {
			assignment.setConstructor(constructorButton.getSelection());
			if (!assignment.isConstructor()) {
				assignment.setMethodName(methodText.getText());
			} else {
				assignment.setMethodName(null);
			}
			if (parametersButton.getSelection()) {
				assignment.setParameters(new ArrayList<>(parametersList));
			} else {
				assignment.setParameters(null);
			}
			assignment.setPublicModifier(publicButton.getSelection());
			assignment.setProtectedModifier(protectedButton.getSelection());
			assignment.setPrivateModifier(privateButton.getSelection());
			assignment.setDefaultModifier(defaultButton.getSelection());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setInput(ISelection selection) {
		if (!selection.isEmpty()) {
			assignment = (MethodSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
		} else {
			assignment = null; // NOPMD
		}
	}

}
