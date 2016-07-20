package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instrumenter that adds byte code for intercepting the mbean server factory.
 *
 * @author Ivan Senic
 *
 */
public abstract class MBeanServerFactoryInstrumenter extends AdviceAdapter {

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
	public MBeanServerFactoryInstrumenter(MethodVisitor mv, int access, String name, String desc) {
		super(Opcodes.ASM5, mv, access, name, desc);
	}

	/**
	 * Generates call to the correct agent method depending on the add or remove implementation.
	 */
	protected abstract void generateAgentCall();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodEnter() {
		// load agent
		loadAgent();

		// then push first parameter
		loadArg(0);

		// generate call
		generateAgentCall();
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

	/**
	 * Sub-class that generates add mbean server call.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class Add extends MBeanServerFactoryInstrumenter {

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
		public Add(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void generateAgentCall() {
			// now invoke loadClass(Object[] params) method (no parameters here)
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IAGENT_INTERNAL_NAME, "mbeanServerAdded", IInstrumenterConstant.IAGENT_MBEAN_SERVER_ADD_REMOVE_DESCRIPTOR, true);
		}

	}

	/**
	 * Sub-class that generates remove mbean server call.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class Remove extends MBeanServerFactoryInstrumenter {

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
		public Remove(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void generateAgentCall() {
			// now invoke loadClass(Object[] params) method (no parameters here)
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IInstrumenterConstant.IAGENT_INTERNAL_NAME, "mbeanServerRemoved", IInstrumenterConstant.IAGENT_MBEAN_SERVER_ADD_REMOVE_DESCRIPTOR, true);
		}

	}
}
