package rocks.inspectit.ui.rcp.ci.form.part.business.rules;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.PatternMatchingType;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

/**
 * Abstract class for all {@link AbstractRuleEditingElement} sub-classes based on option selection.
 *
 * @author Alexander Wert
 * @param <T>
 *            Type of the {@link StringValueSource}
 *
 */
public abstract class AbstractSelectionRuleEditingElement<T extends StringValueSource> extends AbstractSearchInDepthEditingElement {
	/**
	 * The label for the string source.
	 */
	private final String sourceName;

	/**
	 * The {@link StringValueSource} of the expression under modification.
	 */
	private final T stringValueSource;

	/**
	 * Combo box for the selection of the boolean value.
	 */
	private Combo valueComboBox;

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
	public AbstractSelectionRuleEditingElement(StringMatchingExpression expression, IRulesExpressionType ruleType, String description, String sourceLabel, boolean useSearchInTrace, boolean editable,
			AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, ruleType, description, useSearchInTrace, editable, upstreamValidationManager);
		if (null == expression.getStringValueSource()) {
			throw new IllegalArgumentException("String value source of the expression must not be null!");
		}
		if (isValidExpression(expression)) {
			this.stringValueSource = (T) expression.getStringValueSource();
		} else {
			throw new IllegalArgumentException("Invalid String Matching Expression type!");
		}
		expression.setMatchingType(PatternMatchingType.EQUALS);
		this.sourceName = sourceLabel;
	}

	/**
	 * Returns true, if the given expression is a valid expression for this editing element.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to check.
	 * @return true, if the given expression is a valid expression for this editing element.
	 *         Otherwise, false.
	 */
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource().isSelection() && (expression.getStringValueSource().getOptions().length > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		Label fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(fillLabel);

		Label valueLabel = toolkit.createLabel(parent, sourceName);
		valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(valueLabel);

		valueComboBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		valueComboBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1));
		final String[] options = stringValueSource.getOptions();
		valueComboBox.setItems(options);

		valueComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = valueComboBox.getSelectionIndex();
				getExpression().setSnippet(options[idx]);
				notifyModifyListeners();
			}
		});
		addControl(valueComboBox);
		super.createSpecificElements(parent, toolkit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		super.executeSpecificInitialization(expression);
		int index = 0;
		for (String opt : stringValueSource.getOptions()) {
			if (opt.equals(expression.getSnippet())) {
				break;
			}
			index++;
		}
		if (index < stringValueSource.getOptions().length) {
			valueComboBox.select(index);
		}
	}
}
