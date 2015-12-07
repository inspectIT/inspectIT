package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.HttpUriValueSource;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

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
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public HttpUriMatchingRuleEditingElement(boolean editable) {
		super(MatchingRuleType.HTTP_URI.toString(), DESCRIPTION, "HTTP URI ", true, editable);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new HttpUriValueSource();
	}

	@Override
	protected boolean isValidExpression(AbstractExpression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpUriValueSource;
	}

}
