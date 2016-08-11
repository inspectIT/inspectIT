package rocks.inspectit.shared.cs.ci.sensor.method;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpUrlConnectionInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQConsumerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQListenerExtractorSensorConfig;

/**
 * Abstract class for all remote sensor configurations.
 *
 * @author Thomas Kluge
 *
 */
@XmlSeeAlso({ RemoteHttpExtractorSensorConfig.class, RemoteMQConsumerExtractorSensorConfig.class, RemoteMQListenerExtractorSensorConfig.class, RemoteApacheHttpClientV40InserterSensorConfig.class,
		RemoteHttpUrlConnectionInserterSensorConfig.class, RemoteJettyHttpClientV61InserterSensorConfig.class, RemoteMQInserterSensorConfig.class })
public abstract class AbstractRemoteSensorConfig implements IMethodSensorConfig {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PriorityEnum getPriority() {
		return PriorityEnum.HIGH;
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
