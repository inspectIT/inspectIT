package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Collection;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;

/**
 * A class cache model element of the type Class that only provides immutable access.
 *
 * @author Stefan Siegl
 */
public interface ImmutableClassType extends ImmutableType {

	/**
	 * Returns immutable methods as an unmodifiableSet.
	 *
	 * @return Returns immutable methods as an unmodifiableSet.
	 */
	Set<? extends ImmutableMethodType> getImmutableMethods();

	/**
	 * Returns immutable annotations as an unmodifiableSet.
	 *
	 * @return Returns immutable annotations as an unmodifiableSet.
	 */
	Set<? extends ImmutableAnnotationType> getImmutableAnnotations();

	/**
	 * Returns immutable super classes as an unmodifiableSet.
	 *
	 * @return Returns immutable super classes as an unmodifiableSet.
	 */
	Set<? extends ImmutableClassType> getImmutableSuperClasses();

	/**
	 * Returns immutable sub classes as an unmodifiableSet.
	 *
	 * @return Returns immutable sub classes as an unmodifiableSet.
	 */
	Set<? extends ImmutableClassType> getImmutableSubClasses();

	/**
	 * Returns immutable realized interfaces as an unmodifiableSet.
	 *
	 * @return Returns immutable realized interfaces as an unmodifiableSet.
	 */
	Set<? extends ImmutableAbstractInterfaceType> getImmutableRealizedInterfaces();

	/**
	 * Does the class type have instrumentation points.
	 *
	 * @return Does the class type have instrumentation points.
	 */
	boolean hasInstrumentationPoints();

	/**
	 * Returns whether or not this class is a subclass of {@link Throwable} Please note that the
	 * check is done by traversing the parent classes to find the {@link Throwable} class. In case
	 * this class is not loaded an exception cannot be identified.
	 *
	 * @return if this class is an exception.
	 */
	boolean isException();

	/**
	 * Determines if this {@link ClassType} is sub-class of the given class fully qualified name.
	 * This is quick helper method that iterates all super-class and checks it's FQN's and compares
	 * it with the given one. No wild-cards can be used here.
	 *
	 * @param superClassFqn
	 *            Class FQN that this {@link ClassType} should extend.
	 * @return <code>true</code> if this class type is in hierarchy a sub-class of given class FQN
	 */
	boolean isSubClassOf(String superClassFqn);

	/**
	 * Returns collection of all {@link MethodInstrumentationConfig} that have been added to methods
	 * as instrumentation points.
	 *
	 * @return Returns collection of all {@link MethodInstrumentationConfig} that have been added to
	 *         methods as instrumentation points.
	 */
	Collection<MethodInstrumentationConfig> getInstrumentationPoints();
}
