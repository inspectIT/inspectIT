package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.UpdateableSet;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;

/**
 * Models a method used in classes and interfaces.
 *
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public class MethodType implements TypeWithAnnotations, TypeWithModifiers, ImmutableMethodType {

	/**
	 * The type of the method.
	 *
	 * @author Stefan Siegl
	 */
	public enum Character {
		/** A method. */
		METHOD,

		/** A constructor. */
		CONSTRUCTOR,

		/** Static constructor. */
		STATIC_CONSTRUCTOR;
	}

	/**
	 * The name of the method.
	 */
	private String name;

	/**
	 * The modifiers of the method.
	 */
	private int modifiers;

	/**
	 * Return type of the method.
	 */
	private String returnType;

	/**
	 * Ordered list of all parameters of this method.
	 */
	private List<String> parameters = null;

	/**
	 * List of all exceptions this method throws.
	 */
	private UpdateableSet<ClassType> exceptions = null;

	/**
	 * List of annotations of this method.
	 */
	private UpdateableSet<AnnotationType> annotations = null;

	/**
	 * The class this method belongs to.
	 */
	private TypeWithMethods classOrInterface;

	/**
	 * {@link MethodInstrumentationConfig} holding this method instrumentation properties.
	 */
	private MethodInstrumentationConfig methodInstrumentationConfig;

	/**
	 * Gets {@link #classType}.
	 *
	 * @return {@link #classType}
	 */
	public TypeWithMethods getClassOrInterfaceType() {
		return classOrInterface;
	}

	/**
	 * Returns {@link ImmutableTypeWithMethods} being this method's class or interface.
	 *
	 * @return Returns {@link ImmutableTypeWithMethods} being this method's class or interface.
	 */
	public ImmutableTypeWithMethods getImmutableClassOrInterfaceType() {
		return classOrInterface;
	}

	/**
	 * Sets {@link #classType}.
	 *
	 * @param type
	 *            New value for {@link #classType}
	 */
	public void setClassOrInterfaceType(TypeWithMethods type) {
		setClassOrInterfaceTypeNoBidirectionalUpdate(type);
		type.addMethodNoBidirectionalUpdate(this);
	}

	/**
	 * Sets {@link #classType} without updating the back reference.
	 *
	 * @param type
	 *            New value for {@link #classType}
	 */
	public void setClassOrInterfaceTypeNoBidirectionalUpdate(TypeWithMethods type) {
		this.classOrInterface = type;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #modifiers}.
	 *
	 * @return {@link #modifiers}
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
	}

	/**
	 * {@inheritDoc}
	 */
	public Character getMethodCharacter() {
		if ("<init>".equals(name)) {
			return Character.CONSTRUCTOR;
		} else if ("<clinit>".equals(name)) {
			return Character.STATIC_CONSTRUCTOR;
		} else {
			return Character.METHOD;
		}
	}

	/**
	 * Gets {@link #returnType}.
	 *
	 * @return {@link #returnType}
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Sets {@link #returnType}.
	 *
	 * @param returnType
	 *            New value for {@link #returnType}
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Gets {@link #parameters}.
	 *
	 * @return {@link #parameters}
	 */
	public List<String> getParameters() {
		if (null == parameters) {
			return Collections.emptyList();
		}
		return parameters;
	}

	/**
	 * Sets {@link #parameters}.
	 *
	 * @param parameters
	 *            New value for {@link #parameters}
	 */
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * sets a parameter at the specified index.
	 *
	 * @param index
	 *            index
	 * @param type
	 *            type.
	 */
	public void setParameterAt(int index, String type) {
		if (null == parameters) {
			initParameters();
		}
		parameters.set(index, type);
	}

	/**
	 * Init {@link #parameters}.
	 */
	private void initParameters() {
		parameters = new ArrayList<String>(1);
	}

	/**
	 * Adds an exception thrown by this method and ensures that the back-reference on the referred
	 * entity is set as well.
	 *
	 * @param type
	 *            the exception that is thrown.
	 */
	public void addException(ClassType type) {
		addExceptionNoBidirectionalUpdate(type);
		type.addMethodThrowingExceptionNoBidirectionalUpdate(this);
	}

	/**
	 * Adds an exception thrown by this method WITHOUT setting the back-reference. Please be aware
	 * that this method should only be called internally as this might mess up the bidirectional
	 * structure.
	 *
	 * @param type
	 *            the exception that is thrown.
	 */
	public void addExceptionNoBidirectionalUpdate(ClassType type) {
		if (null == exceptions) {
			exceptions = new TypeSet<ClassType>();
		}
		exceptions.addOrUpdate(type);
	}

	/**
	 * Gets {@link #exceptions} as an unmodifiableSet. If you want to add something to the list, use
	 * the provided adders, as they ensure that the bidirectional links are created.
	 *
	 * @return {@link #exceptions}
	 */
	public Set<ClassType> getExceptions() {
		if (null == exceptions) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(exceptions);
	}

	/**
	 * Removes the exception class from the type.
	 *
	 * @param type
	 *            {@link ClassType} to remove.
	 */
	public void removeException(ClassType type) {
		if (null == exceptions) {
			return;
		}
		exceptions.remove(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<? extends ImmutableClassType> getImmutableExceptions() {
		return getExceptions();
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
	public Set<? extends ImmutableAnnotationType> getImmutableAnnotations() {
		return getAnnotations();
	}

	/**
	 * Gets {@link #methodInstrumentationConfig}.
	 *
	 * @return {@link #methodInstrumentationConfig}
	 */
	public MethodInstrumentationConfig getMethodInstrumentationConfig() {
		return methodInstrumentationConfig;
	}

	/**
	 * Sets {@link #methodInstrumentationConfig}.
	 *
	 * @param methodInstrumentationConfig
	 *            New value for {@link #methodInstrumentationConfig}
	 */
	public void setMethodInstrumentationConfig(MethodInstrumentationConfig methodInstrumentationConfig) {
		this.methodInstrumentationConfig = methodInstrumentationConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isType() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isMethodType() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableType castToType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImmutableMethodType castToMethodType() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
		MethodType other = (MethodType) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!returnType.equals(other.returnType)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "MethodType [name=" + name + ", modifiers=" + modifiers + ", methodCharacter=" + getMethodCharacter() + ", returnType=" + returnType + ", parameters=" + parameters + "]";
	}

}
