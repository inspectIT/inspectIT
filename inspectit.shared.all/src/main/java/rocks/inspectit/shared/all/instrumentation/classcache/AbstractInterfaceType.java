package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Collections;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.UpdateableSet;

/**
 * Abstract class for all types that can be used as interfaces (interfaces and annotations).
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractInterfaceType extends Type implements ImmutableAbstractInterfaceType {

	/**
	 * A list of all classes that directly realize this interface.
	 */
	private UpdateableSet<ClassType> realizingClasses = null;

	/**
	 * Primary constructor. Defines the fully qualified name of the type.
	 *
	 * @param fqn
	 *            fully qualified name.
	 */
	public AbstractInterfaceType(String fqn) {
		super(fqn);
	}

	/**
	 * Secondary constructor. Defines the fully qualified name of the type, hash and modifiers.
	 *
	 * @param fqn
	 *            fully qualified name.
	 * @param hash
	 *            the hash of the byte-code.
	 * @param modifiers
	 *            the modifiers of the class.
	 */
	public AbstractInterfaceType(String fqn, String hash, int modifiers) {
		super(fqn, hash, modifiers);
	}

	/**
	 * Adds a class that is annotated with this annotation and ensures that the back-reference on
	 * the referred entity is set as well.
	 *
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addRealizingClass(ClassType type) {
		addRealizingClassNoBidirectionalUpdate(type);
		type.addInterfaceNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a class that is annotated with this annotation WITHOUT setting the back-reference.
	 * Please be aware that this method should only be called internally as this might mess up the
	 * bidirectional structure.
	 *
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addRealizingClassNoBidirectionalUpdate(ClassType type) {
		if (null == realizingClasses) {
			initRealizingClasses();
		}
		realizingClasses.addOrUpdate(type);
	}

	/**
	 * Init {@link #realizingClasses}.
	 */
	private void initRealizingClasses() {
		realizingClasses = new TypeSet<ClassType>();
	}

	/**
	 * Gets {@link #realizingClasses} as an unmodifiableSet. If you want to add something to the
	 * list, use the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #realizingClasses}
	 */
	public Set<ClassType> getRealizingClasses() {
		if (null == realizingClasses) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(realizingClasses);
	}

	/**
	 * Removes the realizing class from the type.
	 *
	 * @param type
	 *            {@link ClassType} to remove.
	 */
	public void removeRealizingClass(ClassType type) {
		if (null == realizingClasses) {
			return;
		}
		realizingClasses.remove(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableClassType> getImmutableRealizingClasses() {
		return getRealizingClasses();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanUpBackReferences() {
		if (null != realizingClasses) {
			realizingClasses.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeReferences() {
		super.removeReferences();

		for (ClassType type : getRealizingClasses()) {
			type.removeInterface(this);
		}
	}

}
