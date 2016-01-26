package info.novatec.inspectit.instrumentation.asm;

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
	 * Class name where our IAgent exists as a field.
	 */
	private static final String AGENT_INTERNAL_NAME = "info/novatec/inspectit/agent/Agent";

	/**
	 * Internal name of our IAgent.
	 */
	private static final String IAGENT_INTERNAL_NAME = "info/novatec/inspectit/agent/IAgent";

	/**
	 * Descriptor of our IAgent.
	 */
	private static final String IAGENT_DESCRIPTOR = "L" + IAGENT_INTERNAL_NAME + ";";

	/**
	 * Method descriptor of the load class method in the IAgent class.
	 */
	private static final String IAGENT_LOAD_CLASS_METHOD_DESCRIPTOR = Type.getMethodDescriptor(CLASS_TYPE, Type.getType(Object[].class));


	/**
	 * @param mv
	 *            Super method visitor.
	 * @param access
	 *            Method access code.
	 * @param name
	 *            Method name.
	 * @param desc
	 *            Method description.
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
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IAGENT_INTERNAL_NAME, "loadClass", IAGENT_LOAD_CLASS_METHOD_DESCRIPTOR, true);

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
		mv.visitFieldInsn(Opcodes.GETSTATIC, AGENT_INTERNAL_NAME, "agent", IAGENT_DESCRIPTOR);
	}

}
