package info.novatec.inspectit.rcp.ci.form.part.business.rules.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.BooleanExpressionType;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.validation.IValidatorRegistry;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Editing element for a boolean value expression.
 *
 * @author Alexander Wert
 *
 */
public class BooleanRuleEditingElement extends AbstractRuleEditingElement<BooleanExpression> {

	/**
	 * Description text for the Boolean rule.
	 */
	private static final String DESCRIPTION = "The evaluation of this rule is independent of any measurement data.\n"
			+ "Depending on the specified value, this expression is either always evaluated true or always to false.";

	/**
	 * Combo box for the selection of the boolean value.
	 */
	private Combo valueComboBox;

	/**
	 * The boolean value for the rule.
	 */
	private boolean value;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public BooleanRuleEditingElement(BooleanExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, BooleanExpressionType.BOOLEAN, DESCRIPTION, editable, validatorRegistry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		Label fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(fillLabel);

		Label valueLabel = toolkit.createLabel(parent, "Value:");
		valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(valueLabel);

		valueComboBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		valueComboBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));
		valueComboBox.setItems(new String[] { "true", "false" });

		valueComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = valueComboBox.getSelectionIndex();

				if (idx == 0) {
					value = true;
				} else {
					value = false;
				}
				notifyModifyListeners();
			}
		});
		addControl(valueComboBox);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(BooleanExpression expression) {
		value = expression.isValue();
		if (value) {
			valueComboBox.select(0);
		} else {
			valueComboBox.select(1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
	}
}
