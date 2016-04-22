package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * Property Tester to check for specific criteria within an invocation sequence element.
 *
 * @author Stefan Siegl
 */
public class InvocationDataTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) receiver;

			if ("hasLoggingData".equals(property)) {
				if (expectedValue instanceof Boolean) {
					return ((Boolean) expectedValue).booleanValue() == InvocationSequenceDataHelper.hasLoggingData(data);
				} else {
					return false;
				}
			}

			if ("hasSQLData".equals(property)) {
				if (expectedValue instanceof Boolean) {
					return ((Boolean) expectedValue).booleanValue() == InvocationSequenceDataHelper.hasSQLData(data);
				} else {
					return false;
				}
			}
		}
		return false;
	}

}
