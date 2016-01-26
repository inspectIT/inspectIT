package info.novatec.inspectit.instrumentation.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Ivan Senic
 *
 */
public class ClassLoaderDelegationMethodInstrumenter extends AdviceAdapter {

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
	private static final String IAGENT_LOAD_CLASS_METHOD_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(Object[].class));

	/**
	 * Method descriptor of the load class method in the class loader:
	 * {@link java.lang.ClassLoader#loadClass(String)}.
	 */
	private static final String CLASS_LOADER_LOAD_CLASS_METHOD_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(String.class));;

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
	 * Returns if the method name and descriptor are fitting the
	 * {@link java.lang.ClassLoader#loadClass(String)} method. This method should be used to
	 * determine if the method should be instrumented with class loading delegation.
	 *
	 * @param name
	 *            name of the method
	 * @param desc
	 *            method type descriptor
	 * @return <code>true</code> if method is fitting for the class loader delegation.
	 */
	public static boolean isLoadClassMethod(String name, String desc) {
		return "loadClass".equals(name) && CLASS_LOADER_LOAD_CLASS_METHOD_DESCRIPTOR.equals(desc);
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

		dup();

		Label label = new Label();
		ifNull(label);

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
