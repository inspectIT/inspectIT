package rocks.inspectit.ui.rcp.ci.form.part.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.cs.ci.business.expression.impl.NameExtractionExpression;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HostValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUriValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodSignatureValueSource;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.InputValidatorControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;
import rocks.inspectit.ui.rcp.validation.validator.FqnMethodSignatureValidator;
import rocks.inspectit.ui.rcp.validation.validator.RegexValidator;

/**
 * A SectionPart for the purpose of editing the definition of dynamic extraction of business
 * transaction names.
 *
 * @author Alexander Wert
 *
 */
public class DynamicNameExtractionPart extends SectionPart {

	/**
	 * Title of this part.
	 */
	public static final String TITLE = "Dynamic Name Extraction";

	/**
	 * Default name pattern.
	 */
	private static final String DEFAULT_PATTERN = "(1)";

	/**
	 * Default regex.
	 */
	private static final String DEFAULT_REGEX = "(.*)";

	/**
	 * Description text for this part.
	 */
	private static final String DESCRIPTION = "Use dynamic name extraction to determine the business transaction name dynamically from measurement data. \n\n"
			+ "Dynamic name extraction is applied on an invocation sequence only if the matching rule is evaluated to true for the corresponding invocation sequence.";

	/**
	 * Description for the regular expression selection.
	 */
	private static final String DESCRIPTION_REGEX_FOR_NAME = "Use a regular expression to extract fragments from the specified string value and use the fragments to specify a name pattern.";

	/**
	 * Description for the string value as name radio button.
	 */
	private static final String DESCRIPTION_STRING_VALUE_AS_NAME = "Use the extracted string value from the string value source as the name for the business transaction.";

	/**
	 * Description for the string source selection.
	 */
	private static final String DESCRIPTION_STRING_SOURCE = "The string source specifies where the string value is extracted from.";

	/**
	 * Description for the method parameter index specification.
	 */
	private static final String DESCRIPTION_METHOD_PARAMATER_INDEX = "Specify the method parameter index of the parameter to extract the string value from.";

	/**
	 * Description of the method signature field.
	 */
	private static final String DESCRIPTION_METHOD_SIGNATURE = "Specify the fully qualified method signature.\nWildcards are not allowed here.";

	/**
	 * Description text for the parameter name field.
	 */
	private static final String DESCRIPTION_HTTP_PARAMETER_NAME = "Specify the HTTP parameter name to extract the string value from.\n\nWildcards are not allowed here.";

	/**
	 * Regex description text.
	 */
	private static final String REGEX_DESCRIPTION = "Specify a regular expression to extract one (or multiple) fragments from the string resulting from the above selection.\n"
			+ "Use brackets to extract string groups. Groups are numbered in the order of their occurence.\n\n"
			+ "Example:\nfor the string \"myPackage.Class.doSomething\" using regex \".*Class\\.(.*)Some(.*)\" yields the following string groups:\n" + "1: \"do\"\n" + "2: \"thing\"";

	/**
	 * Name pattern description text.
	 */
	private static final String NAME_PATTERN_DESCRIPTION = "Use the string groups extracted with the regular expression to specify a name pattern.\n\n"
			+ "Example:\nthe name pattern \"(1)-cool-(2)\" would result in the name \"do-cool-thing\"\nfor the string groups from the example above.";

	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_DESCRIPTION = "If disabled, only the root node of the call tree is evaluated against the specified regular expression.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified regular expression.";

	/**
	 * Identifier for the regular expression control validator.
	 */
	private static final String REGULAR_EXPRESSION_TEXT = "regularExpressionText";

	/**
	 * Identifier for the name pattern constrol validator.
	 */
	private static final String NAME_PATTERN_TEXT = "namePatternText";

	/**
	 * Number of columns in the main {@link GridLayout} of this section.
	 */
	private static final int NUM_COLUMNS = 5;

	/**
	 * Identifier of the name pattern control. (Used for unique identification of corresponding
	 * optional group.)
	 */
	private static final String NAME_PATTERN_ID = "NAME_PATTERN_ID";

	/**
	 * Identifier of the http parameter control. (Used for unique identification of corresponding
	 * optional group.)
	 */
	private static final String HTTP_PARAMETER_ID = "HTTP_PARAMETER_ID";

