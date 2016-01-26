package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.instrumentation.classcache.util.MethodTypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.UpdateableSet;

/**
 * Models an interface.
 *
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public class InterfaceType extends AbstractInterfaceType implements TypeWithMethods, ImmutableInterfaceType {

	/**
	 * A list of all direct super interfaces of this interface.
	 */
	private UpdateableSet<InterfaceType> superInterfaces = null;

	/**
	 * A list of all sub interfaces of this interface.
	 */
	private UpdateableSet<InterfaceType> subInterfaces = null;

	/**
	 * A list of all methods of this interface.
	 */
	private Set<MethodType> methods = null;

	/**
	 * No-arg constructor for serialization.
	 */
	protected InterfaceType() {
		super(null);
	}

	/**
	 * Creates a new <code> InterfaceType </code> without setting the hash and the modifiers. This
	 * constructor is usually used if you want to add the entity without the class being loaded.
	 *
	 * @param fqn
	 *            fully qualified name.
	 */
	public InterfaceType(String fqn) {
		super(fqn);
	}

	/**
	 * Creates a new <code> InterfaceType </code>. This constructor is usually used if the interface
	 * is loaded.
	 *
	 * @param fqn
	 *            fully qualified name.
	 * @param hash
	 *            the hash of the byte code.
	 * @param modifiers
	 *            the modifiers of the annotation.
	 */
	public InterfaceType(String fqn, String hash, int modifiers) {
		super(fqn, hash, modifiers);
	}

	/**
	 * Adds a method that this class contains and ensures that the back-reference on the referred
	 * entity is set as well.
	 *
	 * @param type
	 *            the method that is defined in this class.
	 */
	public void addMethod(MethodType type) {
		addMethodNoBidirectionalUpdate(type);
		type.setClassOrInterfaceTypeNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a method that this class contains WITHOUT setting the back-reference. Please be aware
	 * that this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 *
	 * @param type
	 *            the method that is defined in this class.
	 */
	public void addMethodNoBidirectionalUpdate(MethodType type) {
		if (methods == null) {
			methods = new MethodTypeSet();
		}
		methods.add(type);
	}

	/**
	 * Gets {@link #methods} as an unmodifiableSet. If you want to add something to the list, use
	 * the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #methods}
	 */
	public Set<MethodType> getMethods() {
		if (null == methods) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(methods);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableMethodType> getImmutableMethods() {
		return getMethods();
	}

	/**
	 * Adds a super interface of this interface and ensures that the back-reference on the referred
	 * entity is set as well.
	 *
	 * @param type
	 *            the super interface of this interface.
	 */
	public void addSuperInterface(InterfaceType type) {
		addSuperInterfaceNoBidirectionalUpdate(type);
		type.addSubInterfaceNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a super-interface of this class WITHOUT setting the back-reference. Please be aware that
	 * this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 *
	 * @param type
	 *            the super interface of this interface.
	 */
	public void addSuperInterfaceNoBidirectionalUpdate(InterfaceType type) {
		if (null == superInterfaces) {
			superInterfaces = new TypeSet<InterfaceType>();
		}
		superInterfaces.addOrUpdate(type);
	}

	/**
	 * Gets {@link #superInterfaces} as an unmodifiableSet. If you want to add something to the
	 * list, use the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #superInterfaces}
	 */
	public Set<InterfaceType> getSuperInterfaces() {
		if (null == superInterfaces) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(superInterfaces);
	}

	/**
	 * Removes the super interface from the type.
	 *
	 * @param type
	 *            {@link InterfaceType} to remove.
	 */
	public void removeSuperInterface(InterfaceType type) {
		if (null == superInterfaces) {
			return;
		}
		superInterfaces.remove(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableInterfaceType> getImmutableSuperInterfaces() {
		return getSuperInterfaces();
	}

	/**
	 * Adds a sub interface of this interface and ensures that the back-reference on the referred
	 * entity is set as well.
	 *
	 * @param type
	 *            the sub interface of this interface.
	 */
	public void addSubInterface(InterfaceType type) {
		addSubInterfaceNoBidirectionalUpdate(type);
		type.addSuperInterfaceNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a sub interface of this class WITHOUT setting the back-reference. Please be aware that
	 * this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 *
	 * @param type
	 *            the sub interface of this interface.
	 */
	public void addSubInterfaceNoBidirectionalUpdate(InterfaceType type) {
		if (null == subInterfaces) {
			subInterfaces = new TypeSet<InterfaceType>();
		}
		subInterfaces.addOrUpdate(type);
	}

	/**
	 * Gets {@link #subInterfaces} as an unmodifiableSet. If you want to add something to the list,
	 * use the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #subInterfaces}
	 */
	public Set<InterfaceType> getSubInterfaces() {
		if (null == subInterfaces) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(subInterfaces);
	}

	/**
	 * Removes the sub interface from the type.
	 *
	 * @param type
	 *            {@link InterfaceType} to remove.
	 */
	public void removeSubInterface(InterfaceType type) {
		if (null == subInterfaces) {
			return;
		}
		subInterfaces.remove(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableInterfaceType> getImmutableSubInterfaces() {
		return getSubInterfaces();
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

		if (null != subInterfaces) {
			subInterfaces.clear();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Type> getDependingTypes() {
		if (CollectionUtils.isNotEmpty(superInterfaces)) {
			return new ArrayList<Type>(superInterfaces);
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeReferences() {
		super.removeReferences();

		for (InterfaceType superInterface : getSuperInterfaces()) {
			superInterface.removeSubInterface(this);
		}

		for (InterfaceType subInterface : getSubInterfaces()) {
			subInterface.removeSuperInterface(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "InterfaceType [fqn=" + fqn + ", hashes=" + hashes + "]";
	}
}
