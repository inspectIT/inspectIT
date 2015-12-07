package rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpRequestMethodValueSource;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractSelectionRuleEditingElement;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

/**
 * Editing element for a HTTP request method matching expression.
 *
 * @author Alexander Wert
 *
 */
public class HttpRequestMethodRuleEditingElement extends AbstractSelectionRuleEditingElement<HttpRequestMethodValueSource> {

	/**
	 * The name of the string source.
	 */
	private static final String SOURCE_NAME = "HTTP method:";

	/**
	 * Description text for the HTTP URI matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the HTTP request method \n" + "matches the specified selection.";

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public HttpRequestMethodRuleEditingElement(StringMatchingExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, MatchingRuleType.HTTP_REQUEST_METHOD, DESCRIPTION, SOURCE_NAME, true, editable, upstreamValidationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
		// no validators required here
	}

}
