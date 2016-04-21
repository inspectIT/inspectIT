package rocks.inspectit.server.diagnosis.engine.session;

import java.util.Set;

import org.apache.commons.pool.BasePoolableObjectFactory;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils;

/**
 * BasePoolableObjectFactory implementation to create poolable {@link Session} instances.
 * SessionFactory is used by {@link SessionPool}.
 *
 * @param <I>
 *            The session input type
 * @param <R>
 *            The expected output type
 * @author Claudio Waldvogel
 * @see BasePoolableObjectFactory
 * @see SessionPool
 */
public class SessionFactory<I, R> extends BasePoolableObjectFactory<Session<I, R>> {

	/**
	 * The top-level {@link DiagnosisEngineConfiguration} configuration. Ths configuration might be
	 * used to configuration the factory.
	 *
	 * @see DiagnosisEngineConfiguration
	 * @see DiagnosisEngine
	 */
	private final DiagnosisEngineConfiguration<I, R> configuration;

	/**
	 * All {@link RuleDefinition} to be passed to the {@link Session}s. The definitions are
	 * extracted from {@link DiagnosisEngineConfiguration#ruleClasses}.
	 */
	private final Set<RuleDefinition> ruleDefinitions;

	/**
	 * Default constructor to create new {@link SessionFactory}s.
	 *
	 * @param configuration
	 *            The {@link DiagnosisEngineConfiguration}
	 */
	public SessionFactory(DiagnosisEngineConfiguration<I, R> configuration) {
		this.configuration = configuration;
		this.ruleDefinitions = prepareRuleDefinitions(configuration.getRuleClasses());
	}

	// -------------------------------------------------------------
	// Interface Implementation: BasePoolableObjectFactory
	// -------------------------------------------------------------

	@Override
	public Session<I, R> makeObject() throws Exception {
		return new Session<>(ruleDefinitions, configuration.getResultCollector(), ReflectionUtils.tryInstantiate(configuration.getStorageClass()));
	}

	@Override
	public void passivateObject(Session<I, R> session) throws Exception {
		session.passivate();
	}

	@Override
	public void destroyObject(Session<I, R> session) throws Exception {
		session.destroy();
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	/**
	 * Internal method to transform classes implementing a rule into {@link RuleDefinition}s.
	 *
	 * @param ruleClasses
	 *            The classes to be transformed.
	 * @return Set of {@link RuleDefinition}s.
	 */
	private Set<RuleDefinition> prepareRuleDefinitions(Set<Class<?>> ruleClasses) {
		return Rules.define(ruleClasses);
	}
}
