package rocks.inspectit.agent.java.hooking.impl;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.IIdManager;
import rocks.inspectit.agent.java.hooking.IHookDispatcherMapper;
import rocks.inspectit.agent.java.hooking.IHookInstrumenter;
import rocks.inspectit.shared.all.spring.logger.Log;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The byte code instrumenter class. Used to instrument the additional instructions into the target
 * byte code.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
@Component
public class HookInstrumenter implements IHookInstrumenter {

	public static final String DISPATCH_FIRST_METHOD_AFTER_BODY = ".dispatchFirstMethodAfterBody(";
	public static final String DISPATCH_SECOND_METHOD_AFTER_BODY = ".dispatchSecondMethodAfterBody(";
	public static final String THROW_E = "throw $e; ";
	public static final String L_ARGS = "l, $0, $args);";
	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The hook dispatcher. This string shouldn't be touched. For changing the dispatcher, alter the
	 * hook dispatcher instance in the Agent class.
	 */
	private static String hookDispatcherTarget = "rocks.inspectit.agent.java.Agent#agent.getHookDispatcher()";

	/**
	 * The agent target as string.
	 */
	private static String agentTarget = "rocks.inspectit.agent.java.Agent#agent";

	/**
	 * The hook dispatching service.
	 */
	private final IHookDispatcherMapper hookDispatcher;

	/**
	 * The ID manager used to register the methods and the mapping between the method sensor type id
	 * and the method id.
	 */
	private final IIdManager idManager;

	/**
	 * The expression editor to modify a method body.
	 */
	private MethodExprEditor methodExprEditor = new MethodExprEditor();

	/**
	 * The expression editor to modify a constructor body.
	 */
	private ConstructorExprEditor constructorExprEditor = new ConstructorExprEditor();

	/**
	 * The implementation of the configuration storage where all definitions of the user are stored.
	 */
	private IConfigurationStorage configurationStorage;

