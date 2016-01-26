package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.pattern.IMatchPattern;

/**
 * Configuration that will be sent to the Agent when he initially connects to the CMR.
 *
 * @author Ivan Senic
 *
 */
public class AgentConfig {

	/**
	 * The id for the agent.
	 */
	private long platformId;

	/**
	 * Denotes if the class cache for the agent exist on the CMR. If this is set to
	 * <code>true</code> agent can use its internal sending classes cache, otherwise agent should
	 * send all the loaded classes to the CMR ignoring the sending cache state.
	 */
	private boolean classCacheExistsOnCmr;

	/**
	 * Collection of the platform sensor types that should be active.
	 */
	private Collection<PlatformSensorTypeConfig> platformSensorTypeConfigs;

	/**
	 * Collection of the method sensor types that should be active.
	 */
	private Collection<MethodSensorTypeConfig> methodSensorTypeConfigs;

	/**
	 * Configuration of the exception sensor type.
	 */
	private ExceptionSensorTypeConfig exceptionSensorTypeConfig;

	/**
	 * Buffer strategy.
	 */
	private StrategyConfig bufferStrategyConfig;

	/**
	 * Sending strategy.
	 */
	private StrategyConfig sendingStrategyConfig;

	/**
	 * Collection of the exclude classes patterns.
	 */
	private Collection<IMatchPattern> excludeClassesPatterns;

	/**
	 * Set of known {@link InstrumentationDefinition} for the agent that can be used by the Agent right
	 * away. Each {@link InstrumentationDefinition} is mapped to the collection of the class hashes it
	 * relates to.
	 */
	private Map<Collection<String>, InstrumentationDefinition> initialInstrumentationResults;

	/**
	 * Configuration info that can be printed out on the Agent side to help understanding which
	 * mapping/environment is used.
	 */
	private String configurationInfo;

	/**
	 * Gets {@link #platformId}.
	 *
	 * @return {@link #platformId}
	 */
	public long getPlatformId() {
		return platformId;
	}

	/**
	 * Sets {@link #platformId}.
	 *
	 * @param platformId
	 *            New value for {@link #platformId}
	 */
	public void setPlatformId(long platformId) {
		this.platformId = platformId;
	}

	/**
	 * Gets {@link #classCacheExistsOnCmr}.
	 *
	 * @return {@link #classCacheExistsOnCmr}
	 */
	public boolean isClassCacheExistsOnCmr() {
		return classCacheExistsOnCmr;
	}

	/**
	 * Sets {@link #classCacheExistsOnCmr}.
	 *
	 * @param classCacheExistsOnCmr
	 *            New value for {@link #classCacheExistsOnCmr}
	 */
	public void setClassCacheExistsOnCmr(boolean classCacheExistsOnCmr) {
		this.classCacheExistsOnCmr = classCacheExistsOnCmr;
	}

	/**
	 * Gets {@link #platformSensorTypeConfigs}.
	 *
	 * @return {@link #platformSensorTypeConfigs}
	 */
	public Collection<PlatformSensorTypeConfig> getPlatformSensorTypeConfigs() {
		return platformSensorTypeConfigs;
	}

	/**
	 * Sets {@link #platformSensorTypeConfigs}.
	 *
	 * @param platformSensorTypeConfigs
	 *            New value for {@link #platformSensorTypeConfigs}
	 */
	public void setPlatformSensorTypeConfigs(Collection<PlatformSensorTypeConfig> platformSensorTypeConfigs) {
		this.platformSensorTypeConfigs = platformSensorTypeConfigs;
	}

	/**
	 * Gets {@link #methodSensorTypeConfigs}.
	 *
	 * @return {@link #methodSensorTypeConfigs}
	 */
	public Collection<MethodSensorTypeConfig> getMethodSensorTypeConfigs() {
		return methodSensorTypeConfigs;
	}

	/**
	 * Sets {@link #methodSensorTypeConfigs}.
	 *
	 * @param methodSensorTypeConfigs
	 *            New value for {@link #methodSensorTypeConfigs}
	 */
	public void setMethodSensorTypeConfigs(Collection<MethodSensorTypeConfig> methodSensorTypeConfigs) {
		this.methodSensorTypeConfigs = methodSensorTypeConfigs;
	}

	/**
	 * Return the {@link MethodSensorTypeConfig} for given sensor class name.
	 *
	 * @param sensorClassName
	 *            Sensor class name.
	 * @return {@link MethodSensorTypeConfig} or <code>null</code> if such does not exists in the
	 *         configuration.
	 */
	public MethodSensorTypeConfig getMethodSensorTypeConfig(String sensorClassName) {
		if (StringUtils.isNotBlank(sensorClassName)) {
			for (MethodSensorTypeConfig methodSensorTypeConfig : methodSensorTypeConfigs) {
				if (sensorClassName.equals(methodSensorTypeConfig.getClassName())) {
					return methodSensorTypeConfig;
				}
			}
		}

		return null;
	}

	/**
	 * Gets {@link #exceptionSensorTypeConfig}.
	 *
	 * @return {@link #exceptionSensorTypeConfig}
	 */
	public ExceptionSensorTypeConfig getExceptionSensorTypeConfig() {
		return exceptionSensorTypeConfig;
	}

	/**
	 * Sets {@link #exceptionSensorTypeConfig}.
	 *
	 * @param exceptionSensorTypeConfig
	 *            New value for {@link #exceptionSensorTypeConfig}
	 */
	public void setExceptionSensorTypeConfig(ExceptionSensorTypeConfig exceptionSensorTypeConfig) {
		this.exceptionSensorTypeConfig = exceptionSensorTypeConfig;
	}

