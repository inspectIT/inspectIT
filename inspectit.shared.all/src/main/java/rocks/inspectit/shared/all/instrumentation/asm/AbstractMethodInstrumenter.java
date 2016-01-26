package rocks.inspectit.shared.all.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for both method instrumenter and constructor instrumenter.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractMethodInstrumenter extends AdviceAdapter {

	/**
	 * If method is static or not.
	 */
	protected boolean isStatic;

	/**
	 * Id of the method. This id will be passed to the dispatcher.
	 */
	protected long methodId;

	/**
	 * Marker declaring if enhanced exception sensor is active.
	 */
	protected boolean enhancedExceptionSensor;

	/**
	 * The label for the start of the try/finally or try/catch/finally block that we are adding.
	 */
	protected Label tryBlockStart = new Label();

	/**
	 * The label for the start of the catch block in try/catch/finally.
	 */
	protected Label catchHandler = new Label();

	/**
	 * The label for the start of the finally block in try/finally or try/catch/finally.
	 */
	protected Label finallyHandler = new Label();

	/**
	 * Set of labels that denote the start of catch blocks in the method that are not ours.
	 */
	private final Set<Label> handlers = new HashSet<Label>(1);

	/**
	 * Default constructor for the method instrumenter. Defines method id that will be used during
	 * instrumentation and if enhanced exception sensor is active or not.
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
	 */
	protected AbstractMethodInstrumenter(MethodVisitor mv, int access, String name, String desc, long methodId, boolean enhancedExceptionSensor) {
		super(Opcodes.ASM5, mv, access, name, desc);
		this.methodId = methodId;
		this.enhancedExceptionSensor = enhancedExceptionSensor;
		this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
	}

	/**
	 * Generates code for before catch call. Calling this method expects an exception on the stack
	 * that can be consumed.
	 */
	protected abstract void generateBeforeCatchCall();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);

		// if enhanced sensor is on we must add beforeCatch call to start of each catch block
		// thus we are saving labels that denote start of handler blocks
		// note we are skipping the finally blocks
		// and skipping one handler that denotes our catch block
		if (enhancedExceptionSensor && handler != catchHandler && null != type) { // NOPMD comp refs
			handlers.add(handler);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);

		// any label is saved to the handlers set we must add call to beforeCatch
		// note that these are catch handling blocks so exception is on stack
		if (handlers.contains(label)) {
			// duplicate the exception on the stack and call
			dup();
			generateBeforeCatchCall();
		}
	}

	/**
	 * Loads hook dispatcher on the stack so that methods can be executed on it.
	 * <p>
	 * Protected access so we can change in tests.
	 */
	protected void loadHookDispatcher() {
		// load first the Agent.agent static field
		mv.visitFieldInsn(Opcodes.GETSTATIC, IInstrumenterConstant.AGENT_INTERNAL_NAME, "agent", IInstrumenterConstant.IAGENT_DESCRIPTOR);

		// now invoke getHookDispatcher() method (no parameters here)
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IAGENT_INTERNAL_NAME, "getHookDispatcher", IInstrumenterConstant.GET_IHOOK_DISPATCHER_DESCRIPTOR, true);
	}

	/**
	 * Pushes null to stack.
	 */
	protected void pushNull() {
		mv.visitInsn(Opcodes.ACONST_NULL);
	}

}
