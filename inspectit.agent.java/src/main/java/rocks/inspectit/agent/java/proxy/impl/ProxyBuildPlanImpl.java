package rocks.inspectit.agent.java.proxy.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.agent.java.proxy.IProxyBuildPlan;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.util.AutoboxingHelper;



/**
 * An implementation for {@link IProxyBuildPlan}.
 *
 * @author Jonas Kunz
 */
public final class ProxyBuildPlanImpl implements IProxyBuildPlan {

	/**
	 * the superclass the proxy will inherit from.
	 */
	private Class<?> superClass;

	/**
	 * A list of all interfaces implmented by the proxy class.
	 */
	private List<Class<?>> implementedInterfaces;

	/**
	 * The paramter types of the constructor for {@link #superClass} that will be used.
	 */
	private List<Class<?>> constructorParameterTypes;

	/**
	 * A List of all proxied methods.
	 */
	private List<MethodBuildPlanImpl> methods;

	/**
	 * The full qualified name of the proxy class.
	 */
	private String proxyClassName;

	/**
	 * Constructor.
	 */
	private ProxyBuildPlanImpl() {
		superClass = Object.class;
		implementedInterfaces = new ArrayList<Class<?>>();
		constructorParameterTypes = new ArrayList<Class<?>>();
		methods = new ArrayList<MethodBuildPlanImpl>();
	}


	/**
	 * Creates a build plan based on the {@link ProxyFor} and {@link ProxyMethod} annotations of the given subject.
	 *
	 * @param proxySubjectType the type of the subject the proxy should delegate to
	 * @param proxyName the name of the proxy class to be created
	 * @param context the classloader providing all needed dependencies for the proxy
	 * @return the proxy plan
	 * @throws InvalidProxyDescriptionException if the information is invalid or incomplete
	 */
	public static IProxyBuildPlan create(Class<?> proxySubjectType, String proxyName, ClassLoader context) throws InvalidProxyDescriptionException {
		ProxyBuildPlanImpl plan = new ProxyBuildPlanImpl();
		plan.proxyClassName = proxyName;
		ProxyFor proxyInfo = proxySubjectType.getAnnotation(ProxyFor.class);
		if (proxyInfo  == null) {
			InvalidProxyDescriptionException.throwException("%s does not have the ProxyFor - annotation!", proxySubjectType);
		}

		//collect the super type
		String superClassName = proxyInfo.superClass();
		if (StringUtils.isNotEmpty(superClassName)) {
			Class<?> superClass = getType(superClassName, context);
			plan.superClass = superClass;
		}
		//collect the interfaces
		for (String interfaceName : proxyInfo.implementedInterfaces()) {
			Class<?> interfaceClass = getType(interfaceName, context);
			plan.implementedInterfaces.add(interfaceClass);
		}

		//add the constructor parameters to the build plan
		for (String paramTypeName : proxyInfo.constructorParameterTypes()) {
			Class<?> paramType = getType(paramTypeName, context);
			plan.constructorParameterTypes.add(paramType);
		}
		//collect the method information
		for (Method method : proxySubjectType.getMethods()) {
			if (method.isAnnotationPresent(ProxyMethod.class)) {
				//we catch exceptions because methods may be marked as optional
				ProxyMethod anno = method.getAnnotation(ProxyMethod.class);
				MethodBuildPlanImpl methodPlan = new MethodBuildPlanImpl();
				try {
					methodPlan.targetMethod = method;

					//collect the name
					if (anno.methodName().length() == 0) {
						methodPlan.methodName = method.getName();
					} else {
						methodPlan.methodName = anno.methodName();
					}

					//collect the return type
					if (anno.returnType().length() == 0) {
						methodPlan.returnType = method.getReturnType();
					} else {
						methodPlan.returnType = getType(anno.returnType(), context);
					}

					//collect the parameter types
					if (anno.parameterTypes().length == 0) {
						for (Class<?> paramType : method.getParameterTypes()) {
							methodPlan.parameterTypes.add(paramType);
						}
					} else {
						//make sure the parameter count is equal
						if (anno.parameterTypes().length != method.getParameterTypes().length) {
							InvalidProxyDescriptionException.throwException("The parameter count in the ProxyMethod annotation "
									+ "does not match the actual parameter count for " + method.getName());
						}
						for (String paramType : anno.parameterTypes()) {
							methodPlan.parameterTypes.add(getType(paramType, context));
						}
					}
					plan.methods.add(methodPlan);
				} catch (InvalidProxyDescriptionException e) {
					if (anno.isOptional()) {
						//silently skip
						continue;
					} else {
						throw e;
					}
				}
			}
		}

		//make sure everything is valid
		plan.validate();

		return plan;
	}

