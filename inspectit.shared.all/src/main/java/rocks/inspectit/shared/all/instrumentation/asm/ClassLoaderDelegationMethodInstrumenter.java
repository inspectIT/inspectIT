package rocks.inspectit.shared.all.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;
import info.novatec.inspectit.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instrumenter that adds byte code for the class loading delegation.
 *
 * @author Ivan Senic
 *
 */
public class ClassLoaderDelegationMethodInstrumenter extends AdviceAdapter {

	/**
	 * Class type.
	 */
	private static final Type CLASS_TYPE = Type.getType(Class.class);

	/**
	 * Default constructor.
	 *
	 * @param mv
	 *            Super method visitor.
	 * @param access
	 *            Method access code.
	 * @param name
	 *            Method name.
	 * @param desc
	 *            Method description.
	 * @see AbstractMethodInstrumenter#AbstractMethodInstrumenter(MethodVisitor, int, String,
	 *      String, long, boolean)
	 */
	public ClassLoaderDelegationMethodInstrumenter(MethodVisitor mv, int access, String name, String desc) {
		super(Opcodes.ASM5, mv, access, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodEnter() {
		loadAgent();

		// then push parameters
		loadArgArray();

		// now invoke loadClass(Object[] params) method (no parameters here)
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IAGENT_INTERNAL_NAME, "loadClass", IInstrumenterConstant.IAGENT_LOAD_CLASS_METHOD_DESCRIPTOR, true);

		// create new local and store result
		int local = newLocal(CLASS_TYPE);
		storeLocal(local);

		// then load for null check
		loadLocal(local);

		Label label = new Label();
		ifNull(label);

		// load again for return
		loadLocal(local);
		returnValue();

		visitLabel(label);
	}

	/**
	 * Loads agent on the stack so that methods can be executed on it.
	 * <p>
	 * Protected access so we can change in tests.
	 */
	protected void loadAgent() {
		// load first the Agent.agent static field
		mv.visitFieldInsn(Opcodes.GETSTATIC, IInstrumenterConstant.AGENT_INTERNAL_NAME, "agent", IInstrumenterConstant.IAGENT_DESCRIPTOR);
	}

}
