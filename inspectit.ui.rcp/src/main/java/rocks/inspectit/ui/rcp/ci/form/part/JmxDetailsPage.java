package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.dialog.KeyValueInputDialog;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.InputValidatorControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.validator.RegularExpressionValidator;

/**
 * Details page for the {@link JmxBeanSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class JmxDetailsPage extends AbstractDetailsPage<JmxBeanSensorAssignment> {

	/**
	 * Assignment being edited.
	 */
	private JmxBeanSensorAssignment assignment;

	/**
	 * Map to store object name parameters.
	 */
	private final Map<String, String> parametersMap = new HashMap<String, String>();

	/**
	 * List of attributes to monitor.
	 */
	private final Set<String> attributesSet = new HashSet<>();

	/**
	 * Text box for the domain.
	 */
	private Text domainText;

	/**
	 * Table viewer for the object name parameters.
	 */
	private TableViewer propertiesTableViewer;

	/**
	 * Add object name parameter button.
	 */
	private Button addPropertyButton;

	/**
	 * Remove object name parameter button.
	 */
	private Button removePropertyButton;

	/**
	 * Monitor all attributes check-box.
	 */
	private Button allAttributesButton;

	/**
	 * Table viewer for the attributes.
	 */
	private TableViewer attributesTableViewer;

	/**
	 * Add attribute button.
	 */
	private Button addAttributeButton;

	/**
	 * Remove attribute button.
	 */
	private Button removeAttributeButton;

	/**
	 * Default constructor.
	 *
	 * @param detailsModifiedListener
	 *            details modified listener,
	 * @param validationManager
	 *            master validation manager to report to
	 */
	public JmxDetailsPage(IDetailsModifiedListener<JmxBeanSensorAssignment> detailsModifiedListener, AbstractValidationManager<JmxBeanSensorAssignment> validationManager) {
		super(detailsModifiedListener, validationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();

		TableWrapLayout parentLayout = new TableWrapLayout();
		parentLayout.topMargin = 5;
		parentLayout.leftMargin = 5;
		parentLayout.rightMargin = 2;
		parentLayout.bottomMargin = 2;
		parentLayout.numColumns = 2;
		parentLayout.makeColumnsEqualWidth = true;
		parent.setLayout(parentLayout);

		// title
		String titleText = TextFormatter.getSensorConfigName(JmxSensorConfig.class);
		Image titleImage = ImageFormatter.getSensorConfigImage(JmxSensorConfig.class);
		FormText title = toolkit.createFormText(parent, false);
		title.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		title.setFont("header", JFaceResources.getBannerFont());
		title.setText("<form><p> <img href=\"titleImage\"/> <span color=\"header\" font=\"header\">" + titleText + "</span></p></form>", true, false);
		title.setImage("titleImage", titleImage);

		TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		twd.colspan = 2;
		title.setLayoutData(twd);

		// object name section
		Section objectNameSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		objectNameSection.setText("MBean Object Name");
		objectNameSection.marginWidth = 10;
		objectNameSection.marginHeight = 5;
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		objectNameSection.setLayoutData(td);

		// object name composite
		Composite objectNameComposite = toolkit.createComposite(objectNameSection);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.horizontalSpacing = 10;
		objectNameComposite.setLayout(layout);
		objectNameSection.setClient(objectNameComposite);

		// domain
		toolkit.createLabel(objectNameComposite, "Domain:");
		domainText = toolkit.createText(objectNameComposite, "", SWT.BORDER);
		domainText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		createInfoLabel(objectNameComposite, toolkit,
				"The domain of the object name. If the domain includes at least one occurrence of the wildcard characters asterisk (*) or question mark (?), then the object name is a pattern. The asterisk matches any sequence of zero or more characters, while the question mark matches any single character. If the domain is empty, it will be replaced in certain contexts by the default domain of the MBean server in which the ObjectName is used.");
		// domain name validation
		ValidationControlDecoration<Text> domainValidationDecoration = new InputValidatorControlDecoration(domainText, this,
				new RegularExpressionValidator("[a-zA-Z0-9\\.\\?\\*]*", "Domain name is not valid. Use wildcard characters asterisk (*) to match all domains.", false));
		domainValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(domainValidationDecoration);

		// object name properties
		toolkit.createLabel(objectNameComposite, "Properties:").setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		// table
		final Table propertiesTable = toolkit.createTable(objectNameComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		GridData propertiesGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		propertiesGridData.heightHint = 120;
		propertiesTable.setLayoutData(propertiesGridData);
		propertiesTable.setHeaderVisible(true);
		// table viewer
		propertiesTableViewer = new TableViewer(propertiesTable);
		propertiesTableViewer.setContentProvider(new ArrayContentProvider());
		propertiesTableViewer.setInput(parametersMap.entrySet());
		createColumnsForParametersTable();

		// buttons
		Composite propertiesComposite = toolkit.createComposite(objectNameComposite);
		propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		propertiesComposite.setLayout(new FillLayout(SWT.VERTICAL));
		addPropertyButton = toolkit.createButton(propertiesComposite, "", SWT.PUSH);
		addPropertyButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		removePropertyButton = toolkit.createButton(propertiesComposite, "", SWT.PUSH);
		removePropertyButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		// listeners
		final IInputValidator propertyValidator = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (StringUtils.isEmpty(newText)) {
					return "Both key and value of the object name property are required.";
				}
				return null;
			}
		};
		addPropertyButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				KeyValueInputDialog inputDialog = new KeyValueInputDialog(managedForm.getForm().getShell(), "Object Name Properties", "Define object name property.", "", propertyValidator, "",
						propertyValidator);
				inputDialog.open();
				if (Dialog.OK == inputDialog.getReturnCode()) {
					parametersMap.put(inputDialog.getKey(), inputDialog.getValue());
					propertiesTableViewer.refresh();
					markDirtyListener.handleEvent(event);
				}
			}
		});
		removePropertyButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) propertiesTableViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object selectedObject : selection.toArray()) {
						if (selectedObject instanceof Entry) {
							parametersMap.remove(((Entry<?, ?>) selectedObject).getKey());
						}
					}
					propertiesTableViewer.refresh();
					markDirtyListener.handleEvent(event);
				}
			}
		});
		propertiesTableViewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) propertiesTableViewer.getSelection();
				if (!selection.isEmpty()) {
					Entry<String, String> selected = (Entry<String, String>) selection.getFirstElement();
					KeyValueInputDialog inputDialog = new KeyValueInputDialog(managedForm.getForm().getShell(), "Object Name Properties", "Edit object name property.", selected.getKey(),
							propertyValidator, selected.getValue(), propertyValidator);
					inputDialog.open();
					if (Dialog.OK == inputDialog.getReturnCode()) {
						parametersMap.remove(selected.getKey());
						parametersMap.put(inputDialog.getKey(), inputDialog.getValue());
						propertiesTableViewer.refresh();
						markDirtyListener.handleEvent(event);
					}
				}
			}
		});

		Label infoLabel = createInfoLabel(objectNameComposite, toolkit,
				"The key properties are an unordered set of keys and associated values.\nEach key is a nonempty string of characters which may not contain any of the characters comma (,), equals (=), colon, asterisk, or question mark. The same key may not occur twice in a given ObjectName.\nEach value associated with a key is a string of characters that is either unquoted or quoted. An unquoted value is a possibly empty string of characters which may not contain any of the characters comma, equals, colon, or quote. If the unquoted value contains at least one occurrence of the wildcard characters asterisk or question mark, then the object name is a property value pattern. The asterisk matches any sequence of zero or more characters, while the question mark matches any single character.");
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		// attributes section
		Section attributesSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		attributesSection.setText("Attributes");
		attributesSection.marginWidth = 10;
		attributesSection.marginHeight = 5;
		td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		attributesSection.setLayoutData(td);

		// attributes composite
		Composite attributesComposite = toolkit.createComposite(attributesSection);
		layout = new GridLayout(3, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.horizontalSpacing = 10;
		attributesComposite.setLayout(layout);
		attributesSection.setClient(attributesComposite);

		allAttributesButton = toolkit.createButton(attributesComposite, "Monitor all attributes", SWT.CHECK);
		allAttributesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		createInfoLabel(attributesComposite, toolkit, "Names of the attributes of the MBean to monitor.");

		// attributes table
		final Table attributesTable = toolkit.createTable(attributesComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		GridData attributesGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		attributesGridData.heightHint = 120;
		attributesTable.setLayoutData(attributesGridData);
		attributesTable.setHeaderVisible(true);
		// table viewer
		attributesTableViewer = new TableViewer(attributesTable);
		attributesTableViewer.setContentProvider(new ArrayContentProvider());
		attributesTableViewer.setInput(attributesSet);
		createColumnsForAttributesTable();
		// decoration
		final ValidationControlDecoration<Table> nonEmptyAttributesDecoration = new ValidationControlDecoration<Table>(attributesTable, this) {
			@Override
			protected boolean validate(Table control) {
				if (!allAttributesButton.getSelection() && attributesSet.isEmpty()) {
					return false;
				}
				return true;
			}
		};
		nonEmptyAttributesDecoration.setDescriptionText("At least one attribute must be defined if the monitor all attributes is not selected.");
		addValidationControlDecoration(nonEmptyAttributesDecoration);

		Composite attributesButtonsComposite = toolkit.createComposite(attributesComposite);
		attributesButtonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		attributesButtonsComposite.setLayout(new FillLayout(SWT.VERTICAL));
		addAttributeButton = toolkit.createButton(attributesButtonsComposite, "", SWT.PUSH);
		addAttributeButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		removeAttributeButton = toolkit.createButton(attributesButtonsComposite, "", SWT.PUSH);
		removeAttributeButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		toolkit.createLabel(attributesComposite, "");
		// listeners
		allAttributesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean attributesActive = allAttributesButton.getSelection();
				attributesTable.setEnabled(!attributesActive);
				addAttributeButton.setEnabled(!attributesActive);
				removeAttributeButton.setEnabled(!attributesActive);
				nonEmptyAttributesDecoration.executeValidation();
			}
		});
		final IInputValidator attributeNameValidatior = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (StringUtils.isEmpty(newText)) {
					return "Name of the attribute is required";
				}
				return null;
			}
		};
		addAttributeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				InputDialog inputDialog = new InputDialog(managedForm.getForm().getShell(), "Add Attribute", "Specify attribute name:", "", attributeNameValidatior);
				if (inputDialog.open() == Window.OK && StringUtils.isNotBlank(inputDialog.getValue())) {
					attributesSet.add(inputDialog.getValue());
					attributesTableViewer.refresh();
					nonEmptyAttributesDecoration.executeValidation();
					markDirtyListener.handleEvent(event);
				}
			}
		});
		removeAttributeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) attributesTableViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object selectedObject : selection.toArray()) {
						attributesSet.remove(selectedObject);
					}
					attributesTableViewer.refresh();
					nonEmptyAttributesDecoration.executeValidation();
					markDirtyListener.handleEvent(event);
				}
			}
		});
		attributesTableViewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) attributesTableViewer.getSelection();
				if (!selection.isEmpty()) {
					String selected = (String) selection.getFirstElement();
					InputDialog inputDialog = new InputDialog(managedForm.getForm().getShell(), "Edit Attribute", "Specify attribute name:", selected, attributeNameValidatior);
					if (inputDialog.open() == Window.OK && StringUtils.isNotBlank(inputDialog.getValue())) {
						String value = inputDialog.getValue();
						attributesSet.remove(selected);
						attributesSet.add(value);
						attributesTableViewer.refresh();
						nonEmptyAttributesDecoration.executeValidation();
						markDirtyListener.handleEvent(event);
					}
				}
			}
		});

		// dirty listener
		domainText.addListener(SWT.Modify, markDirtyListener);
		allAttributesButton.addListener(SWT.Selection, markDirtyListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		domainText.setFocus();
	}

	/**
	 * Updates controls from the input.
	 */
	@Override
	protected void updateFromInput() {
		domainText.setText("");
		parametersMap.clear();
		attributesSet.clear();
		allAttributesButton.setSelection(true);
		JmxBeanSensorAssignment assignment = getInput();
		if (null != assignment) {
			domainText.setText(StringUtils.defaultString(assignment.getDomain()));
			if (MapUtils.isNotEmpty(assignment.getObjectNameParameters())) {
				parametersMap.putAll(assignment.getObjectNameParameters());
			}
			if (CollectionUtils.isNotEmpty(assignment.getAttributes())) {
				allAttributesButton.setSelection(false);
				attributesSet.addAll(assignment.getAttributes());
				attributesTableViewer.getTable().setEnabled(true);
				addAttributeButton.setEnabled(true);
				removeAttributeButton.setEnabled(true);
			} else {
				allAttributesButton.setSelection(true);
				attributesTableViewer.getTable().setEnabled(false);
				addAttributeButton.setEnabled(false);
				removeAttributeButton.setEnabled(false);
			}

		}
		propertiesTableViewer.refresh();
		attributesTableViewer.refresh();
	}

	/**
	 * Commits changes in page to input.
	 */
	@Override
	protected void commitToInput() {
		JmxBeanSensorAssignment assignment = getInput();
		if (null != assignment) {
			assignment.setDomain(domainText.getText());
			assignment.setObjectNameParameters(new HashMap<>(parametersMap));
			if (allAttributesButton.getSelection()) {
				assignment.setAttributes(Collections.<String> emptySet());
			} else {
				assignment.setAttributes(new HashSet<>(attributesSet));
			}
		}
	}

	/**
	 * Creates columns for the parameter table.
	 */
	private void createColumnsForParametersTable() {
		TableViewerColumn keyColumn = new TableViewerColumn(propertiesTableViewer, SWT.NONE);
		keyColumn.getColumn().setResizable(true);
		keyColumn.getColumn().setWidth(100);
		keyColumn.getColumn().setText("Key");
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				if (element instanceof Entry) {
					return ((Entry<String, String>) element).getKey();
				}
				return null;
			}
		});

		TableViewerColumn valueColumn = new TableViewerColumn(propertiesTableViewer, SWT.NONE);
		valueColumn.getColumn().setResizable(true);
		valueColumn.getColumn().setWidth(200);
		valueColumn.getColumn().setText("Value");
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				if (element instanceof Entry) {
					return ((Entry<String, String>) element).getValue();
				}
				return null;
			}
		});
	}

	/**
	 * Creates columns for the {@link #attributesTableViewer} table.
	 */
	private void createColumnsForAttributesTable() {
		TableViewerColumn nameColumn = new TableViewerColumn(attributesTableViewer, SWT.NONE);
		nameColumn.getColumn().setResizable(true);
		nameColumn.getColumn().setWidth(200);
		nameColumn.getColumn().setText("Name");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});

	}

	/**
	 * @return Returns the current edited element or null if currently no editing is on.
	 */
	@Override
	protected JmxBeanSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * Sets input from selection.
	 *
	 * @param selection
	 *            Selection
	 */
	@Override
	protected void setInput(ISelection selection) {
		if (!selection.isEmpty()) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			if (selected instanceof JmxBeanSensorAssignment) {
				assignment = (JmxBeanSensorAssignment) selected;
			} else {
				assignment = null; // NOPMD
			}
		} else {
			assignment = null; // NOPMD
		}
	}
}