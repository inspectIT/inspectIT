package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Type;

import rocks.inspectit.agent.java.hooking.IHookDispatcher;

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

	/**
	 * {@link IHookDispatcher#dispatchSpecialMethodBeforeBody(long, Object, Object[])} descriptor.
	 */
	String DISPATCH_SPECIAL_METHOD_BEFORE_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Object.class), Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class));

	/**
	 * {@link IHookDispatcher#dispatchSpecialMethodAfterBody(long, Object, Object[], Object)}
	 * descriptor.
	 */
	String DISPATCH_SPECIAL_METHOD_AFTER_BODY_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Object.class), Type.LONG_TYPE, Type.getType(Object.class), Type.getType(Object[].class),
			Type.getType(Object.class));

	// primitive wrappers cached
	Type BOOLEAN_WRAPPER_TYPE = Type.getType(Boolean.class); // NOCHK
	Type CHAR_WRAPPER_TYPE = Type.getType(Character.class); // NOCHK
	Type BYTE_WRAPPER_TYPE = Type.getType(Byte.class); // NOCHK
	Type SHORT_WRAPPER_TYPE = Type.getType(Short.class); // NOCHK
	Type INT_WRAPPER_TYPE = Type.getType(Integer.class); // NOCHK
	Type FLOAT_WRAPPER_TYPE = Type.getType(Float.class); // NOCHK
	Type LONG_WRAPPER_TYPE = Type.getType(Long.class); // NOCHK
	Type DOUBLE_WRAPPER_TYPE = Type.getType(Double.class); // NOCHK

	/**
	 * Object type cached.
	 */
	Type OBJECT_TYPE = Type.getType(Object.class);

	/**
	 * Object[] type cached.
	 */
	Type OBJECT_ARRAY_TYPE = Type.getType(Object.class);

}
