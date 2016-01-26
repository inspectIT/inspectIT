package info.novatec.inspectit.instrumentation.classcache;

import info.novatec.inspectit.instrumentation.classcache.util.MethodTypeSet;
import info.novatec.inspectit.instrumentation.classcache.util.TypeSet;
import info.novatec.inspectit.instrumentation.classcache.util.UpdateableSet;

import java.util.Collections;
import java.util.Set;

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
	 * Creates a new <code> InterfaceType </code>. This constructor is usually used if the
	 * annotation is loaded.
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
			initMethods();
		}
		methods.add(type);
	}

	/**
	 * Init {@link #methods}.
	 */
	private void initMethods() {
		methods = new MethodTypeSet();
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
			initSuperInterfaces();
		}
		superInterfaces.addOrUpdate(type);
	}

	/**
	 * Init {@link #superInterfaces}.
	 */
	private void initSuperInterfaces() {
		superInterfaces = new TypeSet<InterfaceType>();
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
			initSubInterfaces();
		}
		subInterfaces.addOrUpdate(type);
	}

	/**
	 * Init {@link #subInterfaces}.
	 */
	private void initSubInterfaces() {
		subInterfaces = new TypeSet<InterfaceType>();
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
	public void clearUnmeaningfulBackReferences() {
		super.clearUnmeaningfulBackReferences();

		if (null != subInterfaces) {
			subInterfaces.clear();
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
