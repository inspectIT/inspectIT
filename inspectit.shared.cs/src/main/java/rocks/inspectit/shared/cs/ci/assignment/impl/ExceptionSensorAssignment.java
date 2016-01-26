package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;

/**
 * Exception sensor assignment.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "exception-sensor-assignment")
public class ExceptionSensorAssignment extends AbstractClassSensorAssignment<IExceptionSensorConfig> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends IExceptionSensorConfig> getSensorConfigClass() {
		return ExceptionSensorConfig.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getSettings() {
		return Collections.emptyMap();
	}

}
