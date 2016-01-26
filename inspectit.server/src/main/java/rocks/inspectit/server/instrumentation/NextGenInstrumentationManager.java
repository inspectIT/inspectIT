package rocks.inspectit.server.instrumentation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheModificationException;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Manager for handling the instrumentation decisions for the {@link Type}s that are send by the
 * agent. Also handles the agent registration.
 *
 * @author Ivan Senic
 *
 */
@Component
public class NextGenInstrumentationManager implements ApplicationListener<AgentDeletedEvent> {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Factory for creating new class caches.
	 */
	@Autowired
	private ObjectFactory<ClassCache> classCacheFactory;

	/**
	 * Factory for creating new configuration holder.
	 */
	@Autowired
	private ObjectFactory<ConfigurationHolder> configurationHolderFactory;

	/**
	 * Registration service.
	 */
	@Autowired
	private IRegistrationService registrationService;

	/**
	 * Configuration resolver.
	 */
	@Autowired
	private ConfigurationResolver configurationResolver;

	/**
	 * Executor for dealing with configuration updates.
	 */
	@Autowired
	@Qualifier("agentServiceExecutorService")
	private ExecutorService executor;

	/**
	 * Cache for the agents and it's used class cache, environments and configurations.
	 */
	private final ConcurrentHashMap<Long, AgentCacheEntry> agentCacheMap = new ConcurrentHashMap<>();

	/**
	 * {@inheritDoc}
	 */
	public AgentConfig register(List<String> definedIPs, String agentName, String version) throws BusinessException {
		// load environment for the agent
		Environment environment = configurationResolver.getEnvironmentForAgent(definedIPs, agentName);

		// if environment load is success register agent
		long id = registrationService.registerPlatformIdent(definedIPs, agentName, version);

		// get or create the agent cache entry
		AgentCacheEntry agentCacheEntry = getAgentCacheEntry(id);
		ClassCache classCache = agentCacheEntry.getClassCache();
		ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();

		// check if this agent was already registered and we have environment
		Environment cachedEnvironment = configurationHolder.getEnvironment();

		// if we have same environment and configuration return configuration
		if (configurationHolder.isInitialized() && Objects.equals(environment, cachedEnvironment)) {
			AgentConfig agentConfiguration = configurationHolder.getAgentConfiguration();
			Map<Collection<String>, InstrumentationDefinition> initial = classCache.getInstrumentationService().getInstrumentationResultsWithHashes();
			agentConfiguration.setInitialInstrumentationResults(initial);
			agentConfiguration.setClassCacheExistsOnCmr(true);
			return agentConfiguration;
		}

		// else kick the configuration creator update
		configurationHolder.update(environment, id);

		// return configuration
		return configurationHolder.getAgentConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregister(long platformIdent) throws BusinessException {
		registrationService.unregisterPlatformIdent(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public InstrumentationDefinition analyze(long platformIdent, String hash, Type sentType) throws BusinessException {
		AgentCacheEntry agentCacheEntry = agentCacheMap.get(Long.valueOf(platformIdent));
		if (null == agentCacheEntry) {
			throw new BusinessException("Instrumenting class with hash '" + hash + "' for the agent with id=" + platformIdent, AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}

		ClassCache classCache = agentCacheEntry.getClassCache();
		ImmutableType type = classCache.getLookupService().findByHash(hash);
		// if does not exists, parse, merge & configure instrumentation points
		if (null == type) {
			try {
				classCache.getModificationService().merge(sentType);

				// get real object after merging
				type = classCache.getLookupService().findByHash(hash);
			} catch (ClassCacheModificationException e) {
				log.error("Type can not be analyzed due to the exception during merging.", e);
				return null;
			}
		}

		// no need to do anything with types that are not classes
		// just return
		if (!type.isClass()) {
			return null;
		}

		ImmutableClassType classType = type.castToClass();
		ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();

		// if configuration holder is for any reason not initialized we can not define if it can be
		// instrumented
		if (!configurationHolder.isInitialized()) {
			return null;
		}

		return classCache.getInstrumentationService().addAndGetInstrumentationResult(classType, configurationHolder.getAgentConfiguration(), configurationHolder.getInstrumentationAppliers());
	}

	/**
	 * Generates {@link RefreshInstrumentationTimestampsJob} for the given method IDs.
	 *
	 * @param methodToSensorMap
	 *            methods being instrumented on agent
	 */
	public void instrumentationApplied(final Map<Long, long[]> methodToSensorMap) {
		// Asynchronously refresh idents
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (Entry<Long, long[]> entry : methodToSensorMap.entrySet()) {
					long methodId = entry.getKey().longValue();
					long[] sensorIds = entry.getValue();

					for (long sensorID : sensorIds) {
						registrationService.addSensorTypeToMethod(sensorID, methodId);
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AgentDeletedEvent event) {
		PlatformIdent platformIdent = event.getPlatformIdent();
		agentCacheMap.remove(platformIdent.getId());
	}

	/**
	 * Returns agent cache entry for the agent.
	 *
	 * @param platformIdent
	 *            Agent id.
	 * @return {@link AgentCacheEntry}
	 */
	private AgentCacheEntry getAgentCacheEntry(long platformIdent) {
		AgentCacheEntry agentCacheEntry = agentCacheMap.get(Long.valueOf(platformIdent));
		if (null == agentCacheEntry) {
			ClassCache classCache = classCacheFactory.getObject();
			ConfigurationHolder configurationHolder = configurationHolderFactory.getObject();
			agentCacheEntry = new AgentCacheEntry(platformIdent, classCache, configurationHolder);
			AgentCacheEntry existing = agentCacheMap.putIfAbsent(Long.valueOf(platformIdent), agentCacheEntry);
			if (null != existing) {
				agentCacheEntry = existing;
			}
		}
		return agentCacheEntry;
	}

	/**
	 * Gets {@link #agentCacheMap}.
	 *
	 * @return {@link #agentCacheMap}
	 */
	public Map<Long, AgentCacheEntry> getAgentCacheMap() {
		return Collections.unmodifiableMap(agentCacheMap);
	}

}
