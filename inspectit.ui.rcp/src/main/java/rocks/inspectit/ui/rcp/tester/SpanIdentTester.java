package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.all.tracing.data.ISpanIdentAware;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Tester for the span ident.
 *
 * @author Ivan Senic
 *
 */
public class SpanIdentTester extends PropertyTester {

	/**
	 * Tester property for the span ident not null.
	 */
	private static final String NOT_NULL_PROPERTY = "spanIdentNotNull";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof ISpanIdentAware) {
			if (NOT_NULL_PROPERTY.equals(property)) {
				return null != ((ISpanIdentAware) receiver).getSpanIdent();
			}
		}

		if (receiver instanceof SpanIdent) {
			if (NOT_NULL_PROPERTY.equals(property)) {
				return true;
			}
		}

		return false;
	}

}
