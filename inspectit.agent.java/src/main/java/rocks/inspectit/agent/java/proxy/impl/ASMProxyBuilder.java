package rocks.inspectit.agent.java.proxy.impl;

import static info.novatec.inspectit.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static info.novatec.inspectit.org.objectweb.asm.Opcodes.ACC_STATIC;
import static info.novatec.inspectit.org.objectweb.asm.Opcodes.V1_5;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassVisitor;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.Type;
import info.novatec.inspectit.org.objectweb.asm.commons.GeneratorAdapter;
import info.novatec.inspectit.org.objectweb.asm.commons.Method;
import info.novatec.inspectit.org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.esotericsoftware.minlog.Log;

import rocks.inspectit.agent.java.instrumentation.asm.AsmUtil;
import rocks.inspectit.agent.java.instrumentation.asm.IInstrumenterConstant;
import rocks.inspectit.agent.java.proxy.IProxyBuildPlan;
import rocks.inspectit.agent.java.proxy.IProxyBuildPlan.IMethodBuildPlan;
import rocks.inspectit.agent.java.proxy.IProxyBuilder;
import rocks.inspectit.agent.java.proxy.IProxyClassInfo;
import rocks.inspectit.agent.java.proxy.IProxySubject;

/**
 * An ASM-Based implementation for the proxy cration mechanism.
 *
 * The classes are generated using the following pattern:<br>
 *
 * <pre>
 * {@code
 * public class ProxyClassNameHere extends ... implements ... {
 *
 * 		//stores the instance which recieves the calls
 * 		Object proxySubject;
 *
 * 		//store the delegation targets for every proxyied method
 * 		static Method method_1;
 * 		static Method method_2;
 * 		...
 *
 *		//the only constructor is just based on a constructor of the super class
 *		// the proxySubject is initialized before the super cosntructor is called
 *		// as the super constructor might call proxied methods
 * 		public ProxyClassNameHere(Object subject, superArg1, superArg2,...) {
 * 			this.proxySubject = subject;
 * 			super(superArg1, superArg2, ..);
 * 		}
 *
 * 		//the proxied methods all follow this pattern:
 * 		returnType myProxiedMethod1(arg1, arg2, ...) {
 * 			try {
 * 				return (returnType) method_1.invoke(proxySubject, arg1, arg2, arg3...);
 * 			}catch(InvocationTargetException e) {
 * 				throw e.getCause();
 * 			}
 * 		}
 *
 * }
 * </pre>
 *
 * @author Jonas Kunz
 */
@Component
public class ASMProxyBuilder implements IProxyBuilder {

	/**
	 * The name of the field storing the delegation target.
	 */
	private static final String SUBJECT_FIELD = "subject";

	/**
	 * THe pattern to name the method fields.
	 */
	private static final String METHOD_FIELD_PREFIX = "method_";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProxyClassInfo createProxyClass(IProxyBuildPlan plan) {
		String internalName = AsmUtil.getAsmInternalName(plan.getProxyClassName());
		Type proxyType = Type.getObjectType(internalName);

		String[] interfaces = getInternalNames(plan.getImplementedInterfaces());
		String superClass = Type.getInternalName(plan.getSuperClass());
		// generate a new class for the proxy with the defined interfaces and superclass
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_5, ACC_PUBLIC, internalName, null, superClass, interfaces);

		// create the subject field
		cw.visitField(ACC_PUBLIC, SUBJECT_FIELD, IInstrumenterConstant.OBJECT_TYPE.getDescriptor(), null, null).visitEnd();

		// create the fields for the delegation-target methods
		// The result map maps the proxied methods to their generated field's names
		Map<IMethodBuildPlan, String> methodFields = generateDelegationMethodTargetFields(plan, cw);

		generateConstructor(plan, proxyType, cw);

		// create the proxied methods
		for (Entry<IMethodBuildPlan, String> entry : methodFields.entrySet()) {
			createDelegationMethod(proxyType, entry.getKey(), entry.getValue(), cw);
		}

		cw.visitEnd();

		byte[] bytecode = cw.toByteArray();
		Class<?> cl = loadClass(bytecode, plan.getProxyClassName(), plan.getTargetClassLoader());

		// inject the method fields
		injectDelegationTargetFields(plan, methodFields, cl);

