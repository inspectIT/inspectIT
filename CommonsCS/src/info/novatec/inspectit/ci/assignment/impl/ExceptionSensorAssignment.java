package info.novatec.inspectit.ci.assignment.impl;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.config.applier.ExceptionSensorInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;

import java.util.Collections;
import java.util.Map;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getSettings() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInstrumentationApplier getInstrumentationApplier(Environment environment, IRegistrationService registrationService) {
		return new ExceptionSensorInstrumentationApplier(this, environment, registrationService);
	}

}
