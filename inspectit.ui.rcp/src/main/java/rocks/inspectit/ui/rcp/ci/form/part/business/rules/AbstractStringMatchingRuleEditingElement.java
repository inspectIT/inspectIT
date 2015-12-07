package rocks.inspectit.ui.rcp.ci.form.part.business.rules;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.PatternMatchingType;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * Abstract class for all {@link AbstractRuleEditingElement} sub-classes based on string matching.
 *
 * @author Alexander Wert
 * @param <T>
 *            Type of the {@link StringValueSource}
 *
 */
public abstract class AbstractStringMatchingRuleEditingElement<T extends StringValueSource> extends AbstractRuleEditingElement<StringMatchingExpression> {

	/**
	 * Description text for the string matching row.
	 */
	private static final String DESCRIPTION = "Specify string matching strategy by selecting string matching type and target snippet.\n" + "For the matching type \""
			+ PatternMatchingType.REGEX.toString() + "\", the target snippet must be a valid regular expression.\n" + "For all other matching types no wildcards are allowed!";

	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_INFO_TEXT = "If disabled, only the root node of the call tree is evaluated against the specified condition.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified condition.";

	/**
	 * The default pattern matching type.
	 */
	protected static final PatternMatchingType DEFAULT_MATCHING_TYPE = PatternMatchingType.EQUALS;

	/**
	 * Identifier of the string snippet validator.
	 */
	private static final String STRING_SNIPPET_VALIDATOR_ID = "STRING_SNIPPET_VALIDATOR_ID";

	/**
	 * Combo box for selecting the {@link #matchingType} property.
	 */
	protected Combo patternMatchingTypeComboBox;

	/**
	 * Text editing field for specifying the {@link #snippetText} property.
	 */
	protected Text stringSnippetText;

	/**
	 * The label for the string source.
	 */
	private final String sourceName;

	/**
	 * Indicates whether the searchInDepth sub-element shell be used in this editing element.
	 */
	private final boolean useSearchInDepthComposite;

	/**
	 * Check box to select {@link #searchInDepth} property.
	 */
	private Button searchInTraceCheckBox;

	/**
	 * Spinner for configuring the {@link #searchDepth} property.
	 */
	private Spinner depthSpinner;

	/**
	 * The {@link StringValueSource} of the expression under modification.
	 */
	private final T stringValueSource;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param ruleType
	 *            the type of the matching rule
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param sourceLabel
	 *            The label string for the string source
	 * @param useSearchInTrace
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	@SuppressWarnings("unchecked")
	public AbstractStringMatchingRuleEditingElement(StringMatchingExpression expression, IRulesExpressionType ruleType, String description, String sourceLabel, boolean useSearchInTrace,
			boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, ruleType, description, editable, upstreamValidationManager);
		if (null == expression.getStringValueSource()) {
			throw new IllegalArgumentException("String value source of the expression must not be null!");
		}

