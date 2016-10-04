package rocks.inspectit.ui.rcp.ci.wizard.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.cmr.service.IInfluxDBService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;

/**
 * Wizard Page for the definition of the data source for alerting processing.
 *
 * @author Alexander Wert
 *
 */
public class AlertSourceDefinitionWizardPage extends WizardPage {

	/**
	 * Title of the wizard page.
	 */
	private static final String TITLE = "Alert Definition Source";

	/**
	 * Default message of the wizard page.
	 */
	private static final String DEFAULT_MESSAGE = "Define the name and source for the new alert definition.";

	/**
	 * Description text for the name input field.
	 */
	private static final String NAME_INFO_TEXT = "Specify the name of the alerting rule.\nThis name must be unique.";

	/**
	 * Description text for the metric input fields.
	 */
	private static final String METRIC_INFO_TEXT = "Specify the target metric on which this alerting rule shall be applied.\n"
			+ "A metric is determined by the name of the measurement in the corresponding influxDB and the field name.";

	/**
	 * Description text for the tags input fields.
	 */
	private static final String TAGS_INFO_TEXT = "Specify the scope of the target metric\n" + "by narrowing it down with additional tag specifications.\n"
			+ "Tags are specified by a tag key and a tag value.";

	/**
	 * Number of layout columns in the main composite of this page.
	 */
	private static final int NUM_LAYOUT_COLUMNS = 5;

	/**
	 * Initial name of the alerting definition (used for editing mode).
	 */
	private String initialName;

	/**
	 * Initial measurement name (used for editing mode).
	 */
	private String initialMeasurement;

	/**
	 * Initial field name (used for editing mode).
	 */
	private String initialField;

	/**
	 * Initial key-value pairs for the tags (used for editing mode).
	 */
	private Map<String, String> initialTags;

	/**
	 * Name box.
	 */
	private Text nameBox;

	/**
	 * Measurement box.
	 */
	private Combo measurementBox;

	/**
	 * Field box.
	 */
	private Combo fieldBox;

	/**
	 * List of existing items defining which names are already taken.
	 */
	private final Collection<String> existingItems;

	/**
	 * A list of tag UI components currently created.
	 */
	private final List<TagKeyValueUIComponent> tagComponents = new ArrayList<>();

	/**
	 * Listener that checks whether page can be completed.
	 */
	private Listener pageCompletionListener;

	/**
	 * {@link IInfluxDBService} instance used to retrieve values for different source fields
	 * (measurement, field, tag keys and tag values).
	 */
	private IInfluxDBService influxService;

