package rocks.inspectit.ui.rcp.ci.form.part.business;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.AgentNameValueSource;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

/**
 * Editing element for a agent name matching expression.
 *
 * @author Tobias Angerstein
 *
 */
public class AgentRuleEditingElement extends AbstractStringMatchingRuleEditingElement<AgentNameValueSource> {
	/**
	 * The name of the string source.
	 */
	private static final String SOURCE_NAME = "Agent name";

	/**
	 * Description text for the Agent name matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the agent of the server serving the corresponding request\n" + "matches (equals, starts with, etc.) the specified String value.";

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
	public AgentRuleEditingElement(StringMatchingExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, MatchingRuleType.AGENT, DESCRIPTION, SOURCE_NAME, false, editable, upstreamValidationManager);
	}

	@Override
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource() instanceof AgentNameValueSource;
	}

}
