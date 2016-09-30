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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.esotericsoftware.minlog.Log;

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
	private static final String METHOD_FIELD_PATTERN = "method_";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProxyClassInfo createProxyClass(IProxyBuildPlan plan) {

		String internalName = plan.getProxyClassName().replaceAll(
				Pattern.quote("."), "/");
		Type proxyType = Type.getType("L" + internalName + ";");

		String[] interfaces = getInternalNames(plan.getImplementedInterfaces());
		String superClass = Type.getInternalName(plan.getSuperClass());
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_5, ACC_PUBLIC, internalName, null, superClass, interfaces);

		// create the subejct field
		cw.visitField(ACC_PUBLIC, SUBJECT_FIELD, Type.getDescriptor(Object.class), null, null).visitEnd();

		// create the fields for the delegation-target methods
		Map<IMethodBuildPlan, String> methodFields = new HashMap<IMethodBuildPlan, String>();
		int counter = 1;
		for (IMethodBuildPlan mplan : plan.getMethods()) {
			String name = METHOD_FIELD_PATTERN + counter;
			cw.visitField(ACC_PUBLIC + ACC_STATIC, name, Type.getDescriptor(java.lang.reflect.Method.class), null, null).visitEnd();
			methodFields.put(mplan, name);
			counter++;
		}

		// Generate constructor, we start by defining the relevant signatures
		List<Class<?>> paramTypes = new ArrayList<Class<?>>();
		paramTypes.add(Object.class);
		paramTypes.addAll(plan.getConstructorParameterTypes());
		Method constructor = new Method("<init>", Type.VOID_TYPE, getTypes(paramTypes));
		Method superConstructor = new Method("<init>", Type.VOID_TYPE, getTypes(plan.getConstructorParameterTypes()));
		// our constructor throws the exceptions thown by the super-constructor
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, constructor, null, getTypes(plan.getConstructorExceptions()), cw);

		//initialize the subject field
		mg.loadThis();
		mg.loadArg(0);
		mg.putField(proxyType, SUBJECT_FIELD, Type.getType(Object.class));

		// call super constructor using the remaining arguments
		mg.loadThis();
		mg.loadArgs(1, plan.getConstructorParameterTypes().size());
		mg.invokeConstructor(Type.getType(plan.getSuperClass()), superConstructor);

		mg.returnValue();
		mg.visitMaxs(0, 0);
		mg.endMethod();

		// create the proxied methods
		for (IMethodBuildPlan mplan : plan.getMethods()) {
			createDelegationMethod(proxyType, mplan, methodFields.get(mplan),
					cw);
		}

		cw.visitEnd();

		byte[] bytecode = cw.toByteArray();

		// when something changes this is very useful for viewing the code and
		// the error

		Class<?> cl = loadClass(bytecode, plan.getProxyClassName(), plan.getTargetClassLoader());

		// inject the method fields
		try {
			for (IMethodBuildPlan mplan : plan.getMethods()) {
				Field f = cl.getField(methodFields.get(mplan));
				f.set(null, mplan.getTargetMethod());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new ASMProxyClassInfo(cl);
	}

	/**
	 * Adds a method to the class based on the given MethodBuildPlan.
	 *
	 * @param proxyType the type of the proxy class to which the method is added
	 * @param plan the plan of the method to create
	 * @param fieldName the name of the field storing the method to which the generated method will delegate its calls
	 * @param cv the class writer used for creating the class
	 */
	private void createDelegationMethod(Type proxyType, IMethodBuildPlan plan,
			String fieldName, ClassVisitor cv) {

		Type methodType = Type.getType(java.lang.reflect.Method.class);
		Type objectType = Type.getType(Object.class);
		Type[] invokeMethodArgs = getTypes(Arrays.<Class<?>> asList(
				Object.class, Object[].class));
		Method invokeMethod = new Method("invoke", objectType, invokeMethodArgs);

		Method meth = new Method(plan.getMethodName(), Type.getType(plan
				.getReturnType()), getTypes(plan.getParameterTypes()));
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, meth, null, new Type[] {}, cv);

		// setup the catching of exceptions raised by Method.invoke
		Label tryBegin = new Label();
		Label tryEnd = new Label();
		Label catchBegin = new Label();
		mg.visitTryCatchBlock(tryBegin, tryEnd, catchBegin,
				Type.getInternalName(InvocationTargetException.class));

		mg.visitLabel(tryBegin);
		// load the method field on the stack
		mg.getStatic(proxyType, fieldName, methodType);

		// load the delegation instance
		mg.loadThis();
		mg.getField(proxyType, SUBJECT_FIELD, objectType);

		// load the arguments
		mg.loadArgArray();
		// call invoke
		mg.invokeVirtual(methodType, invokeMethod);
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
		mg.invokeVirtual(Type.getType(Throwable.class),
				Method.getMethod("Throwable getCause()"));
		// now the cause is on the stack, throw it
		mg.throwException();

		mg.visitMaxs(0, 0);
		mg.visitEnd();

	}

	/**
	 * Taken from the ASM FAQ.
	 * Loads the given bytecode as class using the given classloader.
	 * @param b the bytecode
	 * @param className the name of the class to load
	 * @param loader the laoder to use
	 * @return the loaded class
	 */
	private Class<?> loadClass(byte[] b, String className, ClassLoader loader) {
		// override classDefine (as it is protected) and define the class.
		Class<?> clazz = null;
		try {
			Class<?> cls = Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod(
					"defineClass", new Class[] { String.class, byte[].class,
							int.class, int.class });

			// protected method invocaton
			method.setAccessible(true);
			Object[] args = new Object[] { className, b, Integer.valueOf(0),
					Integer.valueOf(b.length) };
			clazz = (Class<?>) method.invoke(loader, args);

		} catch (Exception e) {
			//a) the class already exists (would be very strange as we checked this beforehand)
			//  golden rule: expect the unexpected
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
	 * @author Jonas Kunz
	 */
	private static class ASMProxyClassInfo implements IProxyClassInfo {

		/**
		 * The proxy class.
		 */
		private Class<?> proxyClass;
		/**
		 * The constructor to use for creating a proxy.
		 * Takes the subject as first argument.
		 */
		private Constructor<?> constructor;


		/**
		 * creates a build plan.
		 * @param proxyClass the proxy class
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
	 * @param classes the classes whose names shall be retrieved
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
	 * @param classes the classes whose types shall be retrieved
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
