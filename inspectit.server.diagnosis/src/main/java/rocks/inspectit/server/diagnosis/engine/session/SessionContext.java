package rocks.inspectit.server.diagnosis.engine.session;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;

/**
 * A <code>SessionContext</code> represents the runtime information of a currently processing
 * {@link Session}. Each {@link SessionContext} is associated to exactly one {@link Session} and is
 * not viable without a {@link Session}. <br>
 * <code>SessionContext</code>s implement the same life cycle as {@link Session}:
 * {@link #activate(Object, SessionVariables)}, {@link #passivate()}, {@link #destroy()}.
 *
 * @param <I>
 *            The type of the input of this session.s
 * @author Claudio Waldvogel
 * @see Session
 */
public class SessionContext<I> {

	/**
	 * The input object the session is analyzing.
	 */
	private I input;

	/**
	 * ImmutableSet of all available {@link RuleDefinition}s within the {@link DiagnosisEngine}.
	 * This is a backup to restore the {@link #ruleSet} to an initial state as soon as the
	 * <code>SessionContext</code> gets activated.
	 */
	private final ImmutableSet<RuleDefinition> backupRules;

	/**
	 * The set of processable {@link RuleDefinition}s. As soon as a Session executed a
	 * {@link RuleDefinition}, this definition is evicted from the set. Thus it is easily possible
	 * to determine when a {@link Session} executed all possible {@link RuleDefinition}s.
	 *
	 * @see RuleDefinition
	 * @see Session
	 */
	private Set<RuleDefinition> ruleSet;

	/**
	 * The {@link IRuleOutputStorage} used in this {@link Session}.
	 *
	 * @see IRuleOutputStorage
	 */
	private IRuleOutputStorage storage;

	/**
	 * The available {@link SessionVariables} for this {@link Session} execution.
	 */
	private SessionVariables sessionVariables;

	/**
	 * Multimap of {@link RuleDefinition}s to {@link RuleInput}s to track which rules have already
	 * been excecuted for which inputs.
	 */
	private final Multimap<RuleDefinition, RuleInput> executions;

	/**
	 * Default constructor to create new <code>SessionContext</code>s.
	 *
	 * @param rules
	 *            The set of {@link RuleDefinition}s
	 * @param storage
	 *            The {@link IRuleOutputStorage} implementation
	 * @see RuleDefinition
	 * @see IRuleOutputStorage
	 */
	SessionContext(Set<RuleDefinition> rules, IRuleOutputStorage storage) {
		// Protected the initial rules from being manipulated
		this.backupRules = ImmutableSet.copyOf(rules);
		this.ruleSet = new HashSet<>();
		this.storage = storage;
		this.executions = ArrayListMultimap.create();
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle
	// -------------------------------------------------------------

	/**
	 * Activate the <code>SessionContext</code>. The {@link #ruleSet} is restored to
	 * {@link #backupRules}.
	 *
	 * @param input
	 *            The object to to be analyzed.
	 * @param variables
	 *            The valid {@link SessionVariables},
	 * @return The SessionContext itself.
	 * @see Session
	 * @see Session#activate(Object, SessionVariables)
	 * @see SessionVariables
	 */
	SessionContext<I> activate(I input, SessionVariables variables) {
		this.input = input;
		// ensure a shallow copy, we must never ever operate on the original RuleSet
		this.ruleSet.addAll(backupRules);
		this.sessionVariables = variables;
		return this;
	}

	/**
	 * Passivates the <code>SessionContext</code> by clearing th {@link #ruleSet}, the
	 * {@link #sessionVariables}, the {@link #storage}, and the {@link #input}.
	 *
	 * @return The SessionContext itself.
	 * @see Session
	 * @see Session#passivate()
	 */
	SessionContext<I> passivate() {
		this.input = null; // NOPMD
		this.ruleSet.clear();
		this.storage.clear();
		this.getExecutions().clear();
		this.sessionVariables = null; // NOPMD
		return this;
	}

	/**
	 * Passivates the <code>SessionContext</code> by wiping out {@link #ruleSet}, the
	 * {@link #sessionVariables}, the {@link #storage}, and the {@link #input}.
	 *
	 * @return The SessionContext itself.
	 * @see Session
	 * @see Session#destroy()
	 */
	SessionContext<I> destroy() {
		this.input = null; // NOPMD
		this.storage = null; // NOPMD
		this.ruleSet = null; // NOPMD
		this.sessionVariables = null; // NOPMD
		return this;
	}

	/**
	 * Track an execution of the given rule for the given input.
	 *
	 * @param ruleDefinition
	 *            {@link RuleDefinition} executed rule.
	 * @param input
	 *            {@link RuleInput} used for the rule execution.
	 */
	public void addExecution(RuleDefinition ruleDefinition, RuleInput input) {
		executions.put(ruleDefinition, input);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public I getInput() {
		return input;
	}

	/**
	 * Gets {@link #sessionVariables}.
	 *
	 * @return {@link #sessionVariables}
	 */
	public SessionVariables getSessionVariables() {
		return sessionVariables;
	}

	/**
	 * Gets {@link #ruleSet}.
	 *
	 * @return {@link #ruleSet}
	 */
	public Set<RuleDefinition> getRuleSet() {
		return ruleSet;
	}

	/**
	 * Gets {@link #storage}.
	 *
	 * @return {@link #storage}
	 */
	public IRuleOutputStorage getStorage() {
		return storage;
	}

	/**
	 * Gets {@link #executions}.
	 *
	 * @return {@link #executions}
	 */
	public Multimap<RuleDefinition, RuleInput> getExecutions() {
		return executions;
	}
}
