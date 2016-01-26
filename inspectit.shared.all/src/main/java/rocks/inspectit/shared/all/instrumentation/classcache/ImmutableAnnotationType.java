package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * A class cache model element of the type Annotation that only provides immutable access.
 * 
 * @author Stefan Siegl
 */
public interface ImmutableAnnotationType extends ImmutableAbstractInterfaceType {

	/**
	 * Returns immutable annotated types as an unmodifiableSet.
	 * 
	 * @return Returns immutable annotated types as an unmodifiableSet.
	 */
	Set<? extends ImmutableTypeWithAnnotations> getImmutableAnnotatedTypes();

}
