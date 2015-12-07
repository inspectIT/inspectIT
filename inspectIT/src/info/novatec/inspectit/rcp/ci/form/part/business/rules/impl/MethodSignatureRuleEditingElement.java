package info.novatec.inspectit.rcp.ci.form.part.business.rules.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.MethodSignatureValueSource;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

/**
 * Editing element for a method signature matching expression.
 *
 * @author Alexander Wert
 *
 */
public class MethodSignatureRuleEditingElement extends AbstractStringMatchingRuleEditingElement {
	/**
	 * Description text for the method signature matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the signature of a method within the call tree of the corresponding request\n"
			+ "matches (equals, starts with, etc.) the specified String value.";

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
	public MethodSignatureRuleEditingElement(StringMatchingExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, MatchingRuleType.METHOD_SIGNATURE, DESCRIPTION, "Method signature", true, editable, validatorRegistry);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new MethodSignatureValueSource();
	}

	@Override
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource() instanceof MethodSignatureValueSource;
	}

}
