package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.shared.cs.ci.eum.EumDomEventSelector;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.dialog.EumDomEventSelectorDialog;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.editor.tooltip.ColumnAwareToolTipSupport;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;

/**
 * Part for configuring the user experience management.
 *
 * @author Jonas Kunz
 *
 */
public class EUMSettingsPart extends SectionPart implements IPropertyListener {

	/**
	 * Form page.
	 */
	private final FormPage formPage;

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * The base URL under which the EUM will operate (e.g. place the script).
	 */
	private Text scriptBaseUrl;

	/**
	 * Switch to disable or enable EUM.
	 */
	private Button eumEnabledButton;

	/**
	 * Table for selecting the EUM modules.
	 */
	private Table modulesTable;

	/**
	 * Table viewer for {@link modulesTable}.
	 */
	private TableViewer modulesTableViewer;

	/**
	 * The base URL under which the EUM will operate (e.g. place the script).
	 */
	private Text relevancyThresholdMS;

	/**
	 * Switch to disable or enable minification.
	 */
	private Button minificationEnabledButton;

	/**
	 * Switch to allow or prevent JS event listeners instrumentation.
	 */
	private Button listenerInstrumentationAllowedButton;

	/**
	 * Allows to configure the Agent to either respect or to ignore Do-Not-Track headers.
	 */
	private Button respectDNTButton;

	/**
	 * Table for modifying the Event selectors.
	 */
	private Table selectorsTable;

	/**
	 * Table viewer for {@link selectorsTable}.
	 */
	private TableViewer selectorsTableViewer;

	/**
	 * Table viewer for {@link selectorsTable}.
	 */
	private List<EumDomEventSelector> selectorsList;

	/**
	 * Button for adding a new DOM Event selector.
	 */
	private Button addSelectorBtn;

	/**
	 * Button for editing the selected DOM Event selector.
	 */
	private Button editSelectorBtn;

	/**
	 * Button for removing the selected DOM Event selector.
	 */
	private Button removeSelectorBtn;

