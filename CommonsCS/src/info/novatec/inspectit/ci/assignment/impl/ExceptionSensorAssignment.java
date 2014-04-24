package info.novatec.inspectit.ci.assignment.impl;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;

import javax.xml.bind.annotation.XmlRootElement;

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

}
