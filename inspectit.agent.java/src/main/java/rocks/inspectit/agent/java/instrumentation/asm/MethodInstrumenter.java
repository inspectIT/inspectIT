package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;

import rocks.inspectit.agent.java.hooking.IHookDispatcher;

/**
 * Used to instrument methods that are not constructors.
 *
 * @author Ivan Senic
 *
 */
public class MethodInstrumenter extends AbstractMethodInstrumenter {

	/**
	 * Default constructor. Defines method id that will be used during instrumentation and if
	 * enhanced exception sensor is active or not.
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
	 * @param enhancedExceptionSensor
	 *            Marker declaring if enhanced exception sensor is active.
	 * @see AbstractMethodInstrumenter#AbstractMethodInstrumenter(MethodVisitor, int, String,
	 *      String, long, boolean)
	 */
	public MethodInstrumenter(MethodVisitor mv, int access, String name, String desc, long methodId, boolean enhancedExceptionSensor) {
		super(mv, access, name, desc, methodId, enhancedExceptionSensor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodEnter() {
		generateBeforeBodyCall();

		// start our try block
		visitLabel(tryBlockStart);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodExit(int opcode) {
		// we wont add any byte-code prior to athrow return
		// if exception is thrown we will execute calls in the finally block we are adding
		if (opcode == ATHROW) {
			// exception return
			return;
		}

		// just ensure that result is duplicated on the stack
		// since we are calling two methods, make 2 copies of result on stack
		// in case of void return or push null since we don't have result
		if (opcode == RETURN) {
			// standard return with no object (void)
			pushNull();
			pushNull();
		} else if (opcode == ARETURN) {
			// duplicate the original object
			dup();
			dup();
		} else {
			if ((opcode == LRETURN) || (opcode == DRETURN)) {
				// if we have either long or double return, we need to duplicate the last two stacks
				dup2();
			} else {
				dup();
			}
			// box and then duplicate the object then
			box(Type.getReturnType(this.methodDesc));
			dup();
		}

		// add two false booleans to denote no exception in the call
		// generate code for calling first and second
		generateAfterBodyCall("dispatchFirstMethodAfterBody", false);
		generateAfterBodyCall("dispatchSecondMethodAfterBody", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// the definition of the end of the try block
		Label tryBlockEnd = new Label();
		visitLabel(tryBlockEnd);

		// only add catch block if exception sensor is active
		if (enhancedExceptionSensor) {
			super.visitTryCatchBlock(tryBlockStart, tryBlockEnd, catchHandler, IInstrumenterConstant.THROWABLE_INTERNAL_NAME);
			visitLabel(catchHandler);

			// duplicate exception and call
			dup();
			generateThrowInBodyCall();
		}

		// setup for the finally block
		super.visitTryCatchBlock(tryBlockStart, tryBlockEnd, finallyHandler, null);
		visitLabel(finallyHandler);

		// generate code for calling first and second
		// push exception as we don't have a result
		dup();
		dup();
		// add true booleans to denote exception in the call
		generateAfterBodyCall("dispatchFirstMethodAfterBody", true);
		generateAfterBodyCall("dispatchSecondMethodAfterBody", true);

		mv.visitInsn(ATHROW);

		// update the max stack stuff
		super.visitMaxs(maxStack, maxLocals);
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

		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, "dispatchMethodBeforeBody", IInstrumenterConstant.DISPATCH_METHOD_BEFORE_BODY_DESCRIPTOR,
				true);
	}

	/**
	 * Generates code for the after body call. This method expects the result of the method call on
	 * the stack that can be consumed.
	 *
	 * @param method
	 *            method to be called can be only
	 *            {@link IHookDispatcher#dispatchFirstMethodAfterBody(long, Object, Object[], Object, boolean)}
	 *            or
	 *            {@link IHookDispatcher#dispatchSecondMethodAfterBody(long, Object, Object[], Object, boolean)}
	 * @param exception
	 *            Value of the exception argument pass to the dispatcher.
	 */
	private void generateAfterBodyCall(String method, boolean exception) {
		// prepare first everything up to exception boolean
		prepareAfterBodyCall();

		// then push on stack info about exception
		push(exception);

		// execute after body
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, method, IInstrumenterConstant.DISPATCH_METHOD_AFTER_BODY_DESCRIPTOR, true);
	}

	/**
	 * Generates call for
	 * {@link IHookDispatcher#dispatchOnThrowInBody(long, Object, Object[], Object)}. This method
	 * expects exception object on stack that can be consumed.
	 */
	private void generateThrowInBodyCall() {
		// we can use same code for the after body call since method signature is same (without
		// exception)
		prepareAfterBodyCall();

		// execute dispatchOnThrowInBody
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, "dispatchOnThrowInBody", IInstrumenterConstant.DISPATCH_ON_THROW_BODY_DESCRIPTOR, true);
	}

	/**
	 * Prepares the afterBody or throwInBody calls by loading dispatcher, methodId, object and
	 * parameters to the stack. This method excepts result on stack (result will remain first on
	 * stack).
	 */
	private void prepareAfterBodyCall() {
		// prepare for calls
		// we expect following stack: result (short r)
		loadHookDispatcher();
		// r-d
		swap();
		// d-r

		// push method id
		push(methodId);
		// can not just swap because method id is long, thus a bit of gymnastic
		// d-r-l-l
		dup2X1();
		// d-l-l-r-l-l2
		pop2();
		// d-l-l-r :)

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
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Generates call for {@link IHookDispatcher#dispatchBeforeCatch(long, Object)}. This method
	 * expects exception object on stack that can be consumed.
	 */
	@Override
	protected void generateBeforeCatchCall() {
		// prepare for calls
		// we expect exception on stack so we must swap as exception is last argument in the call
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

		// execute before catch
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IHOOK_DISPATCHER_INTERNAL_NAME, "dispatchBeforeCatch", IInstrumenterConstant.DISPATCH_BEFORE_CATCH_DESCRIPTOR, true);
	}

}
