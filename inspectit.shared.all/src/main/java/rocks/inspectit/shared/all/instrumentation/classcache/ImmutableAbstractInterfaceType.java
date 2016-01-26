package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * Immutable version of the {@link AbstractInterfaceType}.
 * 
 * @author Ivan Senic
 * 
 */
public interface ImmutableAbstractInterfaceType extends ImmutableType {

	/**
	 * Gets immutable realizing classes as an unmodifiableSet.
	 * 
	 * @return Immutable realizing classes as an unmodifiableSet.
	 */
	Set<? extends ImmutableClassType> getImmutableRealizingClasses();
}
