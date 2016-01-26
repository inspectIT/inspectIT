package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * A class cache model element of a type that can have methods, which only provides immutable
 * access.
 * 
 * @author Stefan Siegl
 */
public interface ImmutableTypeWithMethods extends ImmutableType {

	/**
	 * Returns immutable methods as an unmodifiableSet.
	 * 
	 * @return Returns immutable methods as an unmodifiableSet.
	 */
	Set<? extends ImmutableMethodType> getImmutableMethods();
}