	/**
	 * Fetches the type with the given name, if an exception occurs an {@link InvalidProxyDescriptionException} is thrown.
	 * Also supports the primitive classes (like int.class)
	 * @param typeName the name of the type
	 * @param context the context
	 * @return the class representing the given typename
	 * @throws InvalidProxyDescriptionException if the type was not found
	 */
	private static Class<?> getType(String typeName, ClassLoader context) throws InvalidProxyDescriptionException {
		try {
			return AutoboxingHelper.findClass(typeName, false, context);
		} catch (ClassNotFoundException e) {
			InvalidProxyDescriptionException.throwException("The type %s does not exist in the given context!", typeName);
			return null; // never reached
		}
	}


	@Override
	public Class<?> getSuperClass() {
		return superClass;
	}

	@Override
	public List<Class<?>> getImplementedInterfaces() {
		return Collections.unmodifiableList(implementedInterfaces);
	}

	@Override
	public List<Class<?>> getConstructorParameterTypes() {
		return Collections.unmodifiableList(constructorParameterTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getConstructorExceptions() {
		try {

			Constructor<?> constructor = getSuperClass().getDeclaredConstructor(constructorParameterTypes.toArray(new Class<?>[constructorParameterTypes.size()]));
			return Arrays.asList(constructor.getExceptionTypes());
		} catch (Exception e) {
			throw new RuntimeException("Error fetching constructor exception types!", e);
		}
	}

	@Override
	public List<MethodBuildPlanImpl> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassLoader getTargetClassLoader() {

		HashSet<ClassLoader> dependencies = new HashSet<ClassLoader>();
		dependencies.add(getSuperClass().getClassLoader());
		for (Class<?> interf : getImplementedInterfaces()) {
			dependencies.add(interf.getClassLoader());
		}
		for (MethodBuildPlanImpl meth : getMethods()) {
			dependencies.add(meth.getReturnType().getClassLoader());
			for (Class<?> type : meth.getParameterTypes()) {
				dependencies.add(type.getClassLoader());
			}
		}
		return getLowestClassLoader(dependencies);
	}


	/**
	 * Returns the lowest class loader from the given set of classloaders.
	 * @param loaders the loaders to check
	 * @return the lowest class loader
	 */
	private ClassLoader getLowestClassLoader(Set<ClassLoader> loaders) {

		//remove bootstrap loader

		if (loaders.isEmpty() || ((loaders.size() == 1) && loaders.contains(null))) {
			return ClassLoader.getSystemClassLoader(); //bootstrap class loader
		}
		//a loader is the lowest if the other ones in the set are all parents of it
		for (ClassLoader cl : loaders) {
			HashSet<ClassLoader> leftLoaders = new HashSet<ClassLoader>(loaders);
			leftLoaders.remove(null);
			ClassLoader it = cl;
			while (it != null) {
				leftLoaders.remove(it);
				it = it.getParent();
			}
			//if nothing is left, this is the lowest
			if (leftLoaders.isEmpty()) {
				return cl;
			}
		}
		throw new RuntimeException("The given loaders are not on a single path towards the bootstrap loader!");
	}

	/**
	 * @return the superclass plus all directly implemented interfaces
	 */
	private Set<Class<?>> getAllParentTypes() {
		HashSet<Class<?>> allParents = new HashSet<Class<?>>();
		allParents.add(superClass);
		allParents.addAll(implementedInterfaces);
		return allParents;

	}

	/**
	 * Makes sure that this build plan is valid (e.g. all abstract methods are overwritten and so on)
	 * @throws InvalidProxyDescriptionException if something is wrong
	 */
	private void validate() throws InvalidProxyDescriptionException {

		if (superClass.isInterface() || superClass.isAnnotation()) {
			InvalidProxyDescriptionException.throwException("The proxy class can't inherit from %s!", superClass);
		}
		for (Class<?> interf : implementedInterfaces) {
			if (!interf.isInterface()) {
				InvalidProxyDescriptionException.throwException("%s is not an interface!", interf);
			}
		}
		validateConstructor();
		//check for duplicates
		for (MethodBuildPlanImpl first : methods) {
			for (MethodBuildPlanImpl second : methods) {
				if ((!first.equals(second)) && first.isSignatureEqual(second.getMethodName(), second.getParameterTypes())) {
					InvalidProxyDescriptionException.throwException(
							"The method %s%s occurs multiple times!",
							second.getMethodName(), second.getParameterTypes().toArray(new Class<?>[0]));
				}
			}
		}
		//make sure that every abstract method of the superclass / of the interfaces is overwritten
		for (Class<?> parent : getAllParentTypes()) {
			for (Method meth : parent.getMethods()) {
				if ((meth.getModifiers()  & Modifier.ABSTRACT) != 0) {
					boolean isOverriden = false;
					for (MethodBuildPlanImpl mplan : methods) {
						if (mplan.checkMethodOverriding(meth)) {
							isOverriden = true;
						}
					}
					if (!isOverriden) {
						InvalidProxyDescriptionException.throwException(
								"The abstract method %s%s of the type %s is not overriden!", meth.getName(), meth.getParameterTypes(), parent);
					}
				}
			}
		}
	}

	/**
	 * Validates the cosntructor.
	 * @throws InvalidProxyDescriptionException if something is wrong
	 */
	private void validateConstructor() throws InvalidProxyDescriptionException {
		Class<?>[] params = this.getConstructorParameterTypes().toArray(new Class<?>[0]);
		try {
			Constructor<?> ctr = superClass.getDeclaredConstructor(params);
			if ((ctr.getModifiers() & (Modifier.PROTECTED | Modifier.PUBLIC)) == 0) {
				InvalidProxyDescriptionException.throwException(
						"The constructor with parameter types %s found in class %s is neither public nor protected!", params, superClass);
			}
		} catch (NoSuchMethodException e) {
			InvalidProxyDescriptionException.throwException(
					"No accessible constructor with parameter types %s found in class %s", params, superClass);
		}
	}

	@Override
	public String getProxyClassName() {
		return proxyClassName;
	}


	/**
	 *
	 *  Holds the information about a proxied method.
	 * @author Jonas Kunz
	 */
	private static final class MethodBuildPlanImpl implements IMethodBuildPlan {

		/**
		 * The name of the method which will be proxied.
		 */
		private String methodName;

		/**
		 * The return type of the method which will be proxied.
		 */
		private Class<?> returnType;
		/**
		 * The parameter types of the proxied method.
		 */
		private List<Class<?>> parameterTypes;

		/**
		 * The method to which the proxied method will delegate its calls.
		 */
		private Method targetMethod;

		/**
		 *
		 */
		MethodBuildPlanImpl() {
			parameterTypes = new ArrayList<Class<?>>();
		}

		@Override
		public String getMethodName() {
			return methodName;
		}
		@Override
		public Class<?> getReturnType() {
			return returnType;
		}
		@Override
		public List<Class<?>> getParameterTypes() {
			return parameterTypes;
		}

		@Override
		public Method getTargetMethod() {
			return targetMethod;
		}


		/**
		 * Check for method overwriting.
		 * Returns true if every of the following condition holds:
		 * 	a) the names of the methods are equal
		 *  b) both methods take the same number of arguments
		 *  c) the arguemnt types match exactly
		 *  d) the return type of the method represented by this build plan is assignable to the return type of the given other method
		 * @param otherMethod the method to check
		 * @return true if the method described in this plan overrides the given method
		 */
		private boolean checkMethodOverriding(Method otherMethod) {

			if (!otherMethod.getReturnType().isAssignableFrom(this.returnType)) {
				return false;
			}
			Class<?>[] parameterTypes = otherMethod.getParameterTypes();

			return isSignatureEqual(otherMethod.getName(), Arrays.asList(parameterTypes));
		}

		/**
		 * Checks whether the signature (name and parameter types) are equal.
		 * @param methodName the name to compare
		 * @param paramTypes the parameter types to compare against
		 * @return true, if the signature is equal
		 */
		private boolean isSignatureEqual(String methodName, List<Class<?>> paramTypes) {
			if (!this.methodName.equals(methodName)) {
				return false;
			}
			if (paramTypes.size() != this.parameterTypes.size()) {
				return false;
			}
			for (int i = 0;  i < paramTypes.size(); i++) {
				if (paramTypes.get(i) != this.parameterTypes.get(i)) {
					return false;
				}
			}
			return true;
		}


	}

}