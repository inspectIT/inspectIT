package rocks.inspectit.shared.all.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Type;

/**
 * Constants that are often used in the instrumenters like type and method descriptions.
 *
 * @author Ivan Senic
 *
 */
public interface IInstrumenterConstant {

	/**
	 * Class name where our IAgent exists as a field.
	 */
	String AGENT_INTERNAL_NAME = "rocks/inspectit/agent/java/Agent";

	/**
	 * Internal name of our IAgent.
	 */
	String IAGENT_INTERNAL_NAME = "rocks/inspectit/agent/java/IAgent";

	/**
	 * Internal name of our IHookDispatcher.
	 */
	String IHOOK_DISPATCHER_INTERNAL_NAME = "rocks/inspectit/agent/java/hooking/IHookDispatcher";

	/**
	 * {@link Throwable} internal name.
	 */
	String THROWABLE_INTERNAL_NAME = Type.getInternalName(Throwable.class);

	/**
	 * Descriptor of our IAgent.
	 */
	String IAGENT_DESCRIPTOR = "L" + IAGENT_INTERNAL_NAME + ";";

	/**
	 * Internal name of get IHookDispatcher method.
	 */
	String GET_IHOOK_DISPATCHER_DESCRIPTOR = "()L" + IHOOK_DISPATCHER_INTERNAL_NAME + ";";

	/**
	 * Method descriptor of the load class method in the IAgent class.
	 */
	String IAGENT_LOAD_CLASS_METHOD_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(Object[].class));

	/**
	 * {@link IHookDispatcher#dispatchMethodBeforeBody(long, Object, Object[])} descriptor.
	 */
	String DISPATCH_METHOD_BEFORE_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class));

	/**
	 * {@link IHookDispatcher#dispatchFirstMethodAfterBody(long, Object, Object[], Object)} and
	 * {@link IHookDispatcher#dispatchSecondMethodAfterBody(long, Object, Object[], Object)}
	 * descriptor.
	 */
	String DISPATCH_METHOD_AFTER_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class), Type.getType(Object.class));

	/**
	 * {@link IHookDispatcher#dispatchBeforeCatch(long, Object)} descriptor.
	 */
	String DISPATCH_BEFORE_CATCH_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class));;

	/**
	 * {@link IHookDispatcher#dispatchConstructorBeforeBody(long, Object[])} descriptor.
	 */
	String DISPATCH_CONSTRUCTOR_BEFORE_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object[].class));

	/**
	 * {@link IHookDispatcher#dispatchConstructorAfterBody(long, Object, Object[])} descriptor.
	 */
	String DISPATCH_CONSTRUCTOR_AFTER_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class));

	/**
	 * {@link IHookDispatcher#dispatchConstructorBeforeCatch(long, Object)} descriptor.
	 */
	String DISPATCH_CONSTRUCTOR_BEFORE_CATCH_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class));

	/**
	 * {@link IHookDispatcher#dispatchConstructorOnThrowInBody(long, Object, Object[], Object)}
	 * descriptor.
	 */
	String DISPATCH_CONSTRUCTOR_ON_THROW_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class),
			Type.getType(Object.class));

}
