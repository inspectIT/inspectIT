package rocks.inspectit.agent.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultiset;

import rocks.inspectit.agent.java.Agent;
import rocks.inspectit.agent.java.IThreadTransformHelper;
import rocks.inspectit.agent.java.analyzer.impl.ClassHashHelper;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.agent.java.util.ClassUtil;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.communication.message.UpdatedInstrumentationMessage;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Handles incoming {@link UpdatedInstrumentationMessage}s and triggers a retransformation if
 * necessary.
 *
 * @author Marius Oehler
 *
 */
@Component
public class RetransformManager implements ApplicationListener<AgentMessagesReceivedEvent>, IInstrumentationAware {

	/**
	 * The logger for this class.
	 */
	@Log
	private Logger log;

	/**
	 * The used {@link Instrumentation}.
	 */
	private Instrumentation instrumentation;

	/**
	 * {@link IClassHashHelper}.
	 */
	@Autowired
	private ClassHashHelper classHashHelper;

	/**
	 * The {@link IThreadTransformHelper}.
	 */
	@Autowired
	private IThreadTransformHelper threadTransformHelper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInstrumentation(Instrumentation instrumentation) {
		if (log.isDebugEnabled()) {
			log.debug("Assign instrumentation to retransform manager.");
		}

		this.instrumentation = instrumentation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AgentMessagesReceivedEvent event) {
		if (event == null) {
			if (log.isDebugEnabled()) {
				log.debug("A 'null' event will not be processed.");
			}
			return;
		}

		// When we update to Spring v4, we should use @Conditional that the RetransformationManager
		// is not running if retransformation is not used.
		if (!Agent.agent.isUsingRetransformation()) {
			if (log.isInfoEnabled()) {
				log.info("Retransformation is disabled by the used retransformation strategy.");
			}
			return;
		}

		List<InstrumentationDefinition> instrumentationDefinitions = getInstrumentatioDefinitions(event.getAgentMessages());

		if (CollectionUtils.isEmpty(instrumentationDefinitions)) {
			return;
		}

		// remove out-dated duplicates
		Collection<InstrumentationDefinition> cleanedInstrumentationDefinitions = removeOutdatedInstrumentationDefinitions(instrumentationDefinitions);

		processInstrumentationDefinitions(cleanedInstrumentationDefinitions);
	}

	/**
	 * Returns a {@link Collection} containing all received {@link InstrumentationDefinition} which
	 * are contained in the given {@link IAgentMessage}s.
	 *
	 * @param agentMessages
	 *            the {@link IAgentMessage}s
	 * @return all existing {@link InstrumentationDefinition}s
	 */
	private List<InstrumentationDefinition> getInstrumentatioDefinitions(Collection<IAgentMessage<?>> agentMessages) {
		List<InstrumentationDefinition> instrumentationDefinitions = new ArrayList<InstrumentationDefinition>();
		for (IAgentMessage<?> message : agentMessages) {
			if (message instanceof UpdatedInstrumentationMessage) {
				instrumentationDefinitions.addAll(((UpdatedInstrumentationMessage) message).getMessageContent());
			}
		}
		return instrumentationDefinitions;
	}

	/**
	 * Removes duplicate {@link InstrumentationDefinition}s which are out-dated or would have been
	 * overwritten by a newer {@link InstrumentationDefinition}.
	 *
	 * @param instrumentationDefinitions
	 *            the {@link InstrumentationDefinition}s of union
	 * @return {@link Collection} of union {@link InstrumentationDefinition}s
	 */
	private Collection<InstrumentationDefinition> removeOutdatedInstrumentationDefinitions(List<InstrumentationDefinition> instrumentationDefinitions) {
		Map<String, InstrumentationDefinition> definitionMap = new HashMap<String, InstrumentationDefinition>();
		for (InstrumentationDefinition iDefinition : instrumentationDefinitions) {
			definitionMap.put(iDefinition.getClassName(), iDefinition);
		}
		return definitionMap.values();
	}

	/**
	 * Process the given [@link {@link InstrumentationDefinition}s. In this case, they are getting
	 * registered on the respective class, subsequently, the classes are getting retransformed.
	 *
	 * @param instrumentationDefinitions
	 *            {@link Collection} of {@link InstrumentationDefinition}
	 */
	private void processInstrumentationDefinitions(Collection<InstrumentationDefinition> instrumentationDefinitions) {
		if (log.isInfoEnabled()) {
			log.info("Trying to retransform {} class(es)", instrumentationDefinitions.size());
		}

		Collection<Class<?>> classesToRetransform = new ArrayList<Class<?>>();

		// create map of instrumentation definitions (for fast look-up)
		Map<String, InstrumentationDefinition> instrumentationDefinitionMap = new HashMap<String, InstrumentationDefinition>();
		for (InstrumentationDefinition definition : instrumentationDefinitions) {
			instrumentationDefinitionMap.put(definition.getClassName(), definition);

			// register new implementation
			classHashHelper.registerInstrumentationDefinition(definition.getClassName(), definition);
		}

		HashMultiset<String> classLoaderMultiset = HashMultiset.<String> create();

		Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
		for (Class<?> clazz : loadedClasses) {
			try {
				if (!instrumentation.isModifiableClass(clazz)) {
					continue;
				}

				String className = clazz.getName();

				if (instrumentationDefinitionMap.containsKey(className)) {
					classesToRetransform.add(clazz);

					String classLoaderName = ClassUtil.getClassLoaderName(clazz, "unknown classloader");
					classLoaderMultiset.add(classLoaderName);

					if (log.isDebugEnabled()) {
						log.debug("|-{} : {} (is instrumented: {})", className, classLoaderName, !instrumentationDefinitionMap.get(className).getMethodInstrumentationConfigs().isEmpty());
					}
				}
			} catch (Exception e) {
				// This catch prevents the thread/loop from crashing.
				if (log.isDebugEnabled()) {
					log.debug("Failed to add class to the list of classes to retransform.", e);
				}
			}
		}

		if (log.isInfoEnabled()) {
			log.info("|-Going to retransform {} loaded class(es) of {} class loader(s).", classLoaderMultiset.size(), classLoaderMultiset.elementSet().size());
		}

		if (CollectionUtils.isNotEmpty(classesToRetransform)) {
			try {
				threadTransformHelper.setThreadTransformDisabled(false);
				instrumentation.retransformClasses(classesToRetransform.toArray(new Class[classesToRetransform.size()]));
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Failed to triggering retransformation of loaded classes.", e);
				}
			} finally {
				threadTransformHelper.setThreadTransformDisabled(true);
			}
		}

		if (log.isInfoEnabled()) {
			log.info("|-Retransformation finished.");
		}
	}
}