	/**
	 * Identifier of the method parameter control. (Used for unique identification of corresponding
	 * optional group.)
	 */
	private static final String METHOD_PARAMETER_ID = "METHOD_PARAMETER_ID";

	/**
	 * Check box for enabling/disabling dynamic name extraction.
	 */
	private Button extractNameCheckbox;

	/**
	 * Text control for editing the regular expression.
	 */
	private Text regularExpressionText;

	/**
	 * Selection box for string source selection.
	 */
	private Combo stringSourceSelectionCombo;

	/**
	 * Text control for editing the parameter name.
	 */
	private Text parameterNameText;

	/**
	 * Text control for editing the method signature.
	 */
	private Text methodSignatureText;

	/**
	 * Spinner for specifying the parameter index.
	 */
	private Spinner parameterIndexSpinner;

	/**
	 * Text control for editing the name pattern.
	 */
	private Text namePatternText;

	/**
	 * Check box for enabling/disabling search in trace.
	 */
	private Button searchInTraceCheckBox;

	/**
	 * Spinner for modifying the search depth.
	 */
	private Spinner searchDepthSpinner;

	/**
	 * Button for activating usage of string value as the BT name.
	 */
	private Button useStringValueAsNameRadioButton;

	/**
	 * Button for activating usage of a regex and name pattern.
	 */
	private Button useRegexForNameRadioButton;

	/**
	 * Holds the state whether this section is in the initialization phase or not.
	 */
	private boolean initializationPhase = false;

	/**
	 * The {@link BusinessTransactionDefinition} instance serving as provider and receiver of the
	 * {@link NameExtractionExpression} edited in this form part.
	 */
	private BusinessTransactionDefinition businessTransaction;

	/**
	 * Main composite.
	 */
	private Composite mainComposite;

	/**
	 * {@link FormToolkit} used for control creation.
	 */
	private FormToolkit toolkit;

	/**
	 * Control after which the string value source specification controls shell be inserted.
	 */
	private Control moveBelowForStringValueSourceControls;

	/**
	 * Control after which the name pattern and regex specification controls shell be inserted.
	 */
	private Control moveBelowForNamePatternControls;

	/**
	 * List of all controls in this editing element.
	 */
	private final List<Control> childControls = new ArrayList<>();

	/**
	 * List of optional controls (e.g. created / disposed on a specific selection in the mandatory
	 * controls).
	 */
	private final Map<String, Set<Control>> optionalControls = new HashMap<>();

	/**
	 * The validation manager.
	 */
	private final DynamicNameExtractionValidationManager validationManager;

	/**
	 * Current selection of the string source.
	 */
	private MatchingRuleType stringSourceSelection = MatchingRuleType.values()[0];

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param managedForm
	 *            {@link IManagedForm} to add this part to.
	 * @param validationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public DynamicNameExtractionPart(Composite parent, IManagedForm managedForm, AbstractValidationManager<String> validationManager) {
		super(parent, managedForm.getToolkit(), Section.TITLE_BAR);
		this.validationManager = new DynamicNameExtractionValidationManager(validationManager);
		createPart(managedForm);
	}

