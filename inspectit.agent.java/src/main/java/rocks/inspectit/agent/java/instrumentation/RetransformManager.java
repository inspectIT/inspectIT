package rocks.inspectit.agent.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.analyzer.impl.ClassHashHelper;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.AbstractAgentMessage;
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
public class RetransformManager implements ApplicationListener<AgentMessagesReceivedEvent> {

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
	 * Sets the {@link #instrumentation}.
	 *
	 * @param instrumentation
	 *            {@link Instrumentation} to use
	 */
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

		Collection<InstrumentationDefinition> instrumentationDefinitions = getInstrumentatioDefinitions(event.getAgentMessages());

		if (CollectionUtils.isEmpty(instrumentationDefinitions)) {
			return;
		}

		// remove out-dated duplicates
		instrumentationDefinitions = removeOutdatedInstrumentationDefinitions(instrumentationDefinitions);

		processInstrumentationDefinitions(instrumentationDefinitions);
	}

	/**
	 * Returns a {@link Collection} containing all received {@link InstrumentationDefinition} which
	 * are contained in the given {@link AbstractAgentMessage}s.
	 *
	 * @param agentMessages
	 *            the {@link AbstractAgentMessage}s
	 * @return all existing {@link InstrumentationDefinition}s
	 */
	private Collection<InstrumentationDefinition> getInstrumentatioDefinitions(Collection<AbstractAgentMessage> agentMessages) {
		Collection<InstrumentationDefinition> instrumentationDefinitions = new ArrayList<InstrumentationDefinition>();
		for (AbstractAgentMessage message : agentMessages) {
			if (message instanceof UpdatedInstrumentationMessage) {
				instrumentationDefinitions.addAll(((UpdatedInstrumentationMessage) message).getUpdatedInstrumentationDefinitions());
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
	private Collection<InstrumentationDefinition> removeOutdatedInstrumentationDefinitions(Collection<InstrumentationDefinition> instrumentationDefinitions) {
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
			log.info("Retransform {} class(es):", instrumentationDefinitions.size());
		}

		Collection<Class<?>> classesToRetransform = new ArrayList<Class<?>>();

		Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
		for (Class<?> clazz : loadedClasses) {
			for (InstrumentationDefinition instrumentationDefinition : instrumentationDefinitions) {
				if (instrumentationDefinition.getClassName().equals(clazz.getCanonicalName())) {
					classHashHelper.registerInstrumentationDefinition(clazz.getCanonicalName(), instrumentationDefinition);
					classesToRetransform.add(clazz);

					if (log.isInfoEnabled()) {
						log.info("|-{} (is instrumented: {})", clazz.getCanonicalName(), !instrumentationDefinition.getMethodInstrumentationConfigs().isEmpty());
					}
				}
			}
		}

		try {
			if (CollectionUtils.isNotEmpty(classesToRetransform)) {
				instrumentation.retransformClasses(classesToRetransform.toArray(new Class[classesToRetransform.size()]));
			}
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to triggering retransformation of loaded classes.", e);
			}
		}
	}
}
