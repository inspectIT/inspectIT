package rocks.inspectit.agent.java.sensor.platform;

import rocks.inspectit.shared.all.communication.SystemSensorData;

/**
 * Fake implementation of {@link AbstractPlatformSensor}. Use this class only for testing.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class AbstractPlatformSensorFakeImpl extends AbstractPlatformSensor {

	SystemSensorData systemSensorData;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void gather() {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SystemSensorData get() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
		return systemSensorData;
	}
}