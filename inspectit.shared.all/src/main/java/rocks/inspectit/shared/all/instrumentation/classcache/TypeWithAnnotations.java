package rocks.inspectit.shared.all.instrumentation.classcache;


import java.util.Set;

/**
 * Models a type that can have annotations.
 *
 * @author Stefan Siegl
 */
public interface TypeWithAnnotations extends ImmutableTypeWithAnnotations {

	/**
	 * Adds an annotation to this method and ensures that the back-reference on the referred entity
	 * is set as well.
	 *
	 * @param type
	 *            the annotation to add.
	 */
	void addAnnotation(AnnotationType type);

	/**
	 * Adds an annotation to this method WITHOUT setting the back-reference. Please be aware that
	 * this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 *
	 * @param type
	 *            the annotation to add.
	 */
	void addAnnotationNoBidirectionalUpdate(AnnotationType type);

	/**
	 * Gets {@link #annotations} as an unmodifiableList. If you want to add something to the list,
	 * use the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #annotations}
	 */
	Set<AnnotationType> getAnnotations();

	/**
	 * Removes the given annotation from the type.
	 *
	 * @param annotationType
	 *            {@link AnnotationType} to remove.
	 */
	void removeAnnotation(AnnotationType annotationType);
}
