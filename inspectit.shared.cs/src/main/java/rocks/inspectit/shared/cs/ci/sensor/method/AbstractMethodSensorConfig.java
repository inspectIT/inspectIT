package rocks.inspectit.shared.cs.ci.sensor.method;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;

/**
 * Abstract class for all platform sensor configurations.
 *
 * @author Ivan Senic
 *
 */
@XmlSeeAlso({ ConnectionSensorConfig.class, HttpSensorConfig.class, InvocationSequenceSensorConfig.class, PreparedStatementParameterSensorConfig.class, PreparedStatementSensorConfig.class,
		StatementSensorConfig.class, TimerSensorConfig.class, AbstractRemoteSensorConfig.class })
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
		result = (prime * result) + this.getClass().hashCode();
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
