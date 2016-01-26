package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.List;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;

/**
 * A class cache model element of the type Method that only provides immutable access.
 *
 * @author Stefan Siegl
 */
public interface ImmutableMethodType extends ImmutableTypeWithAnnotations {

	/**
	 * Returns method name.
	 *
	 * @return Returns method name.
	 */
	String getName();

	/**
	 * Returns the {@link Character} of the method. Can be constructor or method.
	 *
	 * @return Returns the {@link Character} of the method. Can be constructor or method.
	 */
	Character getMethodCharacter();

	/**
	 * Returns the java-style modifiers of the method. The given value can be tested with the
	 * methods of the {@link Modifiers} class. Note that in comparison to the java-style values, we
	 * added one more to denote the package access: {@link Modifiers#PACKAGE}.
	 *
	 * @return Returns the java-style modifiers of the method.
	 */
	int getModifiers();

	/**
	 * Returns method parameters.
	 *
	 * @return Returns method parameters.
	 */
	List<String> getParameters();

	/**
	 * Returns {@link ImmutableTypeWithMethods} being this method's class or interface.
	 *
	 * @return Returns {@link ImmutableTypeWithMethods} being this method's class or interface.
	 */
	ImmutableTypeWithMethods getImmutableClassOrInterfaceType();

	/**
	 * Returns immutable annotations as an unmodifiableSet.
	 *
	 * @return Returns immutable annotations as an unmodifiableSet.
	 */
	Set<? extends ImmutableAnnotationType> getImmutableAnnotations();

	/**
	 * Returns immutable annotations as an unmodifiableSet.
	 *
	 * @return Returns immutable annotations as an unmodifiableSet.
	 */
	Set<? extends ImmutableClassType> getImmutableExceptions();

}