	/**
	 * Constructor.
	 *
	 * @param influxService
	 *            {@link IInfluxDBService} instance used to retrieve values for different source
	 *            fields (measurement, field, tag keys and tag values).
	 * @param existingNames
	 *            List of existing items defining which names are already taken.
	 */
	public AlertSourceDefinitionWizardPage(IInfluxDBService influxService, Collection<String> existingNames) {
		this(influxService, existingNames, null, null, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param influxService
	 *            {@link IInfluxDBService} instance used to retrieve values for different source
	 *            fields (measurement, field, tag keys and tag values).
	 * @param existingNames
	 *            List of existing items defining which names are already taken.
	 * @param name
	 *            Initial name of the alerting definition (used for editing mode).
	 * @param measurement
	 *            Initial measurement name (used for editing mode).
	 * @param field
	 *            Initial field name (used for editing mode).
	 * @param tags
	 *            Initial key-value pairs for the tags (used for editing mode).
	 */
	public AlertSourceDefinitionWizardPage(IInfluxDBService influxService, Collection<String> existingNames, String name, String measurement, String field, Map<String, String> tags) {
		super(TITLE);
		setTitle(TITLE);
		setMessage(DEFAULT_MESSAGE);
		this.influxService = influxService;
		if (null != existingNames) {
			existingItems = existingNames;
		} else {
			existingItems = Collections.emptyList();
		}
		this.initialName = name;
		this.initialMeasurement = measurement;
		this.initialField = field;
		this.initialTags = tags;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		// create main composite
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		final Composite main = new Composite(scrolledComposite, SWT.NONE);
		main.setLayout(new GridLayout(NUM_LAYOUT_COLUMNS, false));

		// create name controls
		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Name:");
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, NUM_LAYOUT_COLUMNS - 2, 1));
		Label infoLabelName = new Label(main, SWT.RIGHT);
		infoLabelName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelName.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelName.setToolTipText(NAME_INFO_TEXT);

		// create measurement controls
		Label measurementLabel = new Label(main, SWT.LEFT);
		measurementLabel.setText("Measurement:");
		measurementLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		measurementBox = new Combo(main, SWT.BORDER | SWT.DROP_DOWN);
		measurementBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		List<String> measurements = influxService.getMeasurements();
		if (null != measurements) {
			measurementBox.setItems(measurements.toArray(new String[measurements.size()]));
		}

		// create field controls
		Label fieldLabel = new Label(main, SWT.LEFT);
		fieldLabel.setText("Field:");
		fieldLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fieldBox = new Combo(main, SWT.BORDER | SWT.DROP_DOWN);
		fieldBox.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, NUM_LAYOUT_COLUMNS - 4, 1));
		Label infoLabelMetric = new Label(main, SWT.RIGHT);
		infoLabelMetric.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelMetric.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelMetric.setToolTipText(METRIC_INFO_TEXT);

		// create heading for tags section
		FormText headingText = new FormText(main, SWT.NONE);
		headingText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, NUM_LAYOUT_COLUMNS - 1, 1));
		headingText.setFont("header", JFaceResources.getBannerFont());
		headingText.setText("<form><p><span color=\"header\" font=\"header\">Tag Specifications</span></p></form>", true, false);
		Label infoLabelTags = new Label(main, SWT.RIGHT);
		infoLabelTags.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelTags.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelTags.setToolTipText(TAGS_INFO_TEXT);

		setupListeners();
		initContents(main);

		// create add button for new tag rows
		final FormText addText = new FormText(main, SWT.NONE);
		addText.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, NUM_LAYOUT_COLUMNS, 1));
		addText.setText("<form><p>Add tag specification ... <a href=\"delete\"><img href=\"addImg\" /></a></p></form>", true, false);
		addText.setImage("addImg", InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		addText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TagKeyValueUIComponent tagComponent = new TagKeyValueUIComponent(main);
				addText.moveBelow(tagComponent.deleteText);
				tagComponents.add(tagComponent);
				main.layout(true, true);
				scrolledComposite.setMinSize(main.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				pageCompletionListener.handleEvent(null);
			}
		});

		// redraw and finalize main composite setup
		main.layout();
		scrolledComposite.setMinSize(main.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(scrolledComposite);
		scrolledComposite.setContent(main);
	}

	/**
	 * Sets the message based on the page content.
	 */
	protected void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the name entered!", ERROR);
			return;
		}

		if (alreadyExists(getAlertingDefinitionName())) {
			setMessage("An alert definition with this name already exists!", ERROR);
			return;
		}

		if (measurementBox.getText().isEmpty()) {
			setMessage("Measurement must not be empty!", ERROR);
			return;
		}

		if (fieldBox.getText().isEmpty()) {
			setMessage("Field must not be empty!", ERROR);
			return;
		}

		for (TagKeyValueUIComponent tagComponent : tagComponents) {
			if (tagComponent.getTagKey().isEmpty()) {
				setMessage("Tag keys must not be empty!", ERROR);
				return;
			}
			if (tagComponent.getTagValue().isEmpty()) {
				setMessage("Tag value for key '" + tagComponent.getTagKey() + "' must not be empty!", ERROR);
				return;
			}
		}
		setMessage(DEFAULT_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (nameBox.getText().isEmpty() || alreadyExists(nameBox.getText())) {
			return false;
		}

		if (measurementBox.getText().isEmpty()) {
			return false;
		}

		if (fieldBox.getText().isEmpty()) {
			return false;
		}

		for (TagKeyValueUIComponent tagComponent : tagComponents) {
			if (tagComponent.getTagKey().isEmpty() || tagComponent.getTagValue().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the specified name for the {@link AlertingDefinition}.
	 *
	 * @return Returns the specified name for the {@link AlertingDefinition}.
	 */
	public String getAlertingDefinitionName() {
		return nameBox.getText();
	}

	/**
	 * Returns the name of the target influxDB measurement.
	 *
	 * @return Returns the name of the target influxDB measurement.
	 */
	public String getMeasurement() {
		return measurementBox.getText();
	}

	/**
	 * Returns the name of the target influxDB field.
	 *
	 * @return Returns the name of the target influxDB field.
	 */
	public String getField() {
		return fieldBox.getText();
	}

	/**
	 * Returns the key-value pairs for the specification of the target time series.
	 *
	 * @return Returns the key-value pairs for the specification of the target time series.
	 */
	public Map<String, String> getTags() {
		Map<String, String> map = new HashMap<>();
		for (TagKeyValueUIComponent tagComponent : tagComponents) {
			map.put(tagComponent.getTagKey(), tagComponent.getTagValue());
		}
		return map;
	}

	/**
	 * Sets up control listeners.
	 */
	private void setupListeners() {
		pageCompletionListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				setPageMessage();
			}
		};

		Listener measurementChangedListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateFieldOptions();
				for (TagKeyValueUIComponent tagComponent : tagComponents) {
					tagComponent.updateTagKeyOptions();
				}
			}
		};

		nameBox.addListener(SWT.Modify, pageCompletionListener);
		measurementBox.addListener(SWT.Modify, pageCompletionListener);
		fieldBox.addListener(SWT.Modify, pageCompletionListener);
		measurementBox.addListener(SWT.Modify, measurementChangedListener);
	}

	/**
	 * Indicates whether an element with such a name already exists.
	 *
	 * @param name
	 *            name to check
	 * @return true, if an element with the same name already exists.
	 */
	private boolean alreadyExists(String name) {
		for (String item : existingItems) {
			if (item.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the selection options of the Field drop-down box.
	 */
	private void updateFieldOptions() {
		String currentText = fieldBox.getText();
		List<String> fields = influxService.getFields(measurementBox.getText());
		if (null != fields) {
			fieldBox.setItems(fields.toArray(new String[fields.size()]));
		}
		fieldBox.setText(currentText);
	}

	/**
	 * Initializes the contents of all fields if there are initial values.
	 *
	 * @param main
	 *            The main composite where to attach tag UI components to.
	 */
	private void initContents(Composite main) {
		if (null != initialName) {
			nameBox.setText(initialName);
		}

		if (null != initialMeasurement) {
			measurementBox.setText(initialMeasurement);
		}

		if (null != initialField) {
			fieldBox.setText(initialField);
		}

		if (null != initialTags) {
			for (Entry<String, String> tag : initialTags.entrySet()) {
				TagKeyValueUIComponent tagComponent = new TagKeyValueUIComponent(main, tag.getKey(), tag.getValue());
				tagComponents.add(tagComponent);
			}
		}
	}

	/**
	 * This class encapsulates the UI elements required to specify a single tag.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class TagKeyValueUIComponent {
		/**
		 * Key box.
		 */
		private Combo keyBox;

		/**
		 * Value box.
		 */
		private Combo valueBox;

		/**
		 * Label for the key.
		 */
		private Label keyLabel;

		/**
		 * Label for the value.
		 */
		private Label valueLabel;

		/**
		 * FormText for the delete button.
		 */
		private FormText deleteText;

		/**
		 * Constructor.
		 *
		 * To be used in creation mode.
		 *
		 * @param parent
		 *            The parent composite.
		 */
		TagKeyValueUIComponent(Composite parent) {
			this(parent, null, null);
		}

		/**
		 * Constructor.
		 *
		 * To be used in editing mode.
		 *
		 * @param parent
		 *            The parent composite.
		 * @param initialKey
		 *            The initial key of the tag to be edited.
		 * @param initialValue
		 *            The initial value of the tag to be edited.
		 */
		TagKeyValueUIComponent(final Composite parent, String initialKey, String initialValue) {
			createControls(parent, initialKey, initialValue);
		}

		/**
		 * Creates the controls for this UI component.
		 *
		 * @param parent
		 *            The parent composite.
		 * @param initialKey
		 *            The initial key of the tag to be edited.
		 * @param initialValue
		 *            The initial value of the tag to be edited.
		 */
		private void createControls(final Composite parent, String initialKey, String initialValue) {
			// create controls for tag key
			keyLabel = new Label(parent, SWT.LEFT);
			keyLabel.setText("Key:");
			keyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			keyBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
			keyBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			if (null != initialKey) {
				keyBox.setText(initialKey);
			}

			// create controls for tag value
			valueLabel = new Label(parent, SWT.LEFT);
			valueLabel.setText("Value:");
			valueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			valueBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
			valueBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			if (null != initialValue) {
				valueBox.setText(initialValue);
			}

			// create controls for the delete button
			deleteText = new FormText(parent, SWT.NONE);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			deleteText.setText("<form><p><a href=\"delete\"><img href=\"deleteImg\" /></a></p></form>", true, false);
			deleteText.setImage("deleteImg", InspectIT.getDefault().getImage(InspectITImages.IMG_DELETE));
			deleteText.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					keyLabel.dispose();
					keyBox.dispose();
					valueLabel.dispose();
					valueBox.dispose();
					deleteText.dispose();
					parent.layout(true, true);
					tagComponents.remove(TagKeyValueUIComponent.this);
					pageCompletionListener.handleEvent(null);
				}
			});

			setupListeners();
			updateTagKeyOptions();
		}

		/**
		 * Sets up control listeners.
		 */
		private void setupListeners() {
			Listener tagKeyChangedListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					updateAvailableTagValues();
				}
			};
			keyBox.addListener(SWT.Modify, pageCompletionListener);
			keyBox.addListener(SWT.Modify, tagKeyChangedListener);
			valueBox.addListener(SWT.Modify, pageCompletionListener);
		}

		/**
		 * Returns the specified key of the tag.
		 *
		 * @return Returns the specified key of the tag.
		 */
		public String getTagKey() {
			return keyBox.getText();
		}

		/**
		 * Returns the specified value of the tag.
		 *
		 * @return Returns the specified value of the tag.
		 */
		public String getTagValue() {
			return valueBox.getText();
		}

		/**
		 * Updates the selection options for the key drop-down box depending on the selection in the
		 * measurement box.
		 */
		public void updateTagKeyOptions() {
			String currentText = keyBox.getText();
			List<String> tagKeys = influxService.getTags(measurementBox.getText());
			if (null != tagKeys) {
				keyBox.setItems(tagKeys.toArray(new String[tagKeys.size()]));
			}
			keyBox.setText(currentText);
		}

		/**
		 * Updates the selection options for the value drop-down box depending on the selection in
		 * the measurement box and the tag key box.
		 */
		public void updateAvailableTagValues() {
			String currentText = valueBox.getText();
			List<String> tagValues = influxService.getTagValues(measurementBox.getText(), keyBox.getText());
			if (null != tagValues) {
				valueBox.setItems(tagValues.toArray(new String[tagValues.size()]));
			}
			valueBox.setText(currentText);
		}
	}

}
