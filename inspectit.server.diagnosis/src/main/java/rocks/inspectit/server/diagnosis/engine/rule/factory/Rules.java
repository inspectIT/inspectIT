package rocks.inspectit.server.diagnosis.engine.rule.factory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.ConditionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.FireCondition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition.RuleDefinitionBuilder;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.SessionVariableInjection;
import rocks.inspectit.server.diagnosis.engine.rule.TagInjection;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.Visitor;

/**
 * Utility method to work with rule classes. This class serves as static factory to create
 * {@link RuleDefinition}s. So far it is not yet clear if this will stay, or a factory interface
 * will be provided to enable to possibility of replaceable {@link RuleDefinition} implementations.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public final class Rules {

	/**
	 * The name of the internal initial rule to kick of processing.
	 */
	private static final String TRIGGER_RULE = "TRIGGER_RULE";

	/**
	 * Private constructor.
	 */
	private Rules() {
	}

	/**
	 * Creates the faked output of an internal rule to start process of other rules. If we wrap the
	 * input to be analyzed into a RuleOutput the entire rule processing works exactly the same way
	 * and and the input can be access of type {@link Tags#ROOT_TAG}.
	 *
	 * @param input
	 *            The input to be wrapped in output.
	 * @return RuleOutput providing the input as value of {@link Tags#ROOT_TAG}.
	 */
	public static RuleOutput triggerRuleOutput(Object input) {
		return new RuleOutput(TRIGGER_RULE, Tags.ROOT_TAG, Collections.<ConditionFailure> emptyList(), Collections.singleton(Tags.rootTag(input)));
	}

	/**
	 * Methods transforms all given classes to {@link RuleDefinition}s.
	 * <p>
	 * Throws: RuleDefinitionException if a class transformation fails.
	 *
	 * @param classes
	 *            The classes to be transformed.
	 * @return Set of {@link RuleDefinition}s.
	 * @throws RuleDefinitionException
	 *             If any {@link RuleDefinition} is invalid.
	 */
	public static Set<RuleDefinition> define(Class<?>... classes) throws RuleDefinitionException {
		return define(Arrays.asList(checkNotNull(classes, "Rule classes array must not be null!")));
	}

	/**
	 * Methods transforms all given classes to {@link RuleDefinition}s.
	 *
	 * Throws: RuleDefinitionException if a class transformation fails.
	 *
	 * @param classes
	 *            The classes to be transformed.
	 * @return Set of {@link RuleDefinition}s.
	 * @throws RuleDefinitionException
	 *             If any {@link RuleDefinition} is invalid.
	 *
	 */
	public static Set<RuleDefinition> define(Collection<Class<?>> classes) throws RuleDefinitionException {
		checkArgument(CollectionUtils.isNotEmpty(classes), "Rule classes must not be null or empty!");
		Set<RuleDefinition> ruleSet = Sets.newHashSet();
		for (Class<?> clazz : classes) {
			ruleSet.add(define(clazz));
		}
		return ruleSet;
	}

	/**
	 * Methods transforms a class to a {@link RuleDefinition}.
	 * <p>
	 * Throws: RuleDefinitionException if a class transformation fails.
	 *
	 * @param clazz
	 *            The class to be transformed.
	 * @return The corresponding {@link RuleDefinition}.
	 * @throws RuleDefinitionException
	 *             If {@link RuleDefinition} is invalid.
	 */
	public static RuleDefinition define(final Class<?> clazz) throws RuleDefinitionException {
		checkNotNull(clazz, "Rule class must not be null!");
		Rule annotation = ReflectionUtils.findAnnotation(clazz, Rule.class);
		if (annotation == null) {
			throw new RuleDefinitionException(clazz.getName() + " must be annotated with @Rule annotation.");
		}
		if (!ReflectionUtils.hasNoArgsConstructor(clazz)) {
			throw new RuleDefinitionException(clazz.getName() + " must define an empty default constructor.");
		}

		ActionMethod actionMethod = describeActionMethod(clazz);
		List<ConditionMethod> conditionMethods = describeConditionMethods(clazz);
		List<TagInjection> tagInjections = describeTagInjection(clazz);
		List<SessionVariableInjection> variableInjections = describeSessionParameterInjections(clazz);
		FireCondition fireCondition = describeFireCondition(annotation, tagInjections);

		RuleDefinitionBuilder builder = new RuleDefinitionBuilder();
		builder.setName(annotation.name());
		builder.setDescription(annotation.description());
		builder.setImplementation(clazz);
		builder.setFireCondition(fireCondition);
		builder.setConditionMethods(conditionMethods);
		builder.setActionMethod(actionMethod);
		builder.setTagInjections(tagInjections);
		builder.setSessionVariableInjections(variableInjections);
		return builder.build();
	}

	// -------------------------------------------------------------
	// Methods: Descriptions
	// -------------------------------------------------------------

	/**
	 * Utility method to create a {@link FireCondition} either from a {@link Rule} annotation or
	 * from {@link TagInjection}s. The values defined in the annotation overrules the
	 * {@link TagInjection}s.
	 *
	 * @param rule
	 *            The {@link Rule} annotation.
	 * @param tagInjections
	 *            The list of {@link TagInjection}s to extract a {@link FireCondition}.
	 * @return A new {@link FireCondition}
	 */
	public static FireCondition describeFireCondition(Rule rule, List<TagInjection> tagInjections) {
		if ((rule != null) && ArrayUtils.isNotEmpty(rule.fireCondition())) {
			return new FireCondition(Sets.newHashSet(Arrays.asList(rule.fireCondition())));
		} else {
			Set<String> requiredTypes = new HashSet<>();
			for (TagInjection injection : tagInjections) {
				requiredTypes.add(injection.getType());
			}
			return new FireCondition(requiredTypes);
		}
	}

	/**
	 * Extracts the {@link SessionVariableInjection}s from the given class by processing the
	 * {@link SessionVariable} annotations.
	 *
	 * @param clazz
	 *            The class to be analyzed.
	 * @return List of SessionVariableInjections
	 * @throws RuleDefinitionException
	 *             If {@link SessionVariableInjection} annotations are invalid.
	 */
	public static List<SessionVariableInjection> describeSessionParameterInjections(Class<?> clazz) throws RuleDefinitionException {
		return ReflectionUtils.visitFieldsAnnotatedWith(SessionVariable.class, clazz, new Visitor<SessionVariable, Field, SessionVariableInjection>() {
			@Override
			public SessionVariableInjection visit(SessionVariable annotation, Field field) {
				return new SessionVariableInjection(annotation.name(), annotation.optional(), field);
			}
		});
	}

	/**
	 * Extracts the {@link TagInjection}s from the given class by processing the
	 * {@link TagInjection} annotations.
	 *
	 * @param clazz
	 *            The class to be analyzed.
	 * @return List of TagInjection
	 * @throws RuleDefinitionException
	 *             If {@link TagInjection} annotations are invalid. Contract is that a class must
	 *             defined at least one field annotated with {@link TagValue}. Otherwise the rule
	 *             could never fire, because no input could be determined.
	 */
	public static List<TagInjection> describeTagInjection(Class<?> clazz) throws RuleDefinitionException {
		List<TagInjection> tagInjections = ReflectionUtils.visitFieldsAnnotatedWith(TagValue.class, clazz, new Visitor<TagValue, Field, TagInjection>() {
			@Override
			public TagInjection visit(TagValue annotation, Field field) {
				return new TagInjection(annotation.type(), field, annotation.injectionStrategy());
			}
		});

		if (CollectionUtils.isEmpty(tagInjections)) {
			throw new RuleDefinitionException(clazz.getName() + " must annotate at least one field with @Value. Otherwise the " + "rule will never fire and is useless.");
		} else {
			return tagInjections;
		}
	}

	/**
	 * Extracts the {@link Action} from the given class by processing the {@link Action} annotation.
	 *
	 * @param clazz
	 *            The class to be analyzed.
	 * @return List of ActionMethod
	 * @throws RuleDefinitionException
	 *             If {@link Action} annotation is invalid.
	 */
	public static ActionMethod describeActionMethod(Class<?> clazz) throws RuleDefinitionException {
		List<ActionMethod> actionMethods = ReflectionUtils.visitMethodsAnnotatedWith(Action.class, clazz, new Visitor<Action, Method, ActionMethod>() {
			@Override
			public ActionMethod visit(Action annotation, Method method) throws RuleDefinitionException {
				return new ActionMethod(method, annotation.resultTag(), annotation.resultQuantity());
			}
		});
		if ((null == actionMethods) || (actionMethods.size() != 1)) {
			throw new RuleDefinitionException("A rule must define exactly one method annotated with @Action. Otherwise the rule could never be exectued.");
		} else {
			return actionMethods.get(0);
		}
	}

	/**
	 * Extracts the {@link ConditionMethod}s from the given class by processing the
	 * {@link Condition} annotation.
	 *
	 * @param clazz
	 *            The class to be analyzed.
	 * @return List of ConditionMethods
	 * @throws RuleDefinitionException
	 *             If {@link ConditionMethod} annotations are invalid.
	 */
	public static List<ConditionMethod> describeConditionMethods(Class<?> clazz) throws RuleDefinitionException {
		List<ConditionMethod> conditions = ReflectionUtils.visitMethodsAnnotatedWith(Condition.class, clazz, new Visitor<Condition, Method, ConditionMethod>() {
			@Override
			public ConditionMethod visit(Condition annotation, Method method) throws RuleDefinitionException {
				return new ConditionMethod(annotation.name(), annotation.hint(), method);
			}
		});
		return conditions;
	}
}
