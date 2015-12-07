package info.novatec.inspectit.rcp.ci.form.part.business.rules.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.HttpUriValueSource;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

/**
 * Editing element for a HTTP URI matching expression.
 *
 * @author Alexander Wert
 *
 */
public class HttpUriMatchingRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

	/**
	 * Description text for the HTTP URI matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the URI of the corresponding request\n" + "matches (equals, starts with, etc.) the specified String value.";

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
	public HttpUriMatchingRuleEditingElement(StringMatchingExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, MatchingRuleType.HTTP_URI, DESCRIPTION, "URI", true, editable, validatorRegistry);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected StringValueSource getStringValueSource() {
		return new HttpUriValueSource();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource() instanceof HttpUriValueSource;
	}

}