	/**
	 * Initializes the contents of this part according to the passed
	 * {@link BusinessTransactionDefinition} instance.
	 *
	 * @param businessTransaction
	 *            {@link BusinessTransactionDefinition} instance to retrieve the contents from.
	 */
	public void init(BusinessTransactionDefinition businessTransaction) {
		initializationPhase = true;
		this.businessTransaction = businessTransaction;
		reset();
		validationManager.reset();
		NameExtractionExpression nameExtractionExpression = businessTransaction.getNameExtractionExpression();
		if (null != nameExtractionExpression) {
			extractNameCheckbox.setSelection(true);
			stringSourceSelection = MatchingRulesEditingElementFactory.getMatchingRuleType(nameExtractionExpression.getStringValueSource());

			int selectionIndex = 0;
			for (int i = 0; i < MatchingRuleType.values().length; i++) {
				if (MatchingRuleType.values()[i].equals(stringSourceSelection)) {
					selectionIndex = i;
					break;
				}
			}
			stringSourceSelectionCombo.select(selectionIndex);

			switch (stringSourceSelection) {
			case HTTP_PARAMETER:
				createHttpParameterControls();
				String parameterName = ((HttpParameterValueSource) nameExtractionExpression.getStringValueSource()).getParameterName();
				if (null == parameterName) {
					parameterName = "";
				}
				parameterNameText.setText(parameterName);
				break;
			case METHOD_PARAMETER:
				createMethodParameterControls();
				String methodSignature = ((MethodParameterValueSource) nameExtractionExpression.getStringValueSource()).getMethodSignature();
				if (null == methodSignature) {
					methodSignature = "";
				}
				methodSignatureText.setText(methodSignature);
				parameterIndexSpinner.setSelection(((MethodParameterValueSource) nameExtractionExpression.getStringValueSource()).getParameterIndex());
				break;
			case HTTP_URI:
			case IP:
			case METHOD_SIGNATURE:
			default:
				break;
			}

			String regularExpression = (nameExtractionExpression.getRegularExpression() == null) ? DEFAULT_REGEX : nameExtractionExpression.getRegularExpression();
			String namePattern = (nameExtractionExpression.getTargetNamePattern() == null) ? DEFAULT_PATTERN : nameExtractionExpression.getTargetNamePattern();
			boolean searchNodeInTrace = nameExtractionExpression.isSearchNodeInTrace();
			int maxSearchDepth = nameExtractionExpression.getMaxSearchDepth();
			if (!regularExpression.equals(DEFAULT_REGEX) || !namePattern.equals(DEFAULT_PATTERN)) {
				useRegexForNameRadioButton.setSelection(true);
				useStringValueAsNameRadioButton.setSelection(false);
				createNamePatternControls();
				regularExpressionText.setText(regularExpression);
				namePatternText.setText(namePattern);
			} else {
				useRegexForNameRadioButton.setSelection(false);
				useStringValueAsNameRadioButton.setSelection(true);
			}

			searchInTraceCheckBox.setSelection(searchNodeInTrace);
			if (searchNodeInTrace) {
				searchDepthSpinner.setSelection(maxSearchDepth);
				searchDepthSpinner.setEnabled(true);
			} else {
				searchDepthSpinner.setEnabled(false);
			}

			setMainPartEnabled(true);
		} else {
			reset();
		}
		initializationPhase = false;
	}

