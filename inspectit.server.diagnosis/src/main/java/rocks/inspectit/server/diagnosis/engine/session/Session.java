package rocks.inspectit.server.diagnosis.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.FireCondition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.exception.SessionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * The Session is the core class of the {@link IDiagnosisEngine}. It executes all rules, stores
 * interim results, and prepares the final results by utilizing the {@link ISessionResultCollector}.
 * To ensure a proper execution it defines an explicit life cycle. To ensure a compliance with the
 * life cycle the current state of session is held in a {@link State} object. Additional runtime
 * information is stored in a {@link SessionContext}.
 *
 *
 * The lifecycle of a session is as follows:
 * <ul>
 * <li>{@link #activate(Object, SessionVariables)} Prepares the session for the next execution.</li>
 * <li>{@link #process()} Executes all rules until no more rule can be executed.</li>
 * <li>{@link #collectResults()} Invokes the {@link ISessionResultCollector} to gather and provide
 * results.</li>
 * <li>{@link #passivate()} Cleans the session and removes all data from the latest execution. The
 * session is now ready to be reactivated by invoking {@link #activate(Object)} again.</li>
 * <li>{@link #destroy()} Destroys the session. Session can not be revived anymore.</li>
 * </ul>
 *
 * <p>
 * In order to facilitate compliance with the life cycle it is strongly recommended to use the
 * provided {@link SessionPool} in combination with {@link ExecutorService}.
 * <p>
 *
 * <pre>
 * {
 * 	SessionPool<String, DefaultSessionResult<String>> pool = new SessionPool<>(configuration);
 * 	Session<String, DefaultSessionResult<String>> session = pool.borrowObject("Input", new SessionVariables());
 * 	DefaultSessionResult<String> result = executorService.submit(input).get();
 * }
 * </pre>
 *
 * @param <I>
 *            The type of input to be analyzed.
 * @param <R>
 *            The expected result type.
 * @author Claudio Waldvogel, Alexander Wert
 * @see IDiagnosisEngine
 * @see ISessionResultCollector
 * @see SessionContext
 */
public final class Session<I, R> implements Callable<R> {

	/**
	 * Constant for empty session variables.
	 */
	public static final Map<String, Object> EMPTY_SESSION_VARIABLES = Collections.unmodifiableMap(new HashMap<String, Object>());

	/**
	 * The slf4j Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Session.class);

	/**
	 * The current state of this Session. A session can enter 6 states:
	 * <p>
	 * NEW, ACTIVATED, PROCESSED, PASSIVATED, DESTROYED, FAILURE
	 *
	 * @see State
	 */
	private State state = State.NEW;

	/**
	 * The {@link SessionContext} which is associated with this Session. It is created, executed,
	 * and destroyed in accordance with the session itself.
	 */
	private SessionContext<I> sessionContext;

	/**
	 * The {@link ISessionResultCollector} which produces the results of a session execution. The
	 * {@link ISessionResultCollector} is configurable from the {@link DiagnosisEngineConfiguration}
	 * .
	 */
	private ISessionResultCollector<I, R> resultCollector;

	// -------------------------------------------------------------
	// Methods: Construction
	// -------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param ruleDefinitions
	 *            Set of {@link RuleDefinition} instances.
	 * @param sessionResultCollector
	 *            The {@link ISessionResultCollector} for the results of the session.
	 */
	public Session(Set<RuleDefinition> ruleDefinitions, ISessionResultCollector<I, R> sessionResultCollector) {
		this(ruleDefinitions, sessionResultCollector, new DefaultRuleOutputStorage());
	}

	/**
	 * Constructor.
	 *
	 * @param ruleDefinitions
	 *            Set of {@link RuleDefinition} instances.
	 * @param sessionResultCollector
	 *            The {@link ISessionResultCollector} for the results of the session.
	 * @param storage
	 *            The storage for the rule outputs.
	 */
	public Session(Set<RuleDefinition> ruleDefinitions, ISessionResultCollector<I, R> sessionResultCollector, IRuleOutputStorage storage) {
		checkNotNull(ruleDefinitions);
		checkNotNull(storage);
		checkNotNull(sessionResultCollector);

		this.sessionContext = new SessionContext<>(ruleDefinitions, storage);
		this.resultCollector = sessionResultCollector;
	}

	/**
	 * Protected Constructor for testing purposes.
	 *
	 * @param sessionContext
	 *            The {@link SessionContext}.
	 * @param resultCollector
	 *            The {@link ISessionResultCollector}.
	 */
	protected Session(SessionContext<I> sessionContext, ISessionResultCollector<I, R> resultCollector) {
		this.sessionContext = sessionContext;
		this.resultCollector = resultCollector;
	}

	// -------------------------------------------------------------
	// Interface Implementation: Callable
	// -------------------------------------------------------------

	/**
	 * Executes the Session. Invocation is exclusively possible if {@link Session} is in ACTIVATED
	 * state, any other state forces a {@link SessionException}. Processing is enabled inserting a
	 * initial RuleOutput to the {@link IRuleOutputStorage} which will act as input to further
	 * rules. If processing completes without errors the {@link Session} enters PROCESSED state. In
	 * any case of error it enters FAILURE state.
	 *
	 * @throws Exception
	 *             If Session lifecycle is in an invalid state.
	 */
	@Override
	public R call() throws Exception {
		// Processes and collect results.
		// If a Session is used as Callable this call might horribly fail if sessions are not
		// retrieved from SessionPool and a sessions lifeCycle is neglected. But we have not chance
		// to activate a
		// session internally due to missing input information. So simply fail
		switch (state) {
		case ACTIVATED:
			sessionContext.getStorage().store(Rules.triggerRuleOutput(sessionContext.getInput()));
			doProcess();
			state = State.PROCESSED;
			break;
		default:
			throw new SessionException("Session can not enter process stated from: " + state + " state. Ensure that Session is in ACTIVATED state before processing.");
		}
		return resultCollector.collect(sessionContext);
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle -> reflects the life cycle of a
	// org.apache.commons.pool.impl.GenericObjectPool
	// -------------------------------------------------------------

	/**
	 * Tries to activate the Session for the given input object and SessionVariables. Activation
	 * means that state is changes to ACTIVATED and SessionContext is activated as well. Activation
	 * is only possible if the session is currently in NEW or PASSIVATED state, any other state
	 * forces a SessionException.
	 *
	 * @param input
	 *            The input to be processed.
	 * @param variables
	 *            The session variables to be used.
	 * @return The Session itself
	 * @throws SessionException
	 *             If Session lifecycle is in an invalid state.
	 */
	public Session<I, R> activate(I input, Map<String, ?> variables) throws SessionException {
		switch (state) {
		case NEW:
		case PASSIVATED:
			// All we need to do is to reactivate the SessionContext
			sessionContext.activate(input, variables);
			state = State.ACTIVATED;
			break;
		case DESTROYED:
			throw new SessionException("Session already destroyed.");
		case FAILURE:
		case ACTIVATED:
		case PROCESSED:
		default:
			throw new SessionException("Session can not enter ACTIVATED state from: " + state + " state. Ensure Session is in NEW or PASSIVATED state when activating.");
		}
		return this;
	}

	/**
	 * Cleans the {@link Session} by means of cleaning the {@link SessionContext} and removing all
	 * stale data. Valid transitions to PASSIVATED state are from PROCESSED and Failure.
	 *
	 * @return The Session itself/
	 */
	public Session<I, R> passivate() {
		if (!State.PROCESSED.equals(state)) {
			LOG.warn("Not processed Session gets passivated!");
		}
		// Passivate is always possible. Also it is important to passivate the Session in any case.
		// This ensures a reusable clean Session.
		sessionContext.passivate();
		state = State.PASSIVATED;
		return this;
	}

	/**
	 * Destroys this session. If the session was not yet passivated, it will be passivated in
	 * advance. While the session is destroyed, the ExecutorService is shutdown and the
	 * SessionContext is destroyed. After the session is destroyed it is unusable!
	 *
	 */
	public void destroy() {
		switch (state) {
		case PROCESSED:
			// We can destroy the session but it was not yet passivated. To stay in sync with the
			// state lifeCycle we passivate first
			passivate();
			break;
		case DESTROYED:
			LOG.warn("Failed destroying session. Session has already been destroyed!");
			return;
		case NEW:
		case ACTIVATED:
			LOG.warn("Session is destroy before it was processed.");
			break;
		case FAILURE:
		case PASSIVATED:
		default:
			break;
		}
		state = State.DESTROYED;
		sessionContext = null; // NOPMD
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	/**
	 * Internal processing routine to execute all rules. This methods blocks as long as further
	 * rules can be executed. If this method returns it is assured that all possible rules are
	 * executed and all possible results are available in the IRuleOutputStorage.
	 *
	 * @throws SessionException
	 *             If processing fails.
	 *
	 */
	private void doProcess() throws SessionException {
		// identify initial set of rules that can be executed
		Collection<RuleDefinition> nextRules = findNextRules(sessionContext.getStorage().getAvailableTagTypes(), sessionContext.getRuleSet());

		// execute rules as long as there are any in the pipe
		while (!nextRules.isEmpty()) {
			boolean anyRuleExecuted = false;
			// execute all rules in the pipe
			for (RuleDefinition ruleDef : nextRules) {
				try {
					// Collect all available inputs for the selected rule
					Collection<RuleInput> inputs = collectInputs(ruleDef, sessionContext.getStorage());
					// Filter out all inputs that have already been processed by the selected rule
					// in the past
					inputs = filterProcessedInputs(sessionContext.getExecutions(), ruleDef, inputs);
					if (CollectionUtils.isNotEmpty(inputs)) {
						// Execute selected rule for each input and collect corresponding rule
						// outputs
						Collection<RuleOutput> outputs = ruleDef.execute(inputs, Session.this.sessionContext.getSessionVariables());
						// store results
						sessionContext.getStorage().store(outputs);
						anyRuleExecuted = true;
						// track execution for subsequent checks
						for (RuleInput ruleInput : inputs) {
							sessionContext.addExecution(ruleDef, ruleInput);
						}
					}
				} catch (DiagnosisEngineException ex) {
					failure(ex);
				}
			}
			// Continue looping only if new rule executions are available.
			// Identify next rules that have been activated by the results of the previously
			// executed set of rules.
			if (anyRuleExecuted) {
				nextRules = findNextRules(sessionContext.getStorage().getAvailableTagTypes(), sessionContext.getRuleSet());
			} else {
				break;
			}
		}
	}

	/**
	 * Marks the session as failed and passivates it.
	 *
	 * @param cause
	 *            The root cause of failure.
	 * @throws SessionException
	 *             If diagnosis fails with errors.
	 */
	private void failure(DiagnosisEngineException cause) throws SessionException {
		// enter failure state
		state = State.FAILURE;
		// ensure that Session gets passivated to enable reuse
		passivate();
		// Propagate the cause of failure
		throw new SessionException("Diagnosis Session failed with error(s)", cause);
	}

	/**
	 * Filters the {@link RuleInput}s that have already been processed for the given
	 * {@link RuleDefinition} before.
	 *
	 * @param executions
	 *            Map of rule definitions and previously executed rule inputs.
	 * @param ruleDef
	 *            {@link RuleDefinition} to check against.
	 * @param inputs
	 *            a collection of {@link RuleInput}s to be filtered. {@link RuleInput}s that have
	 *            already been processed for the given {@link RuleDefinition} before are removed
	 *            from this collection.
	 * @return a filtered collection of rule inputs.
	 */
	Collection<RuleInput> filterProcessedInputs(Multimap<RuleDefinition, RuleInput> executions, RuleDefinition ruleDef, Collection<RuleInput> inputs) {
		if (CollectionUtils.isEmpty(inputs)) {
			return Collections.emptyList();
		}

		ArrayList<RuleInput> filteredList = new ArrayList<>();
		for (RuleInput input : inputs) {
			if (!executions.containsEntry(ruleDef, input)) {
				filteredList.add(input);
			}
		}
		return filteredList;
	}

	/**
	 * Utility method to determine the next executable rules. The next rules are determined by
	 * comparing all, so far collected, types of tags in {@link IRuleOutputStorage} and the
	 * {@link FireCondition} of each {@link RuleDefinition}.
	 *
	 * @param availableTagTypes
	 *            Set of strings determining the available tag types for input of potential next
	 *            rules.
	 * @param ruleDefinitions
	 *            Set of available rule definitions.
	 *
	 * @return Collection of {@link RuleDefinition}s.
	 * @see rocks.inspectit.server.diagnosis.engine.rule.FireCondition
	 * @see RuleDefinition
	 * @see IRuleOutputStorage
	 */
	Collection<RuleDefinition> findNextRules(Set<String> availableTagTypes, Set<RuleDefinition> ruleDefinitions) {
		Set<RuleDefinition> nextRules = new HashSet<>();
		Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
		while (iterator.hasNext()) {
			RuleDefinition rule = iterator.next();
			if (rule.getFireCondition().canFire(availableTagTypes)) {
				nextRules.add(rule);
			}
		}
		return nextRules;
	}

	/**
	 * Collects all available inputs for a single {@link RuleDefinition} from the passed storage.
	 * Each RuleInput is equivalent to an execution of the RuleInput.
	 *
	 * @param definition
	 *            The {@link RuleDefinition} to be executed.
	 * @param storage
	 *            The {@link IRuleOutputStorage} to derive the {@link RuleInput} instances from.
	 * @return A Collection of RuleInputs
	 * @see RuleInput
	 * @see RuleDefinition
	 */
	Collection<RuleInput> collectInputs(RuleDefinition definition, IRuleOutputStorage storage) {
		// retrieve all tag types that are required to execute the passed rule definition
		Set<String> requiredInputTags = definition.getFireCondition().getTagTypes();
		// Find all outputs that match the required tag types
		Collection<RuleOutput> leafOutputs = storage.findLatestResultsByTagType(requiredInputTags);
		Set<RuleInput> inputs = Sets.newHashSet();
		// A single rule can produce n inputs (of the same tag type) for the next rule. Each
		// embedded tag in ruleOutput.getTags() will be
		// reflected in a new RuleInput
		// Although this is an O(n²) loop the iterated lists are expected to be rather short.
		// Also the nested while loop is expected to be very short.
		ruleOutputLoop: for (RuleOutput output : leafOutputs) {
			for (Tag leafTag : output.getTags()) {
				Collection<Tag> tags = Tags.unwrap(leafTag, requiredInputTags);
				if (tags.size() == requiredInputTags.size()) {
					// Create and store a new RuleInput
					inputs.add(new RuleInput(leafTag, tags));
				} else {
					// If any of the leaf tags of an ruleOutput does not result in a valid unwrapped
					// tag set, then none of the leaf tags of THIS ruleOutput can produce a valid
					// unwrapped tag set. So we can continue with the next ruleOutput.
					continue ruleOutputLoop;
				}
			}
		}
		return inputs;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #state}.
	 *
	 * @return {@link #state}
	 */
	public State getState() {
		return state;
	}

	/**
	 * Gets {@link #sessionContext}.
	 *
	 * @return {@link #sessionContext}
	 */
	public SessionContext<I> getSessionContext() {
		return sessionContext;
	}

	// -------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------

	/**
	 * Internal enum representing the current state of this session.
	 */
	enum State {
		/**
		 * The initial State of each Session.
		 */
		NEW,

		/**
		 * The state as soon as an {@link Session} gets activated. This state can be entered from
		 * <code>NEW</code> and <code>PASSIVATED</code> states.
		 */
		ACTIVATED,

		/**
		 * A {@link Session} enters the <code>PROCESSED</code> state after all applicable rules were
		 * executed.
		 */
		PROCESSED,

		/**
		 * An {@link Session} can enter the <code>PASSIVATED</code> state only from
		 * <code>PROCESSED</code> state. <code>PASSIVATED</code> is the only state which enables a
		 * transition back to <code>ACTIVATED</code>.
		 */
		PASSIVATED,

		/**
		 * {@link Session} is destroyed and not longer usable.
		 */
		DESTROYED,

		/**
		 * {@link Session} encountered an error and is in a failure state.
		 */
		FAILURE
	}
}