	/**
	 * Gets {@link #bufferStrategyConfig}.
	 *
	 * @return {@link #bufferStrategyConfig}
	 */
	public StrategyConfig getBufferStrategyConfig() {
		return bufferStrategyConfig;
	}

	/**
	 * Sets {@link #bufferStrategyConfig}.
	 *
	 * @param bufferStrategyConfig
	 *            New value for {@link #bufferStrategyConfig}
	 */
	public void setBufferStrategyConfig(StrategyConfig bufferStrategyConfig) {
		this.bufferStrategyConfig = bufferStrategyConfig;
	}

	/**
	 * Gets {@link #sendingStrategyConfig}.
	 *
	 * @return {@link #sendingStrategyConfig}
	 */
	public StrategyConfig getSendingStrategyConfig() {
		return sendingStrategyConfig;
	}

	/**
	 * Sets {@link #sendingStrategyConfig}.
	 *
	 * @param sendingStrategyConfig
	 *            New value for {@link #sendingStrategyConfig}
	 */
	public void setSendingStrategyConfig(StrategyConfig sendingStrategyConfig) {
		this.sendingStrategyConfig = sendingStrategyConfig;
	}

	/**
	 * Gets {@link #excludeClassesPatterns}.
	 *
	 * @return {@link #excludeClassesPatterns}
	 */
	public Collection<IMatchPattern> getExcludeClassesPatterns() {
		return excludeClassesPatterns;
	}

	/**
	 * Sets {@link #excludeClassesPatterns}.
	 *
	 * @param excludeClassesPatterns
	 *            New value for {@link #excludeClassesPatterns}
	 */
	public void setExcludeClassesPatterns(Collection<IMatchPattern> excludeClassesPatterns) {
		this.excludeClassesPatterns = excludeClassesPatterns;
	}

	/**
	 * Gets {@link #initialInstrumentationResults}.
	 *
	 * @return {@link #initialInstrumentationResults}
	 */
	public Map<Collection<String>, InstrumentationDefinition> getInitialInstrumentationResults() {
		return initialInstrumentationResults;
	}

	/**
	 * Sets {@link #initialInstrumentationResults}.
	 *
	 * @param initialInstrumentationResults
	 *            New value for {@link #initialInstrumentationResults}
	 */
	public void setInitialInstrumentationResults(Map<Collection<String>, InstrumentationDefinition> initialInstrumentationResults) {
		this.initialInstrumentationResults = initialInstrumentationResults;
	}

	/**
	 * Gets {@link #configurationInfo}.
	 *
	 * @return {@link #configurationInfo}
	 */
	public String getConfigurationInfo() {
		return configurationInfo;
	}

	/**
	 * Sets {@link #configurationInfo}.
	 *
	 * @param configurationInfo
	 *            New value for {@link #configurationInfo}
	 */
	public void setConfigurationInfo(String configurationInfo) {
		this.configurationInfo = configurationInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bufferStrategyConfig == null) ? 0 : bufferStrategyConfig.hashCode());
		result = prime * result + (classCacheExistsOnCmr ? 1231 : 1237);
		result = prime * result + ((configurationInfo == null) ? 0 : configurationInfo.hashCode());
		result = prime * result + ((exceptionSensorTypeConfig == null) ? 0 : exceptionSensorTypeConfig.hashCode());
		result = prime * result + ((excludeClassesPatterns == null) ? 0 : excludeClassesPatterns.hashCode());
		result = prime * result + ((initialInstrumentationResults == null) ? 0 : initialInstrumentationResults.hashCode());
		result = prime * result + ((methodSensorTypeConfigs == null) ? 0 : methodSensorTypeConfigs.hashCode());
		result = prime * result + (int) (platformId ^ (platformId >>> 32));
		result = prime * result + ((platformSensorTypeConfigs == null) ? 0 : platformSensorTypeConfigs.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AgentConfig other = (AgentConfig) obj;
		if (bufferStrategyConfig == null) {
			if (other.bufferStrategyConfig != null) {
				return false;
			}
		} else if (!bufferStrategyConfig.equals(other.bufferStrategyConfig)) {
			return false;
		}
		if (classCacheExistsOnCmr != other.classCacheExistsOnCmr) {
			return false;
		}
		if (configurationInfo == null) {
			if (other.configurationInfo != null) {
				return false;
			}
		} else if (!configurationInfo.equals(other.configurationInfo)) {
			return false;
		}
		if (exceptionSensorTypeConfig == null) {
			if (other.exceptionSensorTypeConfig != null) {
				return false;
			}
		} else if (!exceptionSensorTypeConfig.equals(other.exceptionSensorTypeConfig)) {
			return false;
		}
		if (excludeClassesPatterns == null) {
			if (other.excludeClassesPatterns != null) {
				return false;
			}
		} else if (!excludeClassesPatterns.equals(other.excludeClassesPatterns)) {
			return false;
		}
		if (initialInstrumentationResults == null) {
			if (other.initialInstrumentationResults != null) {
				return false;
			}
		} else if (!initialInstrumentationResults.equals(other.initialInstrumentationResults)) {
			return false;
		}
		if (methodSensorTypeConfigs == null) {
			if (other.methodSensorTypeConfigs != null) {
				return false;
			}
		} else if (!methodSensorTypeConfigs.equals(other.methodSensorTypeConfigs)) {
			return false;
		}
		if (platformId != other.platformId) {
			return false;
		}
		if (platformSensorTypeConfigs == null) {
			if (other.platformSensorTypeConfigs != null) {
				return false;
			}
		} else if (!platformSensorTypeConfigs.equals(other.platformSensorTypeConfigs)) {
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
