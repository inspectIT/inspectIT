package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * A class cache model element of the type interface that only provides immutable access.
 * 
 * @author Stefan Siegl
 */
public interface ImmutableInterfaceType extends ImmutableAbstractInterfaceType {

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
	 * Returns immutable super interfaces as an unmodifiableSet.
	 * 
	 * @return Returns immutable super interfaces as an unmodifiableSet.
	 */
	Set<? extends ImmutableInterfaceType> getImmutableSuperInterfaces();

	/**
	 * Returns immutable sub interfaces as an unmodifiableSet.
	 * 
	 * @return Returns immutable sub interfaces as an unmodifiableSet.
	 */
	Set<? extends ImmutableInterfaceType> getImmutableSubInterfaces();

}
