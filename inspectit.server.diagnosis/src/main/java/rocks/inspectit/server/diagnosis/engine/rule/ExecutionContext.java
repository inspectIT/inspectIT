package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import rocks.inspectit.server.diagnosis.engine.session.Session;

/**
 * Value object providing information to execute a rule implementation. A instance of
 * <code>ExecutionContext</code> is valid for exactly one execution of a rule implementation.
 *
 * <pre>
 *     This encloses:
 * <ul>
 *     <li>All {@link TagInjection}</li>
 *     <li>All session variables</li>
 *     <li>All {@link ConditionMethod}</li>
 *     <li>The {@link ActionMethod}</li>
 * </ul>
 * </pre>
 *
 * After the rule is executed the ExecutionContext is invalid and destroyed.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class ExecutionContext {

	/**
	 * The backing rule implementation.
	 */
	private final Object instance;

	/**
	 * The {@link RuleDefinition} to be executed. The {@link RuleDefinition} is a abstracted and
	 * generalized of a rule implementation {@link #instance)}.
	 *
	 * @see RuleDefinition
	 */
	private final RuleDefinition definition;

	/**
	 * The input to be processed by the rule.
	 *
	 * @see RuleInput
	 */
	private final RuleInput input;

	/**
	 * Container providing all the session variables.
	 */
	private final Map<String, Object> sessionParameters;

	/**
	 * Default Constructor.
	 *
	 * @param definition
	 *            The {@link RuleDefinition}
	 * @param instance
	 *            The <code>actual implementation</code>
	 * @param input
	 *            The {@link RuleInput} to be processed
	 */
	public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input) {
		this(definition, instance, input, Session.EMPTY_SESSION_VARIABLES);
	}

	/**
	 * Constructor with session variables.
	 *
	 * @param definition
	 *            The {@link RuleDefinition}
	 * @param instance
	 *            The <code>actual implementation</code>
	 * @param input
	 *            The {@link RuleInput} to be processed
	 * @param sessionParameters
	 *            The session variables.
	 */
	public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input, Map<String, Object> sessionParameters) {
		this.definition = checkNotNull(definition, "Rule definition must not be null!");
		this.instance = checkNotNull(instance, "Rule implementation instance must not be null!");
		this.input = checkNotNull(input, "Rule input must not be null!");
		this.sessionParameters = checkNotNull(sessionParameters, "Session parameters must not be null!");
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #instance)}.
	 *
	 * @return {@link #instance}
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * Gets {@link #definition}.
	 *
	 * @return {@link #definition}
	 */
	public RuleDefinition getDefinition() {
		return definition;
	}

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public RuleInput getRuleInput() {
		return input;
	}

	/**
	 * Gets {@link #sessionParameters}.
	 *
	 * @return {@link #sessionParameters}
	 */
	public Map<String, Object> getSessionParameters() {
		return sessionParameters;
	}
}