		return new ASMProxyClassInfo(cl);
	}

	/**
	 * Generates the static fields of type java.lang.reflection.Method which store the delegation
	 * targets.
	 *
	 * @param plan
	 *            the plan of the proxy to build.
	 * @param cw
	 *            the classwriter to use.
	 * @return a map mapping the individual proxied methods to their corresponding field names.
	 */
	private Map<IMethodBuildPlan, String> generateDelegationMethodTargetFields(IProxyBuildPlan plan, ClassWriter cw) {
		if (CollectionUtils.isEmpty(plan.getMethods())) {
			return Collections.emptyMap();
		}
		Map<IMethodBuildPlan, String> methodFields = new HashMap<IMethodBuildPlan, String>();
		int counter = 1;
		for (IMethodBuildPlan mplan : plan.getMethods()) {
			String name = METHOD_FIELD_PREFIX + counter;
			cw.visitField(ACC_STATIC, name, IInstrumenterConstant.REFLECTION_METHOD_TYPE.getDescriptor(), null, null).visitEnd();
			methodFields.put(mplan, name);
			counter++;
		}
		return methodFields;
	}

	/**
	 * After the proxy class has been generated and injected, the static fields storing the
	 * delegation targets have to be injected. This is done by this method.
	 *
	 * @param plan
	 *            the buildplan used for building the proxy
	 * @param methodFields
	 *            the map mapping the proxied methods to their field names.
	 * @param proxyClass
	 *            the generated and loaded proxy class.
	 */
	private void injectDelegationTargetFields(IProxyBuildPlan plan, Map<IMethodBuildPlan, String> methodFields, Class<?> proxyClass) {
		try {
			for (IMethodBuildPlan mplan : plan.getMethods()) {
				Field f = proxyClass.getDeclaredField(methodFields.get(mplan));
				f.setAccessible(true);
				f.set(null, mplan.getTargetMethod());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the constructor of the proxy class. This constructor takes the proxy subject as
	 * first argument, followed by the arguemtns which will be passed to the super constructor. The
	 * cosntructor performs the following steps: 1. Intialize the proxySubject field 2. invoke the
	 * super constructor
	 *
	 * @param plan
	 *            the build plan of the proxy class
	 * @param proxyType
	 *            the type of our proxy class
	 * @param cw
	 *            teh classwriter which is used to generate the proxy class
	 */
	private void generateConstructor(IProxyBuildPlan plan, Type proxyType, ClassWriter cw) {
		// The constructors parameters are the proxySubject followed by the arguments to pass to the
		// super constructor
		List<Class<?>> paramTypes = new ArrayList<Class<?>>();
		paramTypes.add(Object.class);
		paramTypes.addAll(plan.getConstructorParameterTypes());

		Method constructor = new Method(IInstrumenterConstant.CONSTRUCTOR_INTERNAL_NAME, Type.VOID_TYPE, getTypes(paramTypes));
		Method superConstructor = new Method(IInstrumenterConstant.CONSTRUCTOR_INTERNAL_NAME, Type.VOID_TYPE, getTypes(plan.getConstructorParameterTypes()));
		// our constructor throws the exceptions thrown by the super-constructor
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, constructor, null, getTypes(plan.getConstructorExceptions()), cw);

		// initialize the subject field
		mg.loadThis();
		mg.loadArg(0);
		mg.putField(proxyType, SUBJECT_FIELD, Type.getType(Object.class));

		// call super constructor using the remaining arguments
		mg.loadThis();
		mg.loadArgs(1, plan.getConstructorParameterTypes().size());
		mg.invokeConstructor(Type.getType(plan.getSuperClass()), superConstructor);

		mg.returnValue();
		// MAXS is computed as we set COMPUTE_MAXS flag for the ClassWriter
		mg.visitMaxs(0, 0);
		mg.endMethod();
	}

	/**
	 * Adds a method to the class based on the given MethodBuildPlan.
	 *
	 * @param proxyType
	 *            the type of the proxy class to which the method is added
	 * @param plan
	 *            the plan of the method to create
	 * @param fieldName
	 *            the name of the field storing the method to which the generated method will
	 *            delegate its calls
	 * @param cv
	 *            the class writer used for creating the class
	 */
	private void createDelegationMethod(Type proxyType, IMethodBuildPlan plan, String fieldName, ClassVisitor cv) {

		Type[] invokeMethodArgs = { IInstrumenterConstant.OBJECT_TYPE, IInstrumenterConstant.OBJECT_ARRAY_TYPE };
		Method invokeMethod = new Method("invoke", IInstrumenterConstant.OBJECT_TYPE, invokeMethodArgs);

		Method meth = new Method(plan.getMethodName(), Type.getType(plan.getReturnType()), getTypes(plan.getParameterTypes()));
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, meth, null, new Type[] {}, cv);

		// setup the catching of exceptions raised by Method.invoke
		Label tryBegin = new Label();
		Label tryEnd = new Label();
		Label catchBegin = new Label();
		mg.visitTryCatchBlock(tryBegin, tryEnd, catchBegin, Type.getInternalName(InvocationTargetException.class));

		mg.visitLabel(tryBegin);
		// load the method field on the stack
		mg.getStatic(proxyType, fieldName, IInstrumenterConstant.REFLECTION_METHOD_TYPE);

		// load the delegation instance
		mg.loadThis();
		mg.getField(proxyType, SUBJECT_FIELD, IInstrumenterConstant.OBJECT_TYPE);

		// load the arguments
		mg.loadArgArray();
		// call invoke
		mg.invokeVirtual(IInstrumenterConstant.REFLECTION_METHOD_TYPE, invokeMethod);
		// now we have the result on the stack - we possibly need to unbox it,
		// depending on the type
		Class<?> returnType = plan.getReturnType();
		if (returnType == void.class) {
			mg.pop();
		} else if (returnType.isPrimitive()) {
			mg.unbox(Type.getType(returnType));
		} else {
			mg.checkCast(Type.getType(returnType));

		}
		mg.returnValue();

		mg.visitLabel(tryEnd);

		// in case of an InvocationTargetException we extract the root exception
		// and throw it again
		// this way checked exceptions (e.g. IOException) can be thrown to the
		// API using the normal throw
		// in the proxy subjects implementation
		mg.visitLabel(catchBegin);
		mg.invokeVirtual(IInstrumenterConstant.THROWABLE_TYPE, IInstrumenterConstant.THROWABLE_GET_CAUSE_METHOD);
		// now the cause is on the stack, throw it
		mg.throwException();

		// MAXS is computed as we set COMPUTE_MAXS flag for the ClassWriter
		mg.visitMaxs(0, 0);
		mg.visitEnd();

	}

	/**
	 * Taken from the ASM FAQ. Loads the given bytecode as class using the given classloader.
	 *
	 * @param b
	 *            the bytecode
	 * @param className
	 *            the name of the class to load
	 * @param loader
	 *            the laoder to use
	 * @return the loaded class
	 */
	private Class<?> loadClass(byte[] b, String className, ClassLoader loader) {
		// override classDefine (as it is protected) and define the class.
		Class<?> clazz = null;
		try {
			Class<?> cls = Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });

			// protected method invocation
			method.setAccessible(true);
			Object[] args = new Object[] { className, b, Integer.valueOf(0), Integer.valueOf(b.length) };
			clazz = (Class<?>) method.invoke(loader, args);

		} catch (Exception e) {
			// a) the class already exists (would be very strange as we checked this beforehand)
			// golden rule: expect the unexpected
			try {
				Class.forName(className, false, loader);
				Log.error("PROXY CREATION ERROR: class is already present in classloader!");
			} catch (ClassNotFoundException e1) {
				// should be the expected case
				// b) something is wrong with the bytecode, let's verify it manually
				StringWriter logWriter = new StringWriter();
				CheckClassAdapter.verify(new ClassReader(b), loader, false, new PrintWriter(logWriter));
				Log.error("BYTECODE checking result : \r\n" + logWriter.toString());

			}
			throw new RuntimeException(e);

		}
		return clazz;
	}

	/**
	 * The proxy class information.
	 *
	 * @author Jonas Kunz
	 */
	private static class ASMProxyClassInfo implements IProxyClassInfo {

		/**
		 * The proxy class.
		 */
		private Class<?> proxyClass;
		/**
		 * The constructor to use for creating a proxy. Takes the subject as first argument.
		 */
		private Constructor<?> constructor;

		/**
		 * creates a build plan.
		 *
		 * @param proxyClass
		 *            the proxy class
		 */
		ASMProxyClassInfo(Class<?> proxyClass) {
			this.proxyClass = proxyClass;
			try {
				constructor = proxyClass.getConstructors()[0];
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Class<?> getProxyClass() {
			return proxyClass;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object createProxy(IProxySubject proxySubject) {
			try {
				Object[] superArgs = proxySubject.getProxyConstructorArguments();
				Object[] args = new Object[superArgs.length + 1];
				args[0] = proxySubject;
				System.arraycopy(superArgs, 0, args, 1, superArgs.length);

				Object proxy = constructor.newInstance(args);
				return proxy;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Utility function for retrieving the internal names for a set of classes.
	 *
	 * @param classes
	 *            the classes whose names shall be retrieved
	 * @return the internal names
	 */
	private static String[] getInternalNames(Collection<Class<?>> classes) {
		String[] names = new String[classes.size()];
		int i = 0;
		for (Class<?> clazz : classes) {
			names[i] = Type.getInternalName(clazz);
			i++;
		}
		return names;
	}

	/**
	 * Utility function for retrieving the types for a set of classes.
	 *
	 * @param classes
	 *            the classes whose types shall be retrieved
	 * @return the types
	 */
	private static Type[] getTypes(Collection<Class<?>> classes) {
		Type[] types = new Type[classes.size()];
		int i = 0;
		for (Class<?> clazz : classes) {
			types[i] = Type.getType(clazz);
			i++;
		}
		return types;
	}

}
