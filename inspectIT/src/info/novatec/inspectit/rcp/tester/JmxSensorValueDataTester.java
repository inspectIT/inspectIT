package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.communication.data.JmxSensorValueData;

import org.eclipse.core.expressions.PropertyTester;

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
