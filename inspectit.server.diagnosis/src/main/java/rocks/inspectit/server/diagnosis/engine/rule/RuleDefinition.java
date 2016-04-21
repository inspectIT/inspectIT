package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.tryInstantiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * A {@link RuleDefinition} is an abstracted and generalized view of a rule implementation. Each
 * rule implementation which is passed to the
 * {@link rocks.inspectit.server.diagnosis.engine.DiagnosisEngine} is converted in a
 * <code>RuleDefinition</code>.
 * <p>
 * <p>
 *
 * <pre>
 *  A <code>RuleDefinition</code> summarizes
 *  <ul>
 *      <li>The name of a rule</li>
 *      <li>The description of a rule</li>
 *      <li>The implementing class of a rule</li>
 *      <li>The {@link FireCondition} of a rule</li>
 *      <li>The {@link TagInjection}s of a rule</li>
 *      <li>The {@link SessionVariableInjection}s of a rule</li>
 *      <li>The {@link ConditionMethod}s of a rule</li>
 *  </ul>
 * </pre>
 *
 * @author Claudio Waldvogel
 * @see FireCondition
 * @see TagInjection
 * @see SessionVariables
 * @see ConditionMethod
 */
public class RuleDefinition {

	/**
	 * The default description of a rule.
	 */
	private static final String EMPTY_DESCRIPTION = "EMPTY";

	/**
	 * The name of this rule.
	 */
	private String name;

	/**
	 * The description of this rule.
	 */
	private String description;

	/**
	 * The backing implementation class of this rule.
	 */
	private Class<?> implementation;

	/**
	 * The {@link FireCondition} of this rule.
	 */
	private FireCondition fireCondition;

	/**
	 * The required {@link TagInjection}s of this rule.
	 *
	 * @see TagInjection
	 */
	private List<TagInjection> tagInjections;

	/**
	 * The required {@link SessionVariableInjection}s of this rule.
	 *
	 * @see SessionVariableInjection
	 */
	private List<SessionVariableInjection> variableInjections;

	/**
	 * The {@link ConditionMethod}s of this rule.
	 *
	 * @see ConditionMethod
	 */
	private List<ConditionMethod> conditionMethods;

	/**
	 * The {@link ActionMethod}s of this rule.
	 *
	 * @see ActionMethod
	 */
	private ActionMethod actionMethod;

	/**
	 * Private constructor.
	 */
	protected RuleDefinition() {

	}

	// -------------------------------------------------------------
	// Methods: RuleExecution
	// -------------------------------------------------------------

	/**
	 * Executes this {@link RuleDefinition} in 6 steps.
	 * <p/>
	 * <p>
	 *
	 * <pre>
	 * 1. The raw class which implements this <code>RuleDefinition</code> is instantiated and wrapped in a new {@link ExecutionContext}.
	 * 2. All {@link TagInjection}s are executed.
	 * 3. All {@link SessionVariableInjection}s are executed
	 * 4. All {@link ConditionMethod}s are executed.
	 * 5. If all {@link ConditionMethod}s succeed, the {@link ActionMethod} is executed.
	 * 6. A new {@link RuleOutput} is created and returned
	 * </pre>
	 *
	 * @param input
	 *            The {@link RuleInput} to be processed. Must not null.
	 * @param variables
	 *            The {@link SessionVariables}. Must not null.
	 * @return A new {@link RuleOutput}
	 * @throws RuntimeException
	 * @see ExecutionContext
	 * @see RuleInput
	 * @see RuleOutput
	 * @see SessionVariables
	 */
	public RuleOutput execute(RuleInput input, SessionVariables variables) {
		checkNotNull(input, "The RuleInput must not be null!");
		checkNotNull(variables, "The SessionVariables must not be null!");

		// Create a new ExecutionContext for this run
		ExecutionContext ctx = new ExecutionContext(this, tryInstantiate(getImplementation()), input, variables);

		// Inject tags
		for (TagInjection injection : getTagInjections()) {
			injection.execute(ctx);
		}

		// Inject session variables
		for (SessionVariableInjection injection : getSessionVariableInjections()) {
			injection.execute(ctx);
		}

		// Check conditions
		Collection<ConditionFailure> conditionFailures = new ArrayList<>();
		for (ConditionMethod conditionMethod : getConditionMethods()) {
			ConditionFailure failure = conditionMethod.execute(ctx);
			if (failure != null) {
				conditionFailures.add(failure);
			}
		}

		// If no condition failed, execute the actual action
		Collection<Tag> tags = Lists.newArrayList();
		if (conditionFailures.isEmpty()) {
			Collection<Tag> results = getActionMethod().execute(ctx);
			if (null != results) {
				tags = results;
			}
		}

		// Deliver result
		return new RuleOutput(getName(), getActionMethod().getResultTag(), conditionFailures, tags);
	}