	/**
	 * Default constructor.
	 *
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public EUMSettingsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createPart(getSection(), toolkit);

		// text and description on our own
		getSection().setText("User Experience Monitoring");
		Label label = toolkit.createLabel(getSection(), "Configuration of the User Experience Monitoring");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);
	}

	/**
	 * Creates complete client.
	 *
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createPart(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		// enable / disable button
		toolkit.createLabel(mainComposite, "EUM Enabled:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		eumEnabledButton = toolkit.createButton(mainComposite, "Active", SWT.CHECK);
		eumEnabledButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eumEnabledButton.setSelection(environment.getEumConfig().isEumEnabled());
		createInfoLabel(mainComposite, toolkit, "If activated, the java agent will inject a script (the JS-Agent) into the webpages and monitor client-side performance data.");

		toolkit.createLabel(mainComposite, "Script Base URL:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		scriptBaseUrl = toolkit.createText(mainComposite, environment.getEumConfig().getScriptBaseUrl(), SWT.BORDER | SWT.LEFT);
		scriptBaseUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createInfoLabel(mainComposite, toolkit,
				"The url-prefix under which the agents monitoring scripts will be made accessible to the client.\n"
						+ "This Url must be mapped by at least one servlet or filter, usually the base-url of other static content like scripts or images is a good choice here.\n"
						+ "Also, the entered path must begin and end with a slash.");

		toolkit.createLabel(mainComposite, "Relevancy Threshold (ms):").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		relevancyThresholdMS = toolkit.createText(mainComposite, String.valueOf(environment.getEumConfig().getRelevancyThreshold()), SWT.BORDER | SWT.LEFT);
		relevancyThresholdMS.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createInfoLabel(mainComposite, toolkit,
				"This Threshold given in milliseconds prevents fast-running JS functions to be sent to the CMR, as this improves the performance and the clarity of traces.\n"
						+ "A function execution will only be captured if its executation took at least as long as configured by this threshold.\n"
						+ "Setting the threshold to zero will cause everything to be sent to the CMR.");

		toolkit.createLabel(mainComposite, "Allow JS Listener Instrumentation:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		listenerInstrumentationAllowedButton = toolkit.createButton(mainComposite, "", SWT.CHECK);
		listenerInstrumentationAllowedButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		listenerInstrumentationAllowedButton.setSelection(environment.getEumConfig().isListenerInstrumentationAllowed());
		createInfoLabel(mainComposite, toolkit, "If deactivated, the JS agent will not instrument any JS event listeners to prevent performance issues.");

		toolkit.createLabel(mainComposite, "Respect Do-Not-Track Header:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		respectDNTButton = toolkit.createButton(mainComposite, "", SWT.CHECK);
		respectDNTButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		respectDNTButton.setSelection(environment.getEumConfig().isRespectDNTHeader());
		createInfoLabel(mainComposite, toolkit, "If enabled, users which send a Do-Not-Track header will not be monitored.");

		toolkit.createLabel(mainComposite, "Deliver Minified JS Agent:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		minificationEnabledButton = toolkit.createButton(mainComposite, "", SWT.CHECK);
		minificationEnabledButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		minificationEnabledButton.setSelection(environment.getEumConfig().isAgentMinificationEnabled());
		createInfoLabel(mainComposite, toolkit,
				"If enabled, the clients will receive a minified version of the JS Agent which reduces the size and improves the performance. Should only be disabled for debugging.");

		ValidationControlDecoration<Text> scriptBaseUrlValidation = new ValidationControlDecoration<Text>(scriptBaseUrl, formPage.getManagedForm().getMessageManager()) {
			@Override
			protected boolean validate(Text control) {
				return control.getText().startsWith("/") && control.getText().endsWith("/"); // NOPMD
			}

		};
		scriptBaseUrlValidation.setDescriptionText("The URL must begin and end with a slash");
		scriptBaseUrlValidation.registerListener(SWT.Modify);

		ValidationControlDecoration<Text> relevancyThresholdValidation = new ValidationControlDecoration<Text>(relevancyThresholdMS, formPage.getManagedForm().getMessageManager()) {
			@Override
			protected boolean validate(Text control) {
				try {
					int value = Integer.parseInt(control.getText());
					return value >= 0;
				} catch (NumberFormatException e) {
					return false;
				}
			}

		};
		relevancyThresholdValidation.setDescriptionText("The relevancy threshold must be a duration in milliseconds.");
		relevancyThresholdValidation.registerListener(SWT.Modify);

		createModulesTable(toolkit, mainComposite);
		createSelectorsTable(toolkit, mainComposite, environment.getEumConfig());

		updateCheckedItems();

		// dirty listener
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateEnabledState();
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		eumEnabledButton.addListener(SWT.Selection, dirtyListener);
		scriptBaseUrl.addListener(SWT.Modify, dirtyListener);
		listenerInstrumentationAllowedButton.addListener(SWT.Selection, dirtyListener);
		respectDNTButton.addListener(SWT.Selection, dirtyListener);
		minificationEnabledButton.addListener(SWT.Selection, dirtyListener);
		relevancyThresholdMS.addListener(SWT.Modify, dirtyListener);

		updateEnabledState();
	}

	/**
	 * Disables or enabled the controls depending on wheterh EUM is enalbed or not.
	 */
	private void updateEnabledState() {
		boolean en = eumEnabledButton.getSelection();
		modulesTable.setEnabled(en);
		scriptBaseUrl.setEnabled(en);
		listenerInstrumentationAllowedButton.setEnabled(en);
		respectDNTButton.setEnabled(en);
		minificationEnabledButton.setEnabled(en);
		relevancyThresholdMS.setEnabled(en);

		boolean listenerModuleActive = false;
		for (TableItem item : modulesTableViewer.getTable().getItems()) {
			JSAgentModule moduleInfo = (JSAgentModule) item.getData();
			if (moduleInfo == JSAgentModule.LISTENER_MODULE) {
				listenerModuleActive = item.getChecked();
				break;
			}
		}
		if (listenerModuleActive && en) {
			addSelectorBtn.setEnabled(true);
			selectorsTable.setEnabled(true);
			StructuredSelection structuredSelection = (StructuredSelection) selectorsTableViewer.getSelection();
			if (structuredSelection.isEmpty()) {
				removeSelectorBtn.setEnabled(false);
				editSelectorBtn.setEnabled(false);
			} else {
				removeSelectorBtn.setEnabled(true);
				editSelectorBtn.setEnabled(true);
			}
		} else {
			addSelectorBtn.setEnabled(false);
			selectorsTable.setEnabled(false);
			removeSelectorBtn.setEnabled(false);
			editSelectorBtn.setEnabled(false);
		}
	}