	/**
	 * Sets {@link #editable}.
	 *
	 * @param editable
	 *            New value for {@link #editable}
	 */
	public void setEditable(boolean editable) {
		getSection().setEnabled(editable);
		extractNameCheckbox.setEnabled(editable);
		setMainPartEnabled(editable && extractNameCheckbox.getSelection());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		businessTransaction.setNameExtractionExpression(constructNameExtractionExpression());
		if (onSave) {
			super.commit(onSave);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markDirty() {
		commit(false);
		super.markDirty();
	}

	/**
	 * Creates part controls.
	 *
	 * @param managedForm
	 *            {@link IManagedForm} to add this part to.
	 */
	private void createPart(IManagedForm managedForm) {
		initializationPhase = true;
		managedForm.addPart(this);
		getSection().setLayout(new GridLayout(1, false));
		getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getSection().setText(TITLE);
		toolkit = managedForm.getToolkit();

		// section body
		mainComposite = toolkit.createComposite(getSection());
		GridLayout layout = new GridLayout(NUM_COLUMNS, false);
		layout.horizontalSpacing = 8;
		mainComposite.setLayout(layout);
		extractNameCheckbox = toolkit.createButton(mainComposite, "Extract name dynamically", SWT.CHECK);
		extractNameCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, NUM_COLUMNS - 1, 1));

		extractNameCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMainPartEnabled(extractNameCheckbox.getSelection());
				if (!initializationPhase) {
					markDirty();
				}
			}
		});

		Label labelInfoImage = toolkit.createLabel(mainComposite, "");
		labelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		labelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		labelInfoImage.setToolTipText(DESCRIPTION);

		// one empty row for separation
		toolkit.createLabel(mainComposite, "").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		createStringSourceSection();

		// one empty row for separation
		toolkit.createLabel(mainComposite, "").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		createNamePatternSection();

		// one empty row for separation
		toolkit.createLabel(mainComposite, "").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		createScopeInTraceSection();

		setMainPartEnabled(false);
		getSection().setClient(mainComposite);
		initializationPhase = false;
	}

	/**
	 * Creates control section for selection of the string source.
	 */
	private void createStringSourceSection() {
		FormText headingText = toolkit.createFormText(mainComposite, false);
		headingText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		headingText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		headingText.setFont("header", JFaceResources.getBannerFont());
		headingText.setText("<form><p><span color=\"header\" font=\"header\">String Source Selection</span></p></form>", true, false);

		Label selectionComboLabel = toolkit.createLabel(mainComposite, "String source:");
		selectionComboLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		stringSourceSelectionCombo = new Combo(mainComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		stringSourceSelectionCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, NUM_COLUMNS - 2, 1));
		String[] items = new String[MatchingRuleType.values().length];
		for (int i = 0; i < MatchingRuleType.values().length; i++) {
			items[i] = MatchingRuleType.values()[i].toString().replace(" Matching", "");
		}
		stringSourceSelectionCombo.setItems(items);
		stringSourceSelectionCombo.select(0);
		addControl(stringSourceSelectionCombo);

		final Label stringSourceSelectionLabelInfoImage = toolkit.createLabel(mainComposite, NAME_PATTERN_DESCRIPTION);
		stringSourceSelectionLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		stringSourceSelectionLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		stringSourceSelectionLabelInfoImage.setToolTipText(DESCRIPTION_STRING_SOURCE);
		moveBelowForStringValueSourceControls = stringSourceSelectionLabelInfoImage;
		stringSourceSelectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stringSourceSelectionChanged();
			}
		});
	}

	/**
	 * Creates control section for specification of the name pattern.
	 */
	private void createNamePatternSection() {
		FormText headingText = toolkit.createFormText(mainComposite, false);
		headingText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		headingText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		headingText.setFont("header", JFaceResources.getBannerFont());
		headingText.setText("<form><p><span color=\"header\" font=\"header\">Name Pattern Specification</span></p></form>", true, false);

		useStringValueAsNameRadioButton = toolkit.createButton(mainComposite, "Use string value as name", SWT.RADIO);
		useStringValueAsNameRadioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, NUM_COLUMNS - 1, 1));
		useStringValueAsNameRadioButton.setSelection(true);
		useStringValueAsNameRadioButton.addSelectionListener(new SelectionAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useStringValueAsNameRadioButton.getSelection()) {
					disposeOptionalControls(NAME_PATTERN_ID);
					validationManager.validationRemoved(NAME_PATTERN_TEXT);
					validationManager.validationRemoved(REGULAR_EXPRESSION_TEXT);
					if (!initializationPhase) {
						markDirty();
					}
					mainComposite.layout(true, true);
				}
			}
		});
		addControl(useStringValueAsNameRadioButton);

		Label useStringValueAsNameLabelInfoImage = toolkit.createLabel(mainComposite, DESCRIPTION_STRING_VALUE_AS_NAME);
		useStringValueAsNameLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		useStringValueAsNameLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		useStringValueAsNameLabelInfoImage.setToolTipText(DESCRIPTION_STRING_VALUE_AS_NAME);

		useRegexForNameRadioButton = toolkit.createButton(mainComposite, "Specify name pattern", SWT.RADIO);
		useRegexForNameRadioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, NUM_COLUMNS - 1, 1));
		useRegexForNameRadioButton.setSelection(false);
		addControl(useRegexForNameRadioButton);

		final Label useRegexForNameLabelInfoImage = toolkit.createLabel(mainComposite, DESCRIPTION_REGEX_FOR_NAME);
		useRegexForNameLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		useRegexForNameLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		useRegexForNameLabelInfoImage.setToolTipText(DESCRIPTION_REGEX_FOR_NAME);
		moveBelowForNamePatternControls = useRegexForNameLabelInfoImage;
		useRegexForNameRadioButton.addSelectionListener(new SelectionAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useRegexForNameRadioButton.getSelection()) {
					createNamePatternControls();
					if (!initializationPhase) {
						markDirty();
					}
					mainComposite.layout(true, true);
				}
			}
		});
	}

	/**
	 * Creates control section for specification of the scope in trace.
	 */
	private void createScopeInTraceSection() {
		FormText headingText = toolkit.createFormText(mainComposite, false);
		headingText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));
		headingText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		headingText.setFont("header", JFaceResources.getBannerFont());
		headingText.setText("<form><p><span color=\"header\" font=\"header\">Scope in Trace</span></p></form>", true, false);

		Label searchInTraceLabel = toolkit.createLabel(mainComposite, "Search in trace:");
		searchInTraceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		searchInTraceCheckBox = toolkit.createButton(mainComposite, "Yes", SWT.CHECK);
		searchInTraceCheckBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		searchInTraceCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDepthSpinner.setEnabled(searchInTraceCheckBox.getSelection());
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		addControl(searchInTraceCheckBox);

		Label depthLabel = toolkit.createLabel(mainComposite, "Maximum search depth: ");
		depthLabel.setEnabled(false);
		depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		searchDepthSpinner = new Spinner(mainComposite, SWT.BORDER);
		searchDepthSpinner.setMinimum(-1);
		searchDepthSpinner.setMaximum(Integer.MAX_VALUE);
		searchDepthSpinner.setSelection(-1);
		searchDepthSpinner.setIncrement(1);
		searchDepthSpinner.setPageIncrement(100);
		searchDepthSpinner.setEnabled(false);
		searchDepthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
		searchDepthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		searchDepthSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		addControl(searchDepthSpinner);

		Label searchInTraceInfoLabelImage = toolkit.createLabel(mainComposite, "");
		searchInTraceInfoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		searchInTraceInfoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		searchInTraceInfoLabelImage.setToolTipText(SEARCH_IN_TRACE_DESCRIPTION);
	}

	/**
	 * Creates name pattern and regex controls.
	 */
	private void createNamePatternControls() {
		Label regularExpressionLabel = toolkit.createLabel(mainComposite, "Regular Expression:");
		regularExpressionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		regularExpressionLabel.moveBelow(moveBelowForNamePatternControls);
		addOptionalControl(NAME_PATTERN_ID, regularExpressionLabel);

		regularExpressionText = toolkit.createText(mainComposite, DEFAULT_REGEX, SWT.BORDER);
		regularExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		regularExpressionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		regularExpressionText.moveBelow(regularExpressionLabel);
		addOptionalControl(NAME_PATTERN_ID, regularExpressionText);

		InputValidatorControlDecoration regexTextValidation = new InputValidatorControlDecoration(regularExpressionText, validationManager, new RegexValidator(false));
		regexTextValidation.registerListener(SWT.Modify);
		validationManager.addValidator(regexTextValidation, REGULAR_EXPRESSION_TEXT);

		Label regularExpressionLabelInfoImage = toolkit.createLabel(mainComposite, REGEX_DESCRIPTION);
		regularExpressionLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		regularExpressionLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		regularExpressionLabelInfoImage.setToolTipText(REGEX_DESCRIPTION);
		regularExpressionLabelInfoImage.moveBelow(regularExpressionText);
		addOptionalControl(NAME_PATTERN_ID, regularExpressionLabelInfoImage);

		Label namePatternLabel = toolkit.createLabel(mainComposite, "Name Pattern:");
		namePatternLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		namePatternLabel.moveBelow(regularExpressionLabelInfoImage);
		addOptionalControl(NAME_PATTERN_ID, namePatternLabel);

		namePatternText = toolkit.createText(mainComposite, DEFAULT_PATTERN, SWT.BORDER);
		namePatternText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		namePatternText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		namePatternText.moveBelow(namePatternLabel);
		addOptionalControl(NAME_PATTERN_ID, namePatternText);

		ValidationControlDecoration<Text> namePatternTextValidation = new ValidationControlDecoration<Text>(namePatternText, validationManager) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText()) && control.getText().matches(".*\\(\\d\\).*");
			}
		};

		namePatternTextValidation.setDescriptionText("Name pattern must not be empty and must contain at least one capturing group (e.g. '(1)', '(2)', ...) from the regular expression.!");
		namePatternTextValidation.registerListener(SWT.Modify);
		validationManager.addValidator(namePatternTextValidation, NAME_PATTERN_TEXT);

		Label namePatternLabelInfoImage = toolkit.createLabel(mainComposite, NAME_PATTERN_DESCRIPTION);
		namePatternLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		namePatternLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		namePatternLabelInfoImage.setToolTipText(NAME_PATTERN_DESCRIPTION);
		namePatternLabelInfoImage.moveBelow(namePatternText);
		addOptionalControl(NAME_PATTERN_ID, namePatternLabelInfoImage);
	}

	/**
	 * Creates controls for the HTTP parameter value source.
	 */
	private void createHttpParameterControls() {
		Label parameterNameLabel = toolkit.createLabel(mainComposite, "Parameter name:");
		parameterNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		parameterNameLabel.moveBelow(moveBelowForStringValueSourceControls);
		addOptionalControl(HTTP_PARAMETER_ID, parameterNameLabel);

		parameterNameText = toolkit.createText(mainComposite, "", SWT.BORDER);
		parameterNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		parameterNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		parameterNameText.moveBelow(parameterNameLabel);
		addOptionalControl(HTTP_PARAMETER_ID, parameterNameText);

		ValidationControlDecoration<Text> parameterNameTextValidation = new ValidationControlDecoration<Text>(parameterNameText, validationManager) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};

		parameterNameTextValidation.setDescriptionText("Parameter name must not be empty!");
		parameterNameTextValidation.registerListener(SWT.Modify);
		validationManager.addValidator(parameterNameTextValidation, HTTP_PARAMETER_ID);

		Label infoLabelImage = toolkit.createLabel(mainComposite, "");
		infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelImage.setToolTipText(DESCRIPTION_HTTP_PARAMETER_NAME);
		infoLabelImage.moveBelow(parameterNameText);
		addOptionalControl(HTTP_PARAMETER_ID, infoLabelImage);
	}

	/**
	 * Creates controls for the method parameter value source.
	 */
	private void createMethodParameterControls() {
		Label methodSignatureLabel = toolkit.createLabel(mainComposite, "Method signature:");
		methodSignatureLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		methodSignatureLabel.moveBelow(moveBelowForStringValueSourceControls);
		addOptionalControl(METHOD_PARAMETER_ID, methodSignatureLabel);

		methodSignatureText = toolkit.createText(mainComposite, "", SWT.BORDER);
		methodSignatureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		methodSignatureText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		methodSignatureText.moveBelow(methodSignatureLabel);
		addOptionalControl(METHOD_PARAMETER_ID, methodSignatureText);

		InputValidatorControlDecoration methodSignatureTextValidation = new InputValidatorControlDecoration(methodSignatureText, validationManager, new FqnMethodSignatureValidator(false));
		methodSignatureTextValidation.registerListener(SWT.Modify);
		validationManager.addValidator(methodSignatureTextValidation, METHOD_PARAMETER_ID);

		Label infoLabelImage = toolkit.createLabel(mainComposite, "");
		infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelImage.setToolTipText(DESCRIPTION_METHOD_SIGNATURE);
		infoLabelImage.moveBelow(methodSignatureText);
		addOptionalControl(METHOD_PARAMETER_ID, infoLabelImage);

		Label parameterIndexLabel = toolkit.createLabel(mainComposite, "Parameter index:");
		parameterIndexLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		parameterIndexLabel.moveBelow(infoLabelImage);
		addOptionalControl(METHOD_PARAMETER_ID, parameterIndexLabel);

		parameterIndexSpinner = new Spinner(mainComposite, SWT.BORDER);
		parameterIndexSpinner.setMinimum(0);
		parameterIndexSpinner.setMaximum(50);
		parameterIndexSpinner.setSelection(0);
		parameterIndexSpinner.setIncrement(1);
		parameterIndexSpinner.setPageIncrement(5);
		parameterIndexSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		parameterIndexSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		parameterIndexSpinner.moveBelow(parameterIndexLabel);
		addOptionalControl(METHOD_PARAMETER_ID, parameterIndexSpinner);

		infoLabelImage = toolkit.createLabel(mainComposite, "");
		infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, NUM_COLUMNS - 2, 1));
		infoLabelImage.setToolTipText(DESCRIPTION_METHOD_PARAMATER_INDEX);
		infoLabelImage.moveBelow(parameterIndexSpinner);
		addOptionalControl(METHOD_PARAMETER_ID, infoLabelImage);
	}

	/**
	 * Selection of the string source changed.
	 */
	private void stringSourceSelectionChanged() {
		// check if selected string source has changed
		int idx = stringSourceSelectionCombo.getSelectionIndex();
		MatchingRuleType newSelection = MatchingRuleType.values()[idx];
		if (newSelection.equals(stringSourceSelection)) {
			return;
		}

		// if so, dispose all controls associated with the previous selection
		disposeOptionalControls(HTTP_PARAMETER_ID);
		validationManager.validationRemoved(HTTP_PARAMETER_ID);
		disposeOptionalControls(METHOD_PARAMETER_ID);
		validationManager.validationRemoved(METHOD_PARAMETER_ID);

		// set new selection and create controls for the new selection
		stringSourceSelection = newSelection;
		switch (stringSourceSelection) {
		case HTTP_PARAMETER:
			createHttpParameterControls();
			break;
		case METHOD_PARAMETER:
			createMethodParameterControls();
			break;
		case HTTP_URI:
		case IP:
		case METHOD_SIGNATURE:
		default:
			break;
		}
		if (!initializationPhase) {
			markDirty();
		}
		mainComposite.layout(true, true);
	}

	/**
	 * Adds control to the list of mandatory controls.
	 *
	 * @param control
	 *            {@link Control} to add
	 */
	private void addControl(Control control) {
		childControls.add(control);
	}

	/**
	 * Adds control to the list of optional controls for the given group id.
	 *
	 * @param groupId
	 *            the identifier of the optional group the control belongs to
	 * @param control
	 *            {@link Control} to add
	 */
	private void addOptionalControl(String groupId, Control control) {
		if (!optionalControls.containsKey(groupId)) {
			optionalControls.put(groupId, new HashSet<Control>());
		}
		optionalControls.get(groupId).add(control);
	}

	/**
	 * Disposes all optional controls that belong to the specified group ID.
	 *
	 * @param groupId
	 *            identifier of the optional group for which the controls shall be disposed.
	 */
	private void disposeOptionalControls(String groupId) {
		if (optionalControls.containsKey(groupId)) {
			for (Control c : optionalControls.get(groupId)) {
				c.dispose();
			}
			optionalControls.remove(groupId);
		}
		mainComposite.layout(true, true);
	}

	/**
	 * Resets the contents of this part.
	 */
	private void reset() {
		disposeOptionalControls(NAME_PATTERN_ID);
		disposeOptionalControls(HTTP_PARAMETER_ID);
		disposeOptionalControls(METHOD_PARAMETER_ID);
		extractNameCheckbox.setSelection(false);
		stringSourceSelectionCombo.select(0);
		useRegexForNameRadioButton.setSelection(false);
		useStringValueAsNameRadioButton.setSelection(true);
		searchInTraceCheckBox.setSelection(false);
		searchDepthSpinner.setSelection(-1);
		searchDepthSpinner.setEnabled(false);
		setMainPartEnabled(false);
	}

	/**
	 * Constructs a {@link NameExtractionExpression} instance from the contents of the controls of
	 * this part.
	 *
	 * @return a {@link NameExtractionExpression} instance
	 */
	private NameExtractionExpression constructNameExtractionExpression() {
		if (extractNameCheckbox.getSelection()) {
			NameExtractionExpression nameExtractionExpression = new NameExtractionExpression();
			StringValueSource stringValueSource = constructStringValueSource();
			nameExtractionExpression.setStringValueSource(stringValueSource);
			if (useRegexForNameRadioButton.getSelection()) {
				nameExtractionExpression.setRegularExpression(regularExpressionText.getText());
				nameExtractionExpression.setTargetNamePattern(namePatternText.getText());
			} else {
				nameExtractionExpression.setRegularExpression(DEFAULT_REGEX);
				nameExtractionExpression.setTargetNamePattern(DEFAULT_PATTERN);
			}

			nameExtractionExpression.setSearchNodeInTrace(searchInTraceCheckBox.getSelection());
			if (searchInTraceCheckBox.getSelection()) {
				nameExtractionExpression.setMaxSearchDepth(searchDepthSpinner.getSelection());
			}
			return nameExtractionExpression;
		} else {
			return null;
		}
	}

	/**
	 * Constructs the {@link StringValueSource} from the control contents of this part.
	 *
	 * @return Returns a {@link StringValueSource} instance.
	 */
	private StringValueSource constructStringValueSource() {
		MatchingRuleType sourceType = MatchingRuleType.values()[stringSourceSelectionCombo.getSelectionIndex()];
		switch (sourceType) {
		case HTTP_PARAMETER:
			return new HttpParameterValueSource(parameterNameText.getText());
		case HTTP_URI:
			return new HttpUriValueSource();
		case IP:
			return new HostValueSource();
		case METHOD_PARAMETER:
			return new MethodParameterValueSource(parameterIndexSpinner.getSelection(), methodSignatureText.getText());
		case METHOD_SIGNATURE:
			return new MethodSignatureValueSource();
		default:
			throw new RuntimeException("Unsupported value source type!");
		}

	}

	/**
	 * Sets the enabled state of the main content part.
	 *
	 * @param enabled
	 *            enabled state
	 */
	private void setMainPartEnabled(boolean enabled) {
		for (Control control : childControls) {
			if ((null != control) && !(control instanceof Label)) {
				control.setEnabled(enabled);
			}
		}
		for (Entry<String, Set<Control>> optionalControlEntry : optionalControls.entrySet()) {
			for (Control control : optionalControlEntry.getValue()) {
				if ((null != control) && !(control instanceof Label)) {
					control.setEnabled(enabled);
				}
			}
		}

		if (searchInTraceCheckBox.getSelection()) {
			searchDepthSpinner.setEnabled(enabled);
		} else {
			searchDepthSpinner.setEnabled(false);
		}

		getManagedForm().getForm().layout(true, true);
	}

	/**
	 * Validation manager responsible for delegating validation control changes to the upstream
	 * validation manager.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class DynamicNameExtractionValidationManager implements IControlValidationListener {

		/**
		 * A map of {@link ValidationControlDecoration} instances identified by their
		 * {@link ValidatorKey}.
		 */
		private final Map<ValidationControlDecoration<?>, String> controlValidators = new HashMap<>();

		/**
		 * Upstream validation manager to be notified on changes.
		 */
		private final AbstractValidationManager<String> upstreamValidationManager;

		/**
		 * Constructor.
		 *
		 * @param upstreamValidationManager
		 *            Upstream validation manager to be notified on changes.
		 */
		DynamicNameExtractionValidationManager(AbstractValidationManager<String> upstreamValidationManager) {
			this.upstreamValidationManager = upstreamValidationManager;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
			upstreamValidationManager.validationStateChanged(TITLE, new ValidationState(controlValidators.get(validationControlDecoration), valid, validationControlDecoration.getDescriptionText()));
		}

		/**
		 * Remove validation control with the given key.
		 *
		 * @param validationKey
		 *            key of the validation control to be removed.
		 */
		public void validationRemoved(String validationKey) {
			upstreamValidationManager.validationStateRemoved(TITLE, validationKey);
			ValidationControlDecoration<?> toRemove = null;
			for (Entry<ValidationControlDecoration<?>, String> entry : controlValidators.entrySet()) {
				if (entry.getValue().equals(validationKey)) {
					toRemove = entry.getKey();
					break;
				}
			}
			controlValidators.remove(toRemove);
		}

		/**
		 * Removes all {@link ValidationControlDecoration} instances.
		 */
		public void reset() {
			controlValidators.clear();
		}

		/**
		 * Adds an {@link ValidationControlDecoration} instance.
		 *
		 * @param validator
		 *            {@link ValidationControlDecoration} instance to add
		 * @param controlId
		 *            the identifier of the control.
		 */
		private void addValidator(ValidationControlDecoration<?> validator, String controlId) {
			controlValidators.put(validator, controlId);
			validationStateChanged(validator.isValid(), validator);
		}
	}
}
