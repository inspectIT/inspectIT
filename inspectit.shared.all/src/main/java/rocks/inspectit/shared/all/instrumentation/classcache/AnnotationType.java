package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeWithAnnotationsSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.UpdateableSet;

/**
 * Models an annotation.
 *
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public final class AnnotationType extends AbstractInterfaceType implements ImmutableAnnotationType {

	/**
	 * A list of methods annotated with this interface.
	 */
	private UpdateableSet<TypeWithAnnotations> annotatedTypes = null;

	/**
	 * No-arg constructor for serialization.
	 */
	protected AnnotationType() {
		super(null);
	}

	/**
	 * Creates a new <code> AnnotationType </code> without setting the hash and the modifiers. This
	 * constructor is usually used if you want to add the entity without the class being loaded.
	 *
	 * @param fqn
	 *            fully qualified name.
	 */
	public AnnotationType(String fqn) {
		super(fqn);
	}

	/**
	 * Creates a new <code> AnnotationType </code>. This constructor is usually used if the
	 * annotation is loaded.
	 *
	 * @param fqn
	 *            fully qualified name.
	 * @param hash
	 *            the hash of the byte code.
	 * @param modifiers
	 *            the modifiers of the annotation.
	 */
	public AnnotationType(String fqn, String hash, int modifiers) {
		super(fqn, hash, modifiers);
	}

	/**
	 * Adds a class that is annotated with this annotation and ensures that the back-reference on
	 * the referred entity is set as well.
	 *
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addAnnotatedType(TypeWithAnnotations type) {
		addAnnotatedClassNoBidirectionalUpdate(type);
		type.addAnnotationNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a class that is annotated with this annotation WITHOUT setting the back-reference.
	 * Please be aware that this method should only be called internally as this might mess up the
	 * bi-directional structure.
	 *
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addAnnotatedClassNoBidirectionalUpdate(TypeWithAnnotations type) {
		if (null == annotatedTypes) {
			annotatedTypes = new TypeWithAnnotationsSet();
		}
		annotatedTypes.addOrUpdate(type);
	}

	/**
	 * Gets {@link #annotatedMethods} as an unmodifiableList. If you want to add something to the
	 * list, use the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #annotatedMethods}
	 */
	public Set<TypeWithAnnotations> getAnnotatedTypes() {
		if (null == annotatedTypes) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(annotatedTypes);
	}

	/**
	 * Removes annotated type from type.
	 *
	 * @param type
	 *            {@link TypeWithAnnotations} to remove.
	 */
	public void removeAnnotatedType(TypeWithAnnotations type) {
		if (null == annotatedTypes) {
			return;
		}
		annotatedTypes.remove(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableTypeWithAnnotations> getImmutableAnnotatedTypes() {
		return getAnnotatedTypes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAnnotation(AnnotationType annotationType) {
		addAnnotationNoBidirectionalUpdate(annotationType);
		annotationType.addAnnotatedType(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<? extends ImmutableAnnotationType> getImmutableAnnotations() {
		return getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanUpBackReferences() {
		super.cleanUpBackReferences();
		if (null != annotatedTypes) {
			annotatedTypes.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Type> getDependingTypes() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeReferences() {
		super.removeReferences();

		for (ClassType type : getRealizingClasses()) {
			type.removeAnnotation(this);
		}

		for (TypeWithAnnotations typeWithAnnotations : getAnnotatedTypes()) {
			typeWithAnnotations.removeAnnotation(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AnnotationType [fqn=" + fqn + ", hashes=" + hashes + "]";
	}
}
