package rocks.inspectit.agent.java.sensor.platform;

import rocks.inspectit.shared.all.communication.PlatformSensorData;

/**
 * Fake implementation of {@link AbstractPlatformSensor}. Use this class only for testing.
 *
 * @author Max Wassiljew (max.wassiljew@novatec-gmbh.de)
 */
public class AbstractPlatformSensorFakeImpl extends AbstractPlatformSensor {

	PlatformSensorData platformSensorData;

	/** {@inheritDoc} */
	public void reset() {
		// TODO Auto-generated method stub
	}

	/** {@inheritDoc} */
	public void gather() {
		// TODO Auto-generated method stub
	}

	/** {@inheritDoc} */
	public PlatformSensorData get() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected PlatformSensorData getPlatformSensorData() {
		return platformSensorData;
	}
}