	/**
	 * Convenience method to execute this <code>RuleDefinition</code> for several {@link RuleInput}
	 * s. The amount of {@link RuleInput}s equals the amount of executions of this
	 * <code>RuleDefinition</code>. Each {@link RuleInput} concludes in a invocation of
	 * {@link #execute(RuleInput, SessionVariables)}.
	 *
	 * @param inputs
	 *            A collection of {@link RuleInput} to be processed.
	 * @param variables
	 *            The {@link SessionVariables}
	 * @return A collection of {@link RuleOutput}s.
	 * @see RuleInput
	 * @see RuleOutput
	 * @see SessionVariables
	 */
	public Collection<RuleOutput> execute(Collection<RuleInput> inputs, SessionVariables variables) {
		checkNotNull(inputs, "The RuleInputs must not be null!");

		Iterator<RuleInput> iterator = inputs.iterator();
		Set<RuleOutput> outputs = Sets.newHashSet();

		while (iterator.hasNext()) {
			outputs.add(execute(iterator.next(), variables));
		}
		return outputs;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets {@link #description}.
	 *
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets {@link #implementation}.
	 *
	 * @return {@link #implementation}
	 */
	public Class<?> getImplementation() {
		return implementation;
	}

	/**
	 * Gets {@link #fireCondition}.
	 *
	 * @return {@link #fireCondition}
	 */
	public FireCondition getFireCondition() {
		return fireCondition;
	}

	/**
	 * Gets {@link #tagInjections}.
	 *
	 * @return {@link #tagInjections}
	 */
	public List<TagInjection> getTagInjections() {
		return tagInjections;
	}

	/**
	 * Gets {@link #variableInjections}.
	 *
	 * @return {@link #variableInjections}
	 */
	public List<SessionVariableInjection> getSessionVariableInjections() {
		return variableInjections;
	}

	/**
	 * Gets {@link #actionMethod}.
	 *
	 * @return {@link #actionMethod}
	 */
	public ActionMethod getActionMethod() {
		return actionMethod;
	}

	/**
	 * Gets {@link #conditionMethods}.
	 *
	 * @return {@link #conditionMethods}
	 */
	public List<ConditionMethod> getConditionMethods() {
		return conditionMethods;
	}

	/**
	 * Sets {@link #variableInjections}.
	 *
	 * @param variableInjections
	 *            New value for {@link #variableInjections}
	 */
	protected void setSessionVariableInjections(List<SessionVariableInjection> variableInjections) {
		this.variableInjections = variableInjections;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets {@link #description}.
	 *
	 * @param description
	 *            New value for {@link #description}
	 */
	protected void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets {@link #implementation}.
	 *
	 * @param implementation
	 *            New value for {@link #implementation}
	 */
	protected void setImplementation(Class<?> implementation) {
		this.implementation = implementation;
	}

	/**
	 * Sets {@link #fireCondition}.
	 *
	 * @param fireCondition
	 *            New value for {@link #fireCondition}
	 */
	protected void setFireCondition(FireCondition fireCondition) {
		this.fireCondition = fireCondition;
	}

	/**
	 * Sets {@link #tagInjections}.
	 *
	 * @param tagInjections
	 *            New value for {@link #tagInjections}
	 */
	protected void setTagInjections(List<TagInjection> tagInjections) {
		this.tagInjections = tagInjections;
	}

	/**
	 * Sets {@link #conditionMethods}.
	 *
	 * @param conditionMethods
	 *            New value for {@link #conditionMethods}
	 */
	protected void setConditionMethods(List<ConditionMethod> conditionMethods) {
		this.conditionMethods = conditionMethods;
	}

	/**
	 * Sets {@link #actionMethod}.
	 *
	 * @param actionMethod
	 *            New value for {@link #actionMethod}
	 */
	protected void setActionMethod(ActionMethod actionMethod) {
		this.actionMethod = actionMethod;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "RuleDefinition{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", implementation=" + implementation + ", fireCondition=" + fireCondition + ", injectionPoints="
				+ tagInjections + ", actionMethod=" + actionMethod + ", conditionMethods=" + conditionMethods + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RuleDefinition that = (RuleDefinition) o;

		return new EqualsBuilder().append(getName(), that.getName()).append(getDescription(), that.getDescription()).append(getImplementation(), that.getImplementation())
				.append(getFireCondition(), that.getFireCondition()).append(getTagInjections(), that.getTagInjections()).append(variableInjections, that.variableInjections)
				.append(getConditionMethods(), that.getConditionMethods()).append(getActionMethod(), that.getActionMethod()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getName()).append(getDescription()).append(getImplementation()).append(getFireCondition()).append(getTagInjections()).append(variableInjections)
				.append(getConditionMethods()).append(getActionMethod()).toHashCode();
	}

	/**
	 * Builder for {@link RuleDefinition} instances to ensure immutability of the
	 * {@link RuleDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class RuleDefinitionBuilder {

		/**
		 * {@link RuleDefinition} instance under construction.
		 */
		private final RuleDefinition objectUnderConstruction;

		/**
		 * Constructor.
		 */
		public RuleDefinitionBuilder() {
			objectUnderConstruction = new RuleDefinition();
		}

		/**
		 * Sets {@link RuleDefinition#variableInjections}.
		 *
		 * @param variableInjections
		 *            New value for {@link RuleDefinition#variableInjections}
		 */
		public void setSessionVariableInjections(List<SessionVariableInjection> variableInjections) {
			objectUnderConstruction.setSessionVariableInjections(variableInjections);
		}

		/**
		 * Sets {@link RuleDefinition#name}.
		 *
		 * @param name
		 *            New value for {@link RuleDefinition#name}
		 */
		public void setName(String name) {
			objectUnderConstruction.setName(name);
		}

		/**
		 * Sets {@link RuleDefinition#description}.
		 *
		 * @param description
		 *            New value for {@link RuleDefinition#description}
		 */
		public void setDescription(String description) {
			objectUnderConstruction.setDescription(description);
		}

		/**
		 * Sets {@link RuleDefinition#implementation}.
		 *
		 * @param implementation
		 *            New value for {@link RuleDefinition#implementation}
		 */
		public void setImplementation(Class<?> implementation) {
			objectUnderConstruction.setImplementation(implementation);
		}

		/**
		 * Sets {@link RuleDefinition#fireCondition}.
		 *
		 * @param fireCondition
		 *            New value for {@link RuleDefinition#fireCondition}
		 */
		public void setFireCondition(FireCondition fireCondition) {
			objectUnderConstruction.setFireCondition(fireCondition);
		}

		/**
		 * Sets {@link RuleDefinition#tagInjections}.
		 *
		 * @param tagInjections
		 *            New value for {@link RuleDefinition#tagInjections}
		 */
		public void setTagInjections(List<TagInjection> tagInjections) {
			objectUnderConstruction.setTagInjections(tagInjections);
		}

		/**
		 * Sets {@link RuleDefinition#conditionMethods}.
		 *
		 * @param conditionMethods
		 *            New value for {@link RuleDefinition#conditionMethods}
		 */
		public void setConditionMethods(List<ConditionMethod> conditionMethods) {
			objectUnderConstruction.setConditionMethods(conditionMethods);
		}

		/**
		 * Sets {@link RuleDefinition#actionMethod}.
		 *
		 * @param actionMethod
		 *            New value for {@link RuleDefinition#actionMethod}
		 */
		public void setActionMethod(ActionMethod actionMethod) {
			objectUnderConstruction.setActionMethod(actionMethod);
		}

		/**
		 * Creates a {@link RuleDefinition} instance according to the set attributes. Before calling
		 * this method, the following properties must be set:
		 * <ul>
		 * <li>{@link RuleDefinitionBuilder#setSessionVariableInjections(List)}</li>
		 * <li>{@link RuleDefinitionBuilder#setImplementation}</li>
		 * <li>{@link RuleDefinitionBuilder#setFireCondition}</li>
		 * <li>{@link RuleDefinitionBuilder#setTagInjections}</li>
		 * <li>{@link RuleDefinitionBuilder#setConditionMethods}</li>
		 * <li>{@link RuleDefinitionBuilder#setActionMethod}</li>
		 * </ul>
		 *
		 * @return Returns a {@link RuleDefinition} instance.
		 */
		public RuleDefinition build() {
			if (null == objectUnderConstruction.getSessionVariableInjections()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Session Variable Injections!");
			}
			if (null == objectUnderConstruction.getImplementation()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Rule Implementation!");
			}
			if (null == objectUnderConstruction.getFireCondition()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Fire Condition!");
			}
			if (null == objectUnderConstruction.getTagInjections()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Tag Injections!");
			}
			if (null == objectUnderConstruction.getConditionMethods()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Condition Methods!");
			}
			if (null == objectUnderConstruction.getActionMethod()) {
				throw new IllegalStateException("Cannot create a RuleDefinition. Missing Action Method!");
			}

			objectUnderConstruction.setName(StringUtils.defaultIfEmpty(objectUnderConstruction.getName(), objectUnderConstruction.getImplementation().getName()));
			objectUnderConstruction.setDescription(StringUtils.defaultIfEmpty(objectUnderConstruction.getDescription(), EMPTY_DESCRIPTION));

			return objectUnderConstruction;
		}
	}
}
