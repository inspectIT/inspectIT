package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;

import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * Represents the action method of a rule. An <code>ActionMethod</code> reflects the {@link Action}
 * annotation.
 *
 * @author Claudio Waldvogel, Alexander Wert
 * @see Action
 */
public class ActionMethod {

	/**
	 * The {@link Method} to be invoked.
	 */
	private final Method method;

	/**
	 * The tag type this action produces.
	 *
	 * @see Action#resultTag()
	 */
	private final String resultTag;

	/**
	 * The output Quantity.
	 *
	 * @see Action#resultQuantity()
	 */
	private final Action.Quantity resultQuantity;

	/**
	 * Default Constructor.
	 *
	 * @param method
	 *            The method to be invoked
	 * @param resultTag
	 *            The type of tags this action produces
	 * @param resultQuantity
	 *            The result Quantity
	 * @throws RuleDefinitionException
	 *             If the {@link ActionMethod} is invalid. The action method must be public, with
	 *             zero arguments and void return type.
	 */
	public ActionMethod(Method method, String resultTag, Action.Quantity resultQuantity) throws RuleDefinitionException {
		this.method = checkNotNull(method, "The method must not be null.");
		this.resultTag = checkNotNull(resultTag, "The result tag must not be null.");
		this.resultQuantity = checkNotNull(resultQuantity, "The output quantity must not be null.");
		validate();
	}

	// -------------------------------------------------------------
	// Methods: RuleExecution
	// -------------------------------------------------------------

	/**
	 * Executes the action. Throws: RuleExecutionException in case of any error
	 *
	 * @param context
	 *            The current executing {@link ExecutionContext}
	 * @return A collection of {@link Tags}s
	 * @throws RuleExecutionException
	 *             If rule execution fails with an exception.
	 * @see ExecutionContext
	 * @see Tag
	 */
	public Collection<Tag> execute(ExecutionContext context) throws RuleExecutionException {
		try {
			Object result = ReflectionUtils.invokeMethod(getMethod(), context.getInstance());
			return transform(result, context);
		} catch (Exception e) {
			throw new RuleExecutionException("Failed to invoke action method (" + getMethod().getName() + ")", context, e);
		}
	}

	/**
	 * Validates the properties of the {@link ActionMethod}.
	 *
	 * @throws RuleDefinitionException
	 *             If the {@link ActionMethod} is invalid. The action method must be public, with
	 *             zero arguments and void return type.
	 */
	private void validate() throws RuleDefinitionException {
		boolean valid = Modifier.isPublic(method.getModifiers());
		Class<?> returnType = method.getReturnType();
		valid = valid && !returnType.equals(Void.class);
		valid = valid && (method.getParameterTypes().length == 0);
		if (!valid) {
			String msg = method.getDeclaringClass().getName() + " defines an invalid action method with name: " + method.getName();
			msg += "\nValid action methods are public with a non void return type and zero arguments (e.g. public" + " String action())";
			throw new RuleDefinitionException(msg);
		}
		// ensure proper return type in case of MULTIPLE outputQuantity
		if (Action.Quantity.MULTIPLE.equals(resultQuantity)) {
			if (!returnType.isArray() && !Iterable.class.isAssignableFrom(returnType)) {
				throw new RuleDefinitionException(method.getDeclaringClass().getName() + "defines an MULTIPLE outputQuantity, but return type is neither Array nor Collection.");
			}
		}
	}

	/**
	 * Transforms the result of a rule to a collection of {@link Tag}s. How the result is
	 * transformed is controlled by the #resultQuantity property.
	 *
	 * @param result
	 *            The result to be transformed
	 * @param context
	 *            The {@link ExecutionContext} enclosing this execution
	 * @return A collection of {@link Tag}s
	 * @throws RuleExecutionException
	 *             If rule return type does not match.
	 * @see Tag
	 * @see ExecutionContext
	 */
	private Collection<Tag> transform(Object result, ExecutionContext context) throws RuleExecutionException {
		if (result == null) {
			return Collections.emptyList();
		} else {
			Collection<Tag> transformed = Lists.newArrayList();
			switch (getResultQuantity()) {
			case MULTIPLE:
				Object[] values;
				if (result.getClass().isArray()) {
					values = getObjectArray(result);
				} else if (result instanceof Iterable<?>) {
					values = Iterables.toArray((Iterable<?>) result, Object.class);
				} else {
					throw new RuleExecutionException("If resultQuantity is MULTIPLE ensure that either an Array or a Collection is defined as return value", context);
				}
				transformed.addAll(Tags.tags(getResultTag(), context.getRuleInput().getRoot(), values));
				break;
			case SINGLE:
			default:
				transformed.add(Tags.tag(getResultTag(), result, context.getRuleInput().getRoot()));
			}
			return transformed;
		}
	}

	/**
	 * Converts the result to an Object array depending on the component type of the array.
	 *
	 * @param result
	 *            Object representing the result array.
	 * @return Object array
	 */
	private Object[] getObjectArray(Object result) {
		if (result.getClass().getComponentType().isPrimitive()) {
			int length = Array.getLength(result);
			Object[] array = new Object[length];
			for (int i = 0; i < length; ++i) {
				array[i] = Array.get(result, i);
			}
			return array;
		} else {
			return (Object[]) result;
		}
	}

	// -------------------------------------------------------------u
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Gets {@link #resultTag}.
	 *
	 * @return {@link #resultTag}
	 */
	public String getResultTag() {
		return resultTag;
	}

	/**
	 * Gets {@link #resultQuantity}.
	 *
	 * @return {@link #resultQuantity}
	 */
	public Action.Quantity getResultQuantity() {
		return resultQuantity;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ActionMethod{" + "method=" + method + ", resultTag='" + resultTag + '\'' + ", resultQuantity=" + resultQuantity + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.method == null) ? 0 : this.method.hashCode());
		result = (prime * result) + ((this.resultQuantity == null) ? 0 : this.resultQuantity.hashCode());
		result = (prime * result) + ((this.resultTag == null) ? 0 : this.resultTag.hashCode());
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
		ActionMethod other = (ActionMethod) obj;
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		if (this.resultQuantity != other.resultQuantity) {
			return false;
		}
		if (this.resultTag == null) {
			if (other.resultTag != null) {
				return false;
			}
		} else if (!this.resultTag.equals(other.resultTag)) {
			return false;
		}
		return true;
	}
}