		if (isValidExpression(expression)) {
			this.stringValueSource = (T) expression.getStringValueSource();
		} else {
			throw new IllegalArgumentException("Invalid String Matching Expression type!");
		}
		this.useSearchInDepthComposite = useSearchInTrace;
		this.sourceName = sourceLabel;
	}

	/**
	 * Validates the contents of the passed {@link StringMatchingExpression} without the need to
	 * create corresponding editing controls.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} to validate
	 * @param sourceName
	 *            the name of the string snippet source
	 * @return a set of {@link ValidationState} instances.
	 */
	public static Set<ValidationState> validate(StringMatchingExpression expression, String sourceName) {
		Set<ValidationState> resultSet = new HashSet<>();
		if (StringUtils.isBlank(expression.getSnippet())) {
			resultSet.add(new ValidationState(STRING_SNIPPET_VALIDATOR_ID, false, sourceName + " must not be empty!"));
		}
		return resultSet;
	}

	/**
	 * Returns true, if the given expression is a valid expression for this editing element.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to check.
	 * @return true, if the given expression is a valid expression for this editing element.
	 *         Otherwise, false.
	 */
	protected abstract boolean isValidExpression(StringMatchingExpression expression);

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(final Composite parent, FormToolkit toolkit) {
		Label fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(fillLabel);

		Label patternMatchingTypeLabel = toolkit.createLabel(parent, sourceName + ":");
		patternMatchingTypeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(patternMatchingTypeLabel);

		patternMatchingTypeComboBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		patternMatchingTypeComboBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		String[] items = new String[PatternMatchingType.values().length];
		for (int i = 0; i < PatternMatchingType.values().length; i++) {
			items[i] = PatternMatchingType.values()[i].toString();
		}
		patternMatchingTypeComboBox.setItems(items);

		for (int i = 0; i < PatternMatchingType.values().length; i++) {
			if (PatternMatchingType.values()[i].equals(DEFAULT_MATCHING_TYPE)) {
				patternMatchingTypeComboBox.select(i);
				break;
			}
		}

		patternMatchingTypeComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = patternMatchingTypeComboBox.getSelectionIndex();
				getExpression().setMatchingType(PatternMatchingType.values()[idx]);
				notifyModifyListeners();
			}
		});
		addControl(patternMatchingTypeComboBox);

		stringSnippetText = new Text(parent, SWT.BORDER);
		stringSnippetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		stringSnippetText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getExpression().setSnippet(stringSnippetText.getText());
				notifyModifyListeners();
			}
		});
		addControl(stringSnippetText);

		Label infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(DESCRIPTION);
		addControl(infoLabel);

		if (useSearchInDepthComposite) {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceFillLabel);

			Label searchInTraceLabel = toolkit.createLabel(parent, "Search in trace:");
			searchInTraceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceLabel);

			searchInTraceCheckBox = toolkit.createButton(parent, "Yes", SWT.CHECK);
			searchInTraceCheckBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceCheckBox);

			Label depthLabel = toolkit.createLabel(parent, "Maximum search depth: ");
			depthLabel.setEnabled(false);
			depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
			addControl(depthLabel);

			depthSpinner = new Spinner(parent, SWT.BORDER);
			depthSpinner.setMinimum(-1);
			depthSpinner.setMaximum(Integer.MAX_VALUE);
			depthSpinner.setSelection(-1);
			depthSpinner.setIncrement(1);
			depthSpinner.setPageIncrement(100);
			depthSpinner.setEnabled(false);
			depthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
			depthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			depthSpinner.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					getExpression().setMaxSearchDepth(depthSpinner.getSelection());
					notifyModifyListeners();
				}
			});
			addControl(depthSpinner);

			searchInTraceCheckBox.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (searchInTraceCheckBox.getSelection()) {
						depthSpinner.setEnabled(true);
						getExpression().setSearchNodeInTrace(true);
					} else {
						depthSpinner.setEnabled(false);
						getExpression().setSearchNodeInTrace(false);
					}
					notifyModifyListeners();
				}
			});
			Label searchInTraceInfoLabel = toolkit.createLabel(parent, "");
			searchInTraceInfoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			searchInTraceInfoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			searchInTraceInfoLabel.setToolTipText(SEARCH_IN_TRACE_INFO_TEXT);
			addControl(searchInTraceInfoLabel);
		} else {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, NUM_GRID_COLUMNS, 1));
			addControl(searchInTraceFillLabel);
		}
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		if (isValidExpression(expression)) {
			if (expression.isSearchNodeInTrace() && useSearchInDepthComposite) {
				searchInTraceCheckBox.setSelection(true);
				depthSpinner.setSelection(expression.getMaxSearchDepth());
				depthSpinner.setEnabled(true);
			}
			for (int i = 0; i < PatternMatchingType.values().length; i++) {
				if (PatternMatchingType.values()[i].equals(expression.getMatchingType())) {
					patternMatchingTypeComboBox.select(i);
				}
			}
			stringSnippetText.setText(expression.getSnippet());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
		ValidationControlDecoration<Text> snippetTextValidation = new ValidationControlDecoration<Text>(stringSnippetText, getValidationManager()) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		snippetTextValidation.setDescriptionText(sourceName + " must not be empty!");
		snippetTextValidation.registerListener(SWT.Modify);
		getValidationManager().addValidator(snippetTextValidation, STRING_SNIPPET_VALIDATOR_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateEnabledState() {
		super.updateEnabledState();
		if (useSearchInDepthComposite) {
			depthSpinner.setEnabled(isEditable() && searchInTraceCheckBox.getSelection());
		}
	}

	/**
	 * Gets {@link #stringValueSource}.
	 *
	 * @return {@link #stringValueSource}
	 */
	public T getStringValueSource() {
		return stringValueSource;
	}
}