	/**
	 * Builds the JSAgent module table.
	 *
	 * @param toolkit
	 *            the toolkit to use
	 * @param mainComposite
	 *            the main composite contiant all elements of this part
	 */
	private void createModulesTable(FormToolkit toolkit, Composite mainComposite) {

		modulesTable = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		GridData tableLayout = new GridData(SWT.FILL, SWT.FILL, true, false);
		tableLayout.horizontalSpan = 2;
		modulesTable.setLayoutData(tableLayout);
		modulesTable.setHeaderVisible(true);
		modulesTable.setLinesVisible(true);

		modulesTableViewer = new TableViewer(modulesTable);
		ColumnAwareToolTipSupport.enableFor(modulesTableViewer);
		modulesTableViewer.setContentProvider(new ArrayContentProvider());
		modulesTableViewer.setInput(JSAgentModule.values());

		TableViewerColumn activeColumn = new TableViewerColumn(modulesTableViewer, SWT.NONE);
		activeColumn.getColumn().setResizable(false);
		activeColumn.getColumn().setWidth(60);
		activeColumn.getColumn().setText("Active");
		activeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
		});
		activeColumn.getColumn().setToolTipText("Only active modules will be included in the JS Agent and sent to the clients.");

		TableViewerColumn moduleNameColumn = new TableViewerColumn(modulesTableViewer, SWT.NONE);
		moduleNameColumn.getColumn().setResizable(true);
		moduleNameColumn.getColumn().setWidth(250);
		moduleNameColumn.getColumn().setText("Module");
		moduleNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((JSAgentModule) element).getUiName();
			}

			@Override
			public Image getImage(Object element) {
				// TODO add images to modules;
				return null; // ImageFormatter.getSensorConfigImage((ISensorConfig) element);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getToolTipText(Object element) {
				return ((JSAgentModule) element).getUiDescription();
			}

		});
		moduleNameColumn.getColumn().setToolTipText("Module type.");

		modulesTableViewer.refresh();
		createInfoLabel(mainComposite, toolkit, "Select the modules to activate in this environment.");

		modulesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});
	}

	/**
	 * Builds the table for modifying the DOM event selectors.
	 *
	 * @param toolkit
	 *            the toolkit to use
	 * @param mainComposite
	 *            the main composite contiant all elements of this part
	 * @param eumConf
	 *            the current end user monitoring configuration
	 */
	private void createSelectorsTable(FormToolkit toolkit, Composite mainComposite, EndUserMonitoringConfig eumConf) {

		GridData selectorsLabelGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		selectorsLabelGridData.horizontalSpan = 3;
		toolkit.createLabel(mainComposite, "Dom Event Selectors:").setLayoutData(selectorsLabelGridData);

		selectorsTable = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData tableLayout = new GridData(SWT.FILL, SWT.FILL, true, false);
		tableLayout.heightHint = 100;
		tableLayout.horizontalSpan = 2;
		selectorsTable.setLayoutData(tableLayout);
		selectorsTable.setHeaderVisible(true);
		selectorsTable.setLinesVisible(true);

		selectorsTableViewer = new TableViewer(selectorsTable);
		selectorsTableViewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn eventsColumn = new TableViewerColumn(selectorsTableViewer, SWT.NONE);
		eventsColumn.getColumn().setResizable(true);
		eventsColumn.getColumn().setWidth(100);
		eventsColumn.getColumn().setText("Events");
		eventsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EumDomEventSelector) element).getEventsList();
			}
		});

		TableViewerColumn selectorColumn = new TableViewerColumn(selectorsTableViewer, SWT.NONE);
		selectorColumn.getColumn().setResizable(true);
		selectorColumn.getColumn().setWidth(150);
		selectorColumn.getColumn().setText("CSS-Selector");
		selectorColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EumDomEventSelector) element).getSelector();
			}
		});

		TableViewerColumn attributesColumn = new TableViewerColumn(selectorsTableViewer, SWT.NONE);
		attributesColumn.getColumn().setResizable(true);
		attributesColumn.getColumn().setWidth(150);
		attributesColumn.getColumn().setText("Attributes to extract");
		attributesColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EumDomEventSelector) element).getAttributesToExtractList();
			}
		});

		TableViewerColumn ancestorLevelsColumn = new TableViewerColumn(selectorsTableViewer, SWT.NONE);
		ancestorLevelsColumn.getColumn().setResizable(true);
		ancestorLevelsColumn.getColumn().setWidth(100);
		ancestorLevelsColumn.getColumn().setText("Ancestor Levels");
		ancestorLevelsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((EumDomEventSelector) element).getAncestorLevelsToCheck());
			}
		});

		TableViewerColumn relevancyColumn = new TableViewerColumn(selectorsTableViewer, SWT.NONE);
		relevancyColumn.getColumn().setResizable(false);
		relevancyColumn.getColumn().setWidth(100);
		relevancyColumn.getColumn().setText("Always Relevant");
		relevancyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Image getImage(Object element) {
				if (((EumDomEventSelector) element).isAlwaysRelevant()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK);
				} else {
					return null;
				}
			}

		});

		selectorsTableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F2) {
					editSelectedSelector();
				} else if (e.keyCode == SWT.DEL) {
					removeSelectedSelector();
				}
			}
		});
		selectorsTableViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editSelectedSelector();
			}
		});

		// create the buttons

		Composite buttonComposite = toolkit.createComposite(mainComposite, SWT.INHERIT_DEFAULT);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = 30;
		buttonComposite.setLayoutData(gd);

		addSelectorBtn = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		addSelectorBtn.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		addSelectorBtn.setToolTipText("Add");
		addSelectorBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addSelectorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSelector();
			}
		});

		editSelectorBtn = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		editSelectorBtn.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_EDIT));
		editSelectorBtn.setToolTipText("Edit");
		editSelectorBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		editSelectorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editSelectedSelector();
			}
		});

		removeSelectorBtn = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		removeSelectorBtn.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		removeSelectorBtn.setToolTipText("Remove");
		removeSelectorBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeSelectorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedSelector();
			}
		});
		selectorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnabledState();
			}
		});
		selectorsList = new ArrayList<>(eumConf.getEventSelectors());
		selectorsTableViewer.setInput(selectorsList);

		modulesTableViewer.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					if (e.item instanceof TableItem) {
						TableItem item = (TableItem) e.item;
						Object data = item.getData();
						if (data == JSAgentModule.LISTENER_MODULE) {
							updateEnabledState();
						}
					}
				}
			}
		});

		selectorsTableViewer.refresh();
	}

	/**
	 * Updates the selectors table due to a change.
	 *
	 * @param markDirty
	 *            if true, the for mwil lbe marked as dirty, asking for a save.
	 */
	private void updateSelectorsTableInternal(boolean markDirty) {
		selectorsTableViewer.refresh();
		updateEnabledState();
		if (markDirty) {
			markDirty();
		}
	}

	/**
	 * Removes the currently selected dom event selector.
	 */
	private void removeSelectedSelector() {
		StructuredSelection selection = (StructuredSelection) selectorsTableViewer.getSelection();
		for (Object sel : selection.toArray()) {
			selectorsList.remove(sel);
		}
		updateSelectorsTableInternal(true);
	}

	/**
	 * Starts the edit dialog for the currently selected dom event selector.
	 */
	private void editSelectedSelector() {
		StructuredSelection selection = (StructuredSelection) selectorsTableViewer.getSelection();
		Object selected = selection.getFirstElement();
		if (selected instanceof EumDomEventSelector) {
			EumDomEventSelectorDialog dialog = new EumDomEventSelectorDialog(getManagedForm().getForm().getShell(), (EumDomEventSelector) selected);
			if (Window.OK == dialog.open()) {
				updateSelectorsTableInternal(true);
			}
		}
	}

	/**
	 * Starts the dialog for adding a new dom event selector.
	 */
	private void addSelector() {
		EumDomEventSelectorDialog dialog = new EumDomEventSelectorDialog(getManagedForm().getForm().getShell());
		if (Window.OK == dialog.open()) {
			EumDomEventSelector newSelector = dialog.getSelector();
			selectorsList.add(newSelector);
			updateSelectorsTableInternal(true);
			selectorsTableViewer.setSelection(new StructuredSelection(newSelector));
		}
	}

	/**
	 * Updates states of the check boxes next to the elements.
	 */
	private void updateCheckedItems() {
		for (TableItem item : modulesTableViewer.getTable().getItems()) {
			JSAgentModule moduleInfo = (JSAgentModule) item.getData();
			EndUserMonitoringConfig eumConfig = environment.getEumConfig();
			item.setChecked(eumConfig.getActiveModules().contains(String.valueOf(moduleInfo.getIdentifier())));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);
			environment.getEumConfig().setEumEnabled(eumEnabledButton.getSelection());
			environment.getEumConfig().setListenerInstrumentationAllowed(listenerInstrumentationAllowedButton.getSelection());
			environment.getEumConfig().setRespectDNTHeader(respectDNTButton.getSelection());
			environment.getEumConfig().setAgentMinificationEnabled(minificationEnabledButton.getSelection());
			environment.getEumConfig().setScriptBaseUrl(scriptBaseUrl.getText());
			environment.getEumConfig().setRelevancyThreshold(Integer.parseInt(relevancyThresholdMS.getText()));
			environment.getEumConfig().setEventSelectors(selectorsList);
			StringBuilder moduleString = new StringBuilder();
			for (TableItem item : modulesTableViewer.getTable().getItems()) {
				JSAgentModule moduleInfo = (JSAgentModule) item.getData();
				if (item.getChecked()) {
					moduleString.append(moduleInfo.getIdentifier());
				}
			}
			environment.getEumConfig().setActiveModules(moduleString.toString());
			getManagedForm().dirtyStateChanged();
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
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();
		}
	}

	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}