package info.novatec.inspectit.ci.sensor.method;

import info.novatec.inspectit.ci.sensor.method.impl.ConnectionMetaDataSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.HttpSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.StatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.TimerSensorConfig;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class for all platform sensor configurations.
 * 
 * @author Ivan Senic
 * 
 */
@XmlSeeAlso({ ConnectionMetaDataSensorConfig.class, ConnectionSensorConfig.class, HttpSensorConfig.class, InvocationSequenceSensorConfig.class, PreparedStatementParameterSensorConfig.class,
		PreparedStatementSensorConfig.class, StatementSensorConfig.class, TimerSensorConfig.class })
public abstract class AbstractMethodSensorConfig implements IMethodSensorConfig {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

}
