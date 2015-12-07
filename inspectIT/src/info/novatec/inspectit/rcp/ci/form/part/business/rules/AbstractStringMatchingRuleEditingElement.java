package info.novatec.inspectit.rcp.ci.form.part.business.rules;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.PatternMatchingType;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract class for all {@link AbstractRuleEditingElement} sub-classes based on string matching.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractStringMatchingRuleEditingElement extends AbstractRuleEditingElement<StringMatchingExpression> {

	/**
	 * Description text for the string matching row.
	 */
	private static final String DESCRIPTION = "Specify string matching strategy by selecting string matching type and target snippet.\n" + "For the matching type \""
			+ PatternMatchingType.REGEX.toString() + "\", the target snippet must be a valid regular expression.\n" + "For all other matching types no wildcards are allowed!";

	/**
	 * The default pattern matching type.
	 */
	protected static final PatternMatchingType DEFAULT_MATCHING_TYPE = PatternMatchingType.EQUALS;

	/**
	 * Matching type property.
	 */
	protected PatternMatchingType matchingType = DEFAULT_MATCHING_TYPE;

	/**
	 * Snippet text property.
	 */
	protected String snippetText = "";

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
	 * {@link ValidationControlDecoration} for {@link #stringSnippetText}.
	 */
	private ValidationControlDecoration<Text> snippetTextValidation;

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
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public AbstractStringMatchingRuleEditingElement(StringMatchingExpression expression, IRulesExpressionType ruleType, String description, String sourceLabel, boolean useSearchInTrace,
			boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, ruleType, description, useSearchInTrace, editable, validatorRegistry);
		this.sourceName = sourceLabel;
	}

	/**
	 *
	 * @return An instance of the {@link StringValueSource} depending on the sub-class of this
	 *         class.
	 */
	protected abstract StringValueSource getStringValueSource();

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
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression constructRuleExpression() {
		StringMatchingExpression expression = getExpression();
		if (!initialized) {
			// initialize string value source if not yet done, as it serves as identification of the
			// matching rule type.
			if (null == expression.getStringValueSource()) {
				expression.setStringValueSource(getStringValueSource());
			}
			return expression;
		}

		expression.setMatchingType(matchingType);
		expression.setSnippet(snippetText);
		expression.setStringValueSource(getStringValueSource());
		if (isSearchInDepth()) {
			expression.setSearchNodeInTrace(true);
			expression.setMaxSearchDepth(getSearchDepth());
		}
		return expression;
	}

	/**
	 * Gets {@link #matchingType}.
	 *
	 * @return {@link #matchingType}
	 */
	public PatternMatchingType getMatchingType() {
		return matchingType;
	}

	/**
	 * Gets {@link #snippetText}.
	 *
	 * @return {@link #snippetText}
	 */
	public String getSnippetText() {
		return snippetText;
	}

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
				matchingType = PatternMatchingType.values()[idx];
				notifyModifyListeners();
			}
		});
		addControl(patternMatchingTypeComboBox);

		stringSnippetText = new Text(parent, SWT.BORDER);
		stringSnippetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		stringSnippetText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				snippetText = ((Text) e.getSource()).getText();
				if (null != snippetTextValidation) {
					snippetTextValidation.executeValidation();
				}
				notifyModifyListeners();
			}
		});
		addControl(stringSnippetText);

		Label infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(DESCRIPTION);
		addControl(infoLabel);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			matchingType = expression.getMatchingType();
			for (int i = 0; i < PatternMatchingType.values().length; i++) {
				if (PatternMatchingType.values()[i].equals(matchingType)) {
					patternMatchingTypeComboBox.select(i);
				}
			}
			snippetText = expression.getSnippet();
			stringSnippetText.setText(snippetText);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
		snippetTextValidation = new ValidationControlDecoration<Text>(stringSnippetText, getValidatorRegistry()) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		snippetTextValidation.setDescriptionText(sourceName + " must not be empty!");
		addValidator(snippetTextValidation);
	}
}
