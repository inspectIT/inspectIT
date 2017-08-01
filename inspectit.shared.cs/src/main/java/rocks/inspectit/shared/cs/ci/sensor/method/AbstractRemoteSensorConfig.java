package rocks.inspectit.shared.cs.ci.sensor.method;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40SensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteAsyncApacheHttpClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJavaHttpServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61ClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsListenerServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteManualServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteSpringRestTemplateClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteUrlConnectionClientSensorConfig;

/**
 * Abstract class for all remote sensor configurations.
 *
 * @author Thomas Kluge
 *
 */
@XmlSeeAlso({ RemoteJavaHttpServerSensorConfig.class, RemoteJmsListenerServerSensorConfig.class, RemoteApacheHttpClientV40SensorConfig.class, RemoteAsyncApacheHttpClientSensorConfig.class,
	RemoteUrlConnectionClientSensorConfig.class, RemoteJettyHttpClientV61ClientSensorConfig.class, RemoteSpringRestTemplateClientSensorConfig.class, RemoteJmsClientSensorConfig.class,
	RemoteManualServerSensorConfig.class })
public abstract class AbstractRemoteSensorConfig implements IMethodSensorConfig {

	/**
	 * If this remote senor config is for a server side remote sensor.
	 *
	 * @return Returns <code>true</code> if this remote senor config is for a server side remote
	 *         sensor, <code>false</code> otherwise.
	 */
	public abstract boolean isServerSide();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PriorityEnum getPriority() {
		return PriorityEnum.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return true;
	}

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