	/**
	 * The default and only constructor for this class.
	 * 
	 * @param hookDispatcher
	 *            The hook dispatcher which is used in the Agent.
	 * @param idManager
	 *            The ID manager.
	 * @param configurationStorage
	 *            The configuration storage where all definitions of the user are stored.
	 */
	@Autowired
	public HookInstrumenter(IHookDispatcherMapper hookDispatcher, IIdManager idManager, IConfigurationStorage configurationStorage) {
		this.hookDispatcher = hookDispatcher;
		this.idManager = idManager;
		this.configurationStorage = configurationStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodHook(CtMethod method, RegisteredSensorConfig rsc) throws HookException {
		if (log.isDebugEnabled()) {
			log.debug("Match found! Class: " + rsc.getTargetClassName() + " Method: " + rsc.getTargetMethodName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
		}

		if (method.getDeclaringClass().isFrozen()) {
			// defrost before we are adding any instructions
			method.getDeclaringClass().defrost();
		}

		final long methodId = idManager.registerMethod(rsc);

		for (MethodSensorTypeConfig config : rsc.getSensorTypeConfigs()) {
			long sensorTypeId = config.getId();
			idManager.addSensorTypeToMethod(sensorTypeId, methodId);
		}

		try {
			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean exceptionSensorEnhanced = configurationStorage.isEnhancedExceptionSensorActivated();
			// instrument as finally if exception sensor is deactivated or activated in simple mode
			boolean asFinally = !(exceptionSensorActivated && exceptionSensorEnhanced);

			if (Modifier.isStatic(method.getModifiers())) {
				// static method
				method.insertBefore(hookDispatcherTarget + ".dispatchMethodBeforeBody(" + methodId + "l, null, $args);");
				method.insertAfter(hookDispatcherTarget + DISPATCH_FIRST_METHOD_AFTER_BODY + methodId + "l, null, $args, ($w)$_);", asFinally);
				method.insertAfter(hookDispatcherTarget + DISPATCH_SECOND_METHOD_AFTER_BODY + methodId + "l, null, $args, ($w)$_);", asFinally);

				if (!asFinally) {
					// the exception sensor is activated, so instrument the
					// static method with an addCatch
					instrumentMethodWithTryCatch(method, methodId, true);
				}
			} else {
				// normal method
				method.insertBefore(hookDispatcherTarget + ".dispatchMethodBeforeBody(" + methodId + L_ARGS);
				method.insertAfter(hookDispatcherTarget + DISPATCH_FIRST_METHOD_AFTER_BODY + methodId + "l, $0, $args, ($w)$_);", asFinally);
				method.insertAfter(hookDispatcherTarget + DISPATCH_SECOND_METHOD_AFTER_BODY + methodId + "l, $0, $args, ($w)$_);", asFinally);

				if (!asFinally) {
					// the exception sensor is activated, so instrument the
					// normal method with an addCatch
					instrumentMethodWithTryCatch(method, methodId, false);
				}
			}

			// Add the information to the dispatching service
			hookDispatcher.addMethodMapping(methodId, rsc);
		} catch (CannotCompileException cannotCompileException) {
			throw new HookException("Could not insert the bytecode into the method/class", cannotCompileException);
		} catch (NotFoundException notFoundException) {
			// for the addCatch method needed
			throw new HookException("Could not insert the bytecode into the method/class", notFoundException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addConstructorHook(CtConstructor constructor, RegisteredSensorConfig rsc) throws HookException {
		if (log.isDebugEnabled()) {
			log.debug("Constructor match found! Class: " + rsc.getTargetClassName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
		}

		if (constructor.getDeclaringClass().isFrozen()) {
			// defrost before we are adding any instructions
			constructor.getDeclaringClass().defrost();
		}

		long constructorId = idManager.registerMethod(rsc);

		for (MethodSensorTypeConfig config : rsc.getSensorTypeConfigs()) {
			long sensorTypeId = config.getId();
			idManager.addSensorTypeToMethod(sensorTypeId, constructorId);
		}

		try {
			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean exceptionSensorEnhanced = configurationStorage.isEnhancedExceptionSensorActivated();
			// instrument as finally if exception sensor is deactivated or activated in simple mode
			boolean asFinally = !(exceptionSensorActivated && exceptionSensorEnhanced);

			constructor.insertBefore(hookDispatcherTarget + ".dispatchConstructorBeforeBody(" + constructorId + "l, $args);");
			constructor.insertAfter(hookDispatcherTarget + ".dispatchConstructorAfterBody(" + constructorId + L_ARGS, asFinally);

			if (!asFinally) {
				instrumentConstructorWithTryCatch(constructor, constructorId);
			}

			// Add the information to the dispatching service
			hookDispatcher.addConstructorMapping(constructorId, rsc);
		} catch (CannotCompileException cannotCompileException) {
			throw new HookException("Could not insert the bytecode into the constructor/class", cannotCompileException);
		} catch (NotFoundException notFoundException) {
			throw new HookException("Could not insert the bytecode into the constructor/class", notFoundException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addClassLoaderDelegationHook(CtMethod ctMethod) throws HookException {
		if (log.isDebugEnabled()) {
			log.debug("Class loader delegation match found! Method signature: " + ctMethod.getSignature());
		}

		if (ctMethod.getDeclaringClass().isFrozen()) {
			// defrost before we are adding any instructions
			ctMethod.getDeclaringClass().defrost();
		}

		try {
			ctMethod.insertBefore("Class c = " + agentTarget + ".loadClass($args); if (null != c) { return c; }");
		} catch (CannotCompileException cannotCompileException) {
			throw new HookException("Could not insert the bytecode into the method/class for class loader delegation", cannotCompileException);
		}
	}

	/**
	 * The passed {@link CtMethod} is instrumented with an internal <code>try-catch</code> block to
	 * get an event when an exception is thrown in a method body.
	 * 
	 * @see rocks.inspectit.shared.all.javassist.CtMethod#addCatch(String, CtClass)
	 * 
	 * @param method
	 *            The {@link CtMethod} where additional instructions are added.
	 * @param methodId
	 *            The method id of the passed method.
	 * @param isStatic
	 *            Defines whether the current method is a static method.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default {@link ClassPool}.
	 */
	private void instrumentMethodWithTryCatch(CtMethod method, long methodId, boolean isStatic) throws CannotCompileException, NotFoundException {
		if (configurationStorage.isExceptionSensorActivated()) {
			// we instrument the method with an expression editor to get events
			// when a handler of an exception is found.
			methodExprEditor.setId(methodId);
			method.instrument(methodExprEditor);
		}

		CtClass type = ClassPool.getDefault().get("java.lang.Throwable");

		if (!isStatic) {
			// normal method
			method.addCatch(hookDispatcherTarget + ".dispatchOnThrowInBody(" + methodId + "l, $0, $args, $e);" + hookDispatcherTarget + DISPATCH_FIRST_METHOD_AFTER_BODY + methodId
					+ "l, $0, $args, null);" + hookDispatcherTarget + DISPATCH_SECOND_METHOD_AFTER_BODY + methodId + "l, $0, $args, null);" + THROW_E, type);
		} else {
			// static method
			method.addCatch(hookDispatcherTarget + ".dispatchOnThrowInBody(" + methodId + "l, null, $args, $e);" + hookDispatcherTarget + DISPATCH_FIRST_METHOD_AFTER_BODY + methodId
					+ "l, null, $args, null);" + hookDispatcherTarget + DISPATCH_SECOND_METHOD_AFTER_BODY + methodId + "l, null, $args, null);" + THROW_E, type);
		}
	}

	/**
	 * The passed {@link CtConstructor} is instrumented with an internal <code>try-catch</code>
	 * block to get an event when an exception is thrown in a constructor body.
	 * 
	 * @see rocks.inspectit.shared.all.javassist.CtConstructor#addCatch(String, CtClass)
	 * 
	 * @param constructor
	 *            The {@link CtConstructor} where additional instructions are added.
	 * @param constructorId
	 *            The constructor id of the passed constructor.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default {@link ClassPool}.
	 */
	private void instrumentConstructorWithTryCatch(CtConstructor constructor, long constructorId) throws CannotCompileException, NotFoundException {
		if (configurationStorage.isExceptionSensorActivated()) {
			// we instrument the constructor with an expression editor to get
			// events when a handler of an exception is found.
			constructorExprEditor.setId(constructorId);
			constructor.instrument(constructorExprEditor);
		}

		CtClass type = ClassPool.getDefault().get("java.lang.Throwable");
		constructor.addCatch(hookDispatcherTarget + ".dispatchConstructorOnThrowInBody(" + constructorId + "l, $0, $args, $e);" + hookDispatcherTarget + ".dispatchConstructorAfterBody("
				+ constructorId + L_ARGS + THROW_E, type);
	}

	/**
	 * If <code>instrument()</code> is called in <code>CtMethod</code>, the method body is scanned
	 * from the beginning to the end. Whenever an expression, such as a Handler of an Exception is
	 * found, <code>edit()</code> is called in <code>ExprEditor</code>. <code>edit()</code> can
	 * inspect and modify the given expression. The modification is reflected on the original method
	 * body. If <code>edit()</code> does nothing, the original method body is not changed.
	 * 
	 * @see rocks.inspectit.shared.all.javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public static class MethodExprEditor extends ExprEditor {

		/**
		 * The id of the method which is instrumented with the ExpressionEditor.
		 */
		private long id = 0;

		/**
		 * Sets the method id.
		 * 
		 * @param id
		 *            The id of the method to instrument.
		 */
		public void setId(long id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void edit(Handler handler) throws CannotCompileException {
			if (!handler.isFinally()) {
				// $1 is the exception object
				handler.insertBefore(hookDispatcherTarget + ".dispatchBeforeCatch(" + id + "l, $1);");
			}
		}
	}

	/**
	 * If <code>instrument()</code> is called in <code>CtConstructor</code>, the constructor body is
	 * scanned from the beginning to the end. Whenever an expression, such as a Handler of an
	 * Exception is found, <code>edit()</code> is called in <code>ExprEditor</code>.
	 * <code>edit()</code> can inspect and modify the given expression. The modification is
	 * reflected on the original constructor body. If <code>edit()</code> does nothing, the original
	 * constructor body is not changed.
	 * 
	 * @see rocks.inspectit.shared.all.javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public static class ConstructorExprEditor extends ExprEditor {

		/**
		 * The id of the constructor which is instrumented with the ExpressionEditor.
		 */
		private long id = 0;

		/**
		 * Sets the constructor id.
		 * 
		 * @param id
		 *            The id of the constructor to instrument.
		 */
		public void setId(long id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void edit(Handler handler) throws CannotCompileException {
			if (!handler.isFinally()) {
				// $1 is the exception object
				handler.insertBefore(hookDispatcherTarget + ".dispatchConstructorBeforeCatch(" + id + "l, $1);");
			}
		}
	}

}
