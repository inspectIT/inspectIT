package info.novatec.inspectit.instrumentation.classcache;

import info.novatec.inspectit.instrumentation.classcache.util.MethodTypeSet;
import info.novatec.inspectit.instrumentation.classcache.util.TypeSet;
import info.novatec.inspectit.instrumentation.classcache.util.UpdateableSet;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * Models a Java class.
 * 
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public class ClassType extends Type implements TypeWithMethods, ImmutableClassType {

	/**
	 * static final reference to the class name of the java.lang.Throwable class.
	 */
	private static final String FQN_THROWABLE = Throwable.class.getName();

	/**
	 * The super-classes of this class.
	 */
	private UpdateableSet<ClassType> superClasses = null;

	/**
	 * A list of all sub classes of this class.
	 */
	private UpdateableSet<ClassType> subClasses = null;

	/**
	 * A list of all interfaces realized by this class.
	 */
	private UpdateableSet<AbstractInterfaceType> realizedInterfaces = null;

	/**
	 * A list of all methods of this interface.
	 */
	private Set<MethodType> methods = null;

	/**
	 * A list of all methods that throw this exception. Only filled if this class is an exception.
	 */
	private Set<MethodType> methodsThrowingThisException = null;

	/**
	 * No-arg constructor for serialization.
	 */
	protected ClassType() {
		super(null);
	}

	/**
	 * Creates a new <code> ClassType </code> without setting the hash and the modifiers. This
	 * constructor is usually used if you want to add the entity without the class being loaded.
	 * 
	 * @param fqn
	 *            fully qualified name.
	 */
	public ClassType(String fqn) {
		super(fqn);
	}

	/**
	 * Creates a new <code> ClassType </code>. This constructor is usually used if the annotation is
	 * loaded.
	 * 
	 * @param fqn
	 *            fully qualified name.
	 * @param hash
	 *            the hash of the byte code.
	 * @param modifiers
	 *            the modifiers of the annotation.
	 */
	public ClassType(String fqn, String hash, int modifiers) {
		super(fqn, hash, modifiers);
	}

	/**
	 * Adds a class that is annotated with this annotation and ensures that the back-reference on
	 * the referred entity is set as well.
	 * 
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addInterface(AbstractInterfaceType type) {
		addInterfaceNoBidirectionalUpdate(type);
		type.addRealizingClassNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a class that is annotated with this annotation WITHOUT setting the back-reference.
	 * Please be aware that this method should only be called internally as this might mess up the
	 * bidirectional structure.
	 * 
	 * @param type
	 *            the class that uses this annotation.
	 */
	public void addInterfaceNoBidirectionalUpdate(AbstractInterfaceType type) {
		if (null == realizedInterfaces) {
			initRealizedInterfaces();
		}
		realizedInterfaces.addOrUpdate(type);
	}

	/**
	 * Init {@link #realizedInterfaces}.
	 */
	private void initRealizedInterfaces() {
		realizedInterfaces = new TypeSet<AbstractInterfaceType>();
	}

	/**
	 * Gets {@link #realizedInterfaces} as an unmodifiableSet. If you want to add something to the
	 * list, use the provided adders, as they ensure that the bidirectional links are created.
	 * 
	 * @return {@link #realizedInterfaces}
	 */
	public Set<AbstractInterfaceType> getRealizedInterfaces() {
		if (null == realizedInterfaces) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(realizedInterfaces);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableAbstractInterfaceType> getImmutableRealizedInterfaces() {
		return getRealizedInterfaces();
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
	 * Adds a superclass of this class and ensures that the back-reference on the referred entity is
	 * set as well.
	 * 
	 * @param type
	 *            the superclass of this class.
	 */
	public void addSuperClass(ClassType type) {
		addSuperClassNoBidirectionalUpdate(type);
		type.addSubclassNoBidirectionalUpdate(this);
	}

	/**
	 * Adds the superclass of this class WITHOUT setting the back-reference. Please be aware that
	 * this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 * 
	 * @param type
	 *            the method that is defined in this class.
	 */
	public void addSuperClassNoBidirectionalUpdate(ClassType type) {
		if (null == superClasses) {
			initSuperClasses();
		}
		superClasses.addOrUpdate(type);
	}

	/**
	 * Init {@link #superClasses}.
	 */
	private void initSuperClasses() {
		superClasses = new TypeSet<ClassType>();
	}

	/**
	 * Gets {@link #superClasses} as an unmodifiableSet. If you want to add something to the list,
	 * use the provided adders, as they ensure that the bidirectional links are created.
	 * 
	 * @return {@link #superClasses}
	 */
	public Set<ClassType> getSuperClasses() {
		if (null == superClasses) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(superClasses);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableClassType> getImmutableSuperClasses() {
		return getSuperClasses();
	}

	/**
	 * Adds a subclass of this class and ensures that the back-reference on the referred entity is
	 * set as well.
	 * 
	 * @param type
	 *            the subclass of this class.
	 */
	public void addSubclass(ClassType type) {
		addSubclassNoBidirectionalUpdate(type);
		type.addSuperClassNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a subclass of this class WITHOUT setting the back-reference. Please be aware that this
	 * method should only be called internally as this might mess up the bidirectional structure.
	 * 
	 * @param type
	 *            the subclass of this class.
	 */
	public void addSubclassNoBidirectionalUpdate(ClassType type) {
		if (null == subClasses) {
			initSubClasses();
		}
		subClasses.addOrUpdate(type);
	}

	/**
	 * Init {@link #subClasses}.
	 */
	private void initSubClasses() {
		subClasses = new TypeSet<ClassType>();
	}

	/**
	 * Gets {@link #subClasses} as an unmodifiableSer. If you want to add something to the list, use
	 * the provided adders, as they ensure that the bidirectional links are created.
	 * 
	 * @return {@link #subClasses}
	 */
	public Set<ClassType> getSubClasses() {
		if (null == subClasses) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(subClasses);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableClassType> getImmutableSubClasses() {
		return getSubClasses();
	}

	/**
	 * Adds a method that is throwing this class as exception and ensures that the back-reference on
	 * the referred entity is set as well.
	 * 
	 * @param type
	 *            a method that is throwing this class as exception
	 */
	public void addMethodThrowingException(MethodType type) {
		addMethodThrowingExceptionNoBidirectionalUpdate(type);
		type.addExceptionNoBidirectionalUpdate(this);
	}

	/**
	 * Adds a method that is throwing this class as exception WITHOUT setting the back-reference.
	 * Please be aware that this method should only be called internally as this might mess up the
	 * bidirectional structure.
	 * 
	 * @param type
	 *            a method that is throwing this class as exception
	 */
	public void addMethodThrowingExceptionNoBidirectionalUpdate(MethodType type) {
		if (null == methodsThrowingThisException) {
			initMethodsThrowingThisException();
		}
		methodsThrowingThisException.add(type);
	}

	/**
	 * Init {@link #methodsThrowingThisException}.
	 */
	private void initMethodsThrowingThisException() {
		methodsThrowingThisException = new MethodTypeSet();
	}

	/**
	 * Gets {@link #methodsThrowingThisException} as an unmodifiableSet. If you want to add
	 * something to the list, use the provided adders, as they ensure that the bidirectional links
	 * are created.
	 * 
	 * @return {@link #methodsThrowingThisException}
	 */
	public Set<MethodType> getMethodsThrowingThisException() {
		if (null == methodsThrowingThisException) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(methodsThrowingThisException);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearUnmeaningfulBackReferences() {
		if (null != methodsThrowingThisException) {
			methodsThrowingThisException.clear();
		}

		if (null != subClasses) {
			subClasses.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isException() {
		return isSubClassOf(FQN_THROWABLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSubClassOf(String superClassFqn) {
		if (null == superClassFqn) {
			return false;
		}

		if (CollectionUtils.isNotEmpty(superClasses)) {
			for (ClassType superClass : superClasses) {
				if (superClassFqn.equals(superClass.getFQN())) {
					return true;
				} else if (superClass.isSubClassOf(superClassFqn)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasInstrumentationPoints() {
		if (CollectionUtils.isEmpty(methods)) {
			return false;
		}

		for (MethodType methodType : methods) {
			if (null != methodType.getMethodInstrumentationConfig()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<MethodInstrumentationConfig> getInstrumentationPoints() {
		if (CollectionUtils.isEmpty(methods)) {
			return Collections.emptyList();
		}

		Collection<MethodInstrumentationConfig> instrumentationPoints = new ArrayList<MethodInstrumentationConfig>();
		for (MethodType methodType : methods) {
			CollectionUtils.addIgnoreNull(instrumentationPoints, methodType.getMethodInstrumentationConfig());
		}
		return instrumentationPoints;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ClassType [fqn=" + fqn + ", hashes=" + hashes + "]";
	}
}
