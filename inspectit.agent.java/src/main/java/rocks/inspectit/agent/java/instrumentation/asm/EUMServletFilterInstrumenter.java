package rocks.inspectit.agent.java.instrumentation.asm;


import info.novatec.inspectit.org.objectweb.asm.Label;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;
import info.novatec.inspectit.org.objectweb.asm.commons.AdviceAdapter;
import info.novatec.inspectit.org.objectweb.asm.commons.Method;

/**
 * Instrumenter that adds byte code for the interception of EUM requests and for injecting scripts.
 * Redirects the control flow to the {@link rocks.inspectit.agent.java.eum.IServletInstrumenter}
 *
 * @author Jonas Kunz
 *
 */
public class EUMServletFilterInstrumenter extends AdviceAdapter {

	/**
	 * Class name where our IAgent exists as a field.
	 */
	private static final Type AGENT_TYPE = Type.getType("Lrocks/inspectit/agent/java/IAgent;");

	/**
	 * The object type.
	 */
	private static final Type OBJECT_TYPE = Type.getType(Object.class);

	/**
	 * The owner of the servlet instrumenter.
	 */
	private static final Type SERVLET_INSTRUMENTER_OWNER = Type.getType("Lrocks/inspectit/agent/java/Agent;");

	/**
	 * Class name where our IAgent exists as a field.
	 */
	private static final Type SERVLET_INSTRUMENTER_TYPE = Type.getType("Lrocks/inspectit/agent/java/eum/IServletInstrumenter;");

	/**
	 * local variable for storing our started try block.
	 */
	private final Label tryBlockStart;

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
	public EUMServletFilterInstrumenter(MethodVisitor mv, int access, String name, String desc) {
		super(Opcodes.ASM5, mv, access, name, desc);
		tryBlockStart = new Label();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMethodEnter() {
		Type servletResponseType = Type.getType("Ljavax/servlet/ServletResponse;");

		Method interceptRequestMethod = new Method("interceptRequest", Type.BOOLEAN_TYPE, new Type[] { OBJECT_TYPE, OBJECT_TYPE, OBJECT_TYPE });
		Method instrumentResponseMethod = new Method("instrumentResponse", OBJECT_TYPE, new Type[] { OBJECT_TYPE, OBJECT_TYPE, OBJECT_TYPE });

		loadServletInstrumenter();

		dup(); // duplicate ServletIsntrumenter for further invocations

		loadThis();
		loadArg(0);
		loadArg(1);
		invokeInterface(SERVLET_INSTRUMENTER_TYPE, interceptRequestMethod);

		// return if the request was intercepted
		Label label = new Label();
		ifZCmp(EQ, label);

		pop(); // pop servletInstrumenter
		returnValue(); // return

		visitLabel(label);

		loadThis();
		loadArg(0);
		loadArg(1);
		invokeInterface(SERVLET_INSTRUMENTER_TYPE, instrumentResponseMethod);
		checkCast(servletResponseType);
		storeArg(1); // store the result as the new response object

		visitLabel(tryBlockStart);

	}

	/**
	 *
	 */
	private void loadServletInstrumenter() {
		getStatic(SERVLET_INSTRUMENTER_OWNER, "agent", AGENT_TYPE);
		invokeInterface(AGENT_TYPE, new Method("getServletInstrumenter", SERVLET_INSTRUMENTER_TYPE, new Type[] {}));
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
		} else {
			servletOrFilterExit();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// the definition of the end of the try block
		Label tryBlockEnd = new Label();
		visitLabel(tryBlockEnd);

		Label finallyHandler = new Label();
		// setup for the finally block
		super.visitTryCatchBlock(tryBlockStart, tryBlockEnd, finallyHandler, null);
		visitLabel(finallyHandler);

		// generate code for calling first and second
		// push nulls as we don't have a result
		servletOrFilterExit();

		mv.visitInsn(ATHROW);

		// update the max stack stuff
		super.visitMaxs(maxStack, maxLocals);
	}

	/**
	 * notifies the servlet instrumenter thath the servlet or filter has finished.
	 */
	private void servletOrFilterExit() {
		Method servletOrFilterExitMethod = new Method("servletOrFilterExit", Type.VOID_TYPE, new Type[] { OBJECT_TYPE });

		loadServletInstrumenter();
		loadThis();
		invokeInterface(SERVLET_INSTRUMENTER_TYPE, servletOrFilterExitMethod);

	}

}