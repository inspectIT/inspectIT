package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;

import rocks.inspectit.agent.java.hooking.IHookDispatcher;

/**
 * Instrumenter for our special sensor dispatching. Currently able to replace the return value of
 * the method by the result received from the dispatch method.
 *
 * @author Ivan Senic
 *
 */
public class SpecialMethodInstrumenter extends AbstractMethodInstrumenter {

	/**
	 * Return type.
	 */
	private Type returnType;

	/**
	 * Default constructor. Defines method id that will be used during instrumentation.
	 *
	 * @param mv
	 *            Super method visitor.
	 * @param access
	 *            Method access code.
	 * @param name
	 *            Method name.
	 * @param desc
	 *            Method description.
	 * @param methodId
	 *            Method id that will be passed to {@link IHookDispatcher}.
	 */
	public SpecialMethodInstrumenter(MethodVisitor mv, int access, String name, String desc, long methodId) {
		super(mv, access, name, desc, methodId, false);
		this.returnType = Type.getReturnType(desc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void generateBeforeCatchCall() {
		// should never be called as we don't use the enhanced exception sensor in special method
		// instrumentation
		throw new UnsupportedOperationException("SpecialMethodInstrumenter can not generate before catch call.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodEnter() {
		// generate code for calling before body
		generateBeforeBodyCall();
		// and code for returning if result is not null
		generateReturnIfResultNotNull();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodExit(int opcode) {
		// we wont add any byte-code prior to athrow return
		// if exception is thrown we will skip the after body call
		if (opcode == ATHROW) {
			// exception return
			return;
		}

		// just ensure that result is duplicated on the stack
		// in case of void return or push null since we don't have result
		if (opcode == RETURN) {
			// standard return with no object (void)
			// we push null so that null is passed to our dispatching method
			pushNull();
		} else if (opcode == ARETURN) {
			// duplicate the original object
			dup();
		} else {
			// this is a primitive result return
			if ((opcode == LRETURN) || (opcode == DRETURN)) {
				// if we have either long or double return, we need to duplicate the last two stacks
				dup2();
			} else {
				// all other primitives just one duplicate
				dup();
			}
			// box
			box(returnType);
		}

		// generate code for calling after body
		generateAfterBodyCall();
		// and code for returning if result is not null
		generateReturnIfResultNotNull();
	}

	/**
	 * Generates before body call.
	 */
	private void generateBeforeBodyCall() {
		// load hook dispatcher
		loadHookDispatcher();

		// first push method id
		push(methodId);

		// then this object or null if's static
		if (isStatic) {
			pushNull();
		} else {
			loadThis();
		}

		// then parameters
		loadArgArray();

		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, "dispatchSpecialMethodBeforeBody",
				IInstrumenterConstant.DISPATCH_SPECIAL_METHOD_BEFORE_BODY_DESCRIPTOR, true);
	}

	/**
	 * Generates code for the after body call. This method expects the result of the method call on
	 * the stack that can be consumed.
	 */
	private void generateAfterBodyCall() {
		// prepare for calls
		// we expect result on stack so we must swap as result is last argument in the call
		loadHookDispatcher();
		swap();

		// first push method id
		push(methodId);
		// can not just swap because method id is long, thus a bit of gymnastic
		// r-l-l2
		dup2X1();
		// l-l2-r-l-l2
		pop2();
		// l-l2-r :)

		// then this object or null if's static
		if (isStatic) {
			pushNull();
		} else {
			loadThis();
		}
		swap();

		// then parameters
		loadArgArray();
		swap();

		// execute after body
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, "dispatchSpecialMethodAfterBody",
				IInstrumenterConstant.DISPATCH_SPECIAL_METHOD_AFTER_BODY_DESCRIPTOR, true);
	}

	/**
	 * Generates call for returning result on stack if needed.
	 */
	private void generateReturnIfResultNotNull() {
		// create new local and store result
		int local = newLocal(IInstrumenterConstant.OBJECT_TYPE);
		storeLocal(local);

		Label continueExecution = new Label();

		// then load for null check
		loadLocal(local);
		ifNull(continueExecution);

		// we can only return result if return type is not Void
		if (!Type.VOID_TYPE.equals(returnType)) {
			// then load for instance of check if it's an object
			loadLocal(local);
			instanceOfSafe(returnType);
			ifZCmp(EQ, continueExecution);

			// load again for return
			loadLocal(local);
			// we need to unbox here if needed as we are changing the result
			unbox(returnType);
			returnValue();
		}

		visitLabel(continueExecution);
	}

	/**
	 * Based on the given type performs the safe instance of. This means that if given type is
	 * primitive, instance of check will be done with corresponding primitive wrapper.
	 * <p>
	 * This method expects object on stack. If method type is passed as the argument to this
	 * function the object on the stack will be removed and <code>false</code> will be pushed
	 * (imitating the failed instanceOf check).
	 *
	 * @param type
	 *            Type
	 */
	private void instanceOfSafe(Type type) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
			instanceOf(IInstrumenterConstant.BOOLEAN_WRAPPER_TYPE);
			break;
		case Type.CHAR:
			instanceOf(IInstrumenterConstant.CHAR_WRAPPER_TYPE);
			break;
		case Type.BYTE:
			instanceOf(IInstrumenterConstant.CHAR_WRAPPER_TYPE);
			break;
		case Type.SHORT:
			instanceOf(IInstrumenterConstant.CHAR_WRAPPER_TYPE);
			break;
		case Type.INT:
			instanceOf(IInstrumenterConstant.INT_WRAPPER_TYPE);
			break;
		case Type.FLOAT:
			instanceOf(IInstrumenterConstant.FLOAT_WRAPPER_TYPE);
			break;
		case Type.LONG:
			instanceOf(IInstrumenterConstant.LONG_WRAPPER_TYPE);
			break;
		case Type.DOUBLE:
			instanceOf(IInstrumenterConstant.DOUBLE_WRAPPER_TYPE);
			break;
		case Type.OBJECT:
		case Type.ARRAY:
			instanceOf(type);
			break;
		default:
			// safety if somebody passes a method type
			pop();
			push(false);
		}
	}

}
