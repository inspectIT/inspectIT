package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.classcache.util.ArraySet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.UpdateableSet;

/**
 * Base type of most model types.
 *
 * @author Stefan Siegl
 */
public abstract class Type implements ImmutableType, TypeWithAnnotations, TypeWithModifiers {

	/**
	 * The FQN of the type.
	 */
	protected final String fqn;

	/**
	 * Marks whether the type is completely initialized.
	 */
	protected boolean initialized = false;

	/**
	 * The hash of the byte code of this class. As we can have multiple version, we can keep a list
	 * of hashes.
	 */
	protected Set<String> hashes;

	/**
	 * The modifiers of the type.
	 */
	protected int modifiers;

	/**
	 * A list of all annotations assigned to this type.
	 */
	private UpdateableSet<AnnotationType> annotations = null;

	/**
	 * Creates a new <code> Type </code> instance. This constructor is usually invoked to add a
	 * <code>Type</code> at a point when the hashes and the modifiers are not yet available.
	 *
	 * @param fqn
	 *            fully qualified name.
	 */
	public Type(String fqn) {
		this.fqn = fqn;
	}

	/**
	 * Creates a new <code> Type </code> instance. This constructor is usually used when all
	 * information is available.
	 *
	 * @param fqn
	 *            fully qualified name.
	 * @param hash
	 *            the hash of the bytecode.
	 * @param modifiers
	 *            the modifiers of the class.
	 */
	public Type(String fqn, String hash, int modifiers) {
		this.fqn = fqn;
		this.modifiers = modifiers;
		addHash(hash);
		initialized = true;
	}

	/**
	 * Removes all references from the type which are only back references. The merging logic will
	 * ensure that a given instance will only have forward references set by executing this method.
	 */
	public abstract void cleanUpBackReferences();

	/**
	 * Returns depending types of this type considered by the class loading.
	 * <p>
	 * For example, for a class the depending types are super class and all directly realized
	 * interfaces.
	 *
	 * @return Returns depending types of this type
	 */
	public abstract Collection<Type> getDependingTypes();

	/**
	 * Removes any existing reference of this object from all the references.
	 * <p>
	 * Sub-classes should override to add their own removals.
	 */
	public void removeReferences() {
		for (AnnotationType annotationType : getAnnotations()) {
			annotationType.removeAnnotatedType(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFQN() {
		return fqn;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasMultipleVersions() {
		if (null == hashes) {
			return false;
		}
		return hashes.size() > 1;
	}

	/**
	 * Adds a byte code hash.
	 *
	 * @param hash
	 *            the byte code hash.
	 */
	public final void addHash(String hash) {
		if (null == hashes) {
			hashes = new ArraySet<String>(1);
		}
		hashes.add(hash);
		checkInitialized();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsHash(String hash) {
		if (null == hashes) {
			return false;
		}
		return hashes.contains(hash);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getHashes() {
		if (null == hashes) {
			return Collections.emptySet();
		}
		return hashes;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets {@link #modifiers}.
	 *
	 * @param modifiers
	 *            New value for {@link #modifiers}
	 */
	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
		checkInitialized();
	}

	/**
	 * Checks whether the type is completely initialized.
	 */
	protected final void checkInitialized() {
		if (!getHashes().isEmpty() && 0 != modifiers) {
			initialized = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAnnotation(AnnotationType annotationType) {
		addAnnotationNoBidirectionalUpdate(annotationType);
		annotationType.addAnnotatedType(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableAnnotationType> getImmutableAnnotations() {
		return getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAnnotationNoBidirectionalUpdate(AnnotationType annotationType) {
		if (null == annotations) {
			annotations = new TypeSet<AnnotationType>();
		}
		annotations.addOrUpdate(annotationType);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<AnnotationType> getAnnotations() {
		if (null == annotations) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAnnotation(AnnotationType annotationType) {
		if (null == annotations) {
			return;
		}
		annotations.remove(annotationType);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isType() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAnnotation() {
		return AnnotationType.class.isAssignableFrom(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInterface() {
		return InterfaceType.class.isAssignableFrom(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isClass() {
		return ClassType.class.isAssignableFrom(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isMethodType() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableType castToType() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableClassType castToClass() {
		return (ImmutableClassType) this;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableAnnotationType castToAnnotation() {
		return (ImmutableAnnotationType) this;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableInterfaceType castToInterface() {
		return (ImmutableInterfaceType) this;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableMethodType castToMethodType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
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
		Type other = (Type) obj;
		if (fqn == null) {
			if (other.fqn != null) {
				return false;
			}
		} else if (!fqn.equals(other.fqn)) {
			return false;
		}
		return true;
	}

}
