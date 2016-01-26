package info.novatec.inspectit.instrumentation.classcache;

import info.novatec.inspectit.instrumentation.classcache.util.ArraySet;
import info.novatec.inspectit.instrumentation.classcache.util.TypeSet;
import info.novatec.inspectit.instrumentation.classcache.util.UpdateableSet;

import java.util.Collections;
import java.util.Set;

/**
 * Base type of most model types.
 *
 * @author Stefan Siegl
 */
public abstract class Type implements ImmutableType, TypeWithAnnotations {

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
		initHashes();
		hashes.add(hash);
		initialized = true;
	}

	/**
	 * Removes all references from the type which are only back references. The merging logic will
	 * ensure that a given instance will only have forward references set by executing this method.
	 */
	public abstract void clearUnmeaningfulBackReferences();

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
	public void addHash(String hash) {
		if (null == hashes) {
			initHashes();
		}
		hashes.add(hash);
		checkInitialized();
	}

	/**
	 * Init {@link #hashes}.
	 */
	private void initHashes() {
		hashes = new ArraySet<String>(1);
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
	public String getHash() {
		if (null == hashes) {
			return null;
		}
		return hashes.iterator().next();
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
	protected void checkInitialized() {
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
			initAnnotations();
		}
		annotations.addOrUpdate(annotationType);
	}

	/**
	 * Init {@link #annotations}.
	 */
	private void initAnnotations() {
		annotations = new TypeSet<AnnotationType>();
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
