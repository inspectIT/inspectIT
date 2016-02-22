package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;

/**
 * Tester for charting possibilities of {@link JmxSensorValueData}.
 * 
 * @author Marius Oehler
 *
 */
public class JmxSensorValueDataTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof JmxSensorValueData) {
			JmxSensorValueData jmxData = (JmxSensorValueData) receiver;

			if ("canChart".equals(property)) {
				return jmxData.isBooleanOrNumeric();
			}
		}
		return false;
	}

}
