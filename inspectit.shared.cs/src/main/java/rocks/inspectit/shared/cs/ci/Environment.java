package rocks.inspectit.shared.cs.ci;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.factory.ConfigurationDefaultsFactory;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.ListSendingStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.SimpleBufferStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.SizeBufferStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.TimeSendingStrategyConfig;

/**
 * Environment definition. Defines sending & buffer strategies, sensors and their options. Also has
 * a list of profiles to include.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "environment")
public class Environment extends AbstractCiData {

	/**
	 * Configuration for the sending strategy.
	 * <p>
	 * Default is {@link TimeSendingStrategyConfig}.
	 */
	@XmlElementRefs({ @XmlElementRef(type = TimeSendingStrategyConfig.class), @XmlElementRef(type = ListSendingStrategyConfig.class) })
	private IStrategyConfig sendingStrategyConfig = ConfigurationDefaultsFactory.getDefaultSendingStrategy();

	/**
	 * Configuration for the buffer strategy.
	 * <p>
	 * Default is {@link SimpleBufferStrategyConfig}.
	 */
	@XmlElementRefs({ @XmlElementRef(type = SimpleBufferStrategyConfig.class), @XmlElementRef(type = SizeBufferStrategyConfig.class) })
	private IStrategyConfig bufferStrategyConfig = ConfigurationDefaultsFactory.getDefaultBufferStrategy();

	/**
	 * List of the platform sensors configurations.
	 */
	@XmlElementWrapper(name = "platform-sensor-configs")
	@XmlElementRefs({ @XmlElementRef(type = ClassLoadingSensorConfig.class), @XmlElementRef(type = CompilationSensorConfig.class), @XmlElementRef(type = CpuSensorConfig.class),
		@XmlElementRef(type = MemorySensorConfig.class), @XmlElementRef(type = RuntimeSensorConfig.class), @XmlElementRef(type = SystemSensorConfig.class),
		@XmlElementRef(type = ThreadSensorConfig.class) })
	private final List<IPlatformSensorConfig> platformSensorConfigs = ConfigurationDefaultsFactory.getAvailablePlatformSensorConfigs();

	/**
	 * List of the method sensor configurations.
	 */
	@XmlElementWrapper(name = "method-sensor-configs")
	@XmlElementRefs({ @XmlElementRef(type = ConnectionSensorConfig.class), @XmlElementRef(type = HttpSensorConfig.class), @XmlElementRef(type = InvocationSequenceSensorConfig.class),
		@XmlElementRef(type = PreparedStatementParameterSensorConfig.class), @XmlElementRef(type = PreparedStatementSensorConfig.class), @XmlElementRef(type = StatementSensorConfig.class),
		@XmlElementRef(type = TimerSensorConfig.class), @XmlElementRef(type = Log4jLoggingSensorConfig.class) })
	private final List<IMethodSensorConfig> methodSensorConfigs = ConfigurationDefaultsFactory.getAvailableMethodSensorConfigs();

	/**
	 * Exception sensor config. We have only one.
	 */
	@XmlElementRef(type = ExceptionSensorConfig.class)
	private final IExceptionSensorConfig exceptionSensorConfig = ConfigurationDefaultsFactory.getDefaultExceptionSensorConfig();

	/**
	 * List of profile ids. Needed for saving to the XML.
	 */
	@XmlElementWrapper(name = "profile-ids", required = false)
	@XmlElement(name = "profile-id")
	private Set<String> profileIds = new HashSet<>(0);

	/**
	 * If class loading delegation is on or off.
	 */
	@XmlElement(name = "classLoadingDelegation")
	private boolean classLoadingDelegation = true;

	/**
	 * Returns the {@link IMethodSensorConfig} for the given {@link IMethodSensorConfig} class.
	 *
	 * @param clazz
	 *            Class to look for.
	 * @return Returns the {@link IMethodSensorConfig} for the given {@link IMethodSensorConfig}
	 *         class.
	 */
	public IMethodSensorConfig getMethodSensorTypeConfig(Class<? extends IMethodSensorConfig> clazz) {
		for (IMethodSensorConfig config : methodSensorConfigs) {
			if (config.getClass().equals(clazz)) {
				return config;
			}
		}
		return null;
	}

	/**
	 * Gets {@link #sendingStrategyConfig}.
	 *
	 * @return {@link #sendingStrategyConfig}
	 */
	public IStrategyConfig getSendingStrategyConfig() {
		return sendingStrategyConfig;
	}

	/**
	 * Sets {@link #sendingStrategyConfig}.
	 *
	 * @param sendingStrategyConfig
	 *            New value for {@link #sendingStrategyConfig}
	 */
	public void setSendingStrategyConfig(IStrategyConfig sendingStrategyConfig) {
		this.sendingStrategyConfig = sendingStrategyConfig;
	}

	/**
	 * Gets {@link #bufferStrategyConfig}.
	 *
	 * @return {@link #bufferStrategyConfig}
	 */
	public IStrategyConfig getBufferStrategyConfig() {
		return bufferStrategyConfig;
	}

	/**
	 * Sets {@link #bufferStrategyConfig}.
	 *
	 * @param bufferStrategyConfig
	 *            New value for {@link #bufferStrategyConfig}
	 */
	public void setBufferStrategyConfig(IStrategyConfig bufferStrategyConfig) {
		this.bufferStrategyConfig = bufferStrategyConfig;
	}

	/**
	 * Gets {@link #platformSensorConfigs}.
	 *
	 * @return {@link #platformSensorConfigs}
	 */
	public List<IPlatformSensorConfig> getPlatformSensorConfigs() {
		return platformSensorConfigs;
	}

	/**
	 * Gets {@link #methodSensorConfigs}.
	 *
	 * @return {@link #methodSensorConfigs}
	 */
	public List<IMethodSensorConfig> getMethodSensorConfigs() {
		return methodSensorConfigs;
	}

	/**
	 * Gets {@link #exceptionSensorConfig}.
	 *
	 * @return {@link #exceptionSensorConfig}
	 */
	public IExceptionSensorConfig getExceptionSensorConfig() {
		return exceptionSensorConfig;
	}

	/**
	 * Gets {@link #profileIds}.
	 *
	 * @return {@link #profileIds}
	 */
	public Set<String> getProfileIds() {
		return profileIds;
	}

	/**
	 * Sets {@link #profileIds}.
	 *
	 * @param profileIds
	 *            New value for {@link #profileIds}
	 */
	public void setProfileIds(Set<String> profileIds) {
		this.profileIds = profileIds;
	}

	/**
	 * Gets {@link #classLoadingDelegation}.
	 *
	 * @return {@link #classLoadingDelegation}
	 */
	public boolean isClassLoadingDelegation() {
		return classLoadingDelegation;
	}

	/**
	 * Sets {@link #classLoadingDelegation}.
	 *
	 * @param classLoadingDelegation
	 *            New value for {@link #classLoadingDelegation}
	 */
	public void setClassLoadingDelegation(boolean classLoadingDelegation) {
		this.classLoadingDelegation = classLoadingDelegation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bufferStrategyConfig == null) ? 0 : bufferStrategyConfig.hashCode());
		result = prime * result + (classLoadingDelegation ? 1231 : 1237);
		result = prime * result + ((exceptionSensorConfig == null) ? 0 : exceptionSensorConfig.hashCode());
		result = prime * result + ((methodSensorConfigs == null) ? 0 : methodSensorConfigs.hashCode());
		result = prime * result + ((platformSensorConfigs == null) ? 0 : platformSensorConfigs.hashCode());
		result = prime * result + ((profileIds == null) ? 0 : profileIds.hashCode());
		result = prime * result + ((sendingStrategyConfig == null) ? 0 : sendingStrategyConfig.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Environment other = (Environment) obj;
		if (bufferStrategyConfig == null) {
			if (other.bufferStrategyConfig != null) {
				return false;
			}
		} else if (!bufferStrategyConfig.equals(other.bufferStrategyConfig)) {
			return false;
		}
		if (classLoadingDelegation != other.classLoadingDelegation) {
			return false;
		}
		if (exceptionSensorConfig == null) {
			if (other.exceptionSensorConfig != null) {
				return false;
			}
		} else if (!exceptionSensorConfig.equals(other.exceptionSensorConfig)) {
			return false;
		}
		if (methodSensorConfigs == null) {
			if (other.methodSensorConfigs != null) {
				return false;
			}
		} else if (!methodSensorConfigs.equals(other.methodSensorConfigs)) {
			return false;
		}
		if (platformSensorConfigs == null) {
			if (other.platformSensorConfigs != null) {
				return false;
			}
		} else if (!platformSensorConfigs.equals(other.platformSensorConfigs)) {
			return false;
		}
		if (profileIds == null) {
			if (other.profileIds != null) {
				return false;
			}
		} else if (!profileIds.equals(other.profileIds)) {
			return false;
		}
		if (sendingStrategyConfig == null) {
			if (other.sendingStrategyConfig != null) {
				return false;
			}
		} else if (!sendingStrategyConfig.equals(other.sendingStrategyConfig)) {
			return false;
		}
		return true;
	}

}
