package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

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
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public MethodSignatureRuleEditingElement(boolean editable) {
		super(MatchingRuleType.METHOD_SIGNATURE.toString(), DESCRIPTION, "Method signature", true, editable);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new MethodSignatureValueSource();
	}

	@Override
	protected boolean isValidExpression(AbstractExpression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodSignatureValueSource;
	}

}
