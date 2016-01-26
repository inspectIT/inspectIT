package rocks.inspectit.agent.java.instrumentation.asm;

/**
 * Dummy exception to throw in
 * {@link rocks.inspectit.agent.java.instrumentation.asm.InstrumentationExceptionTestClass} during
 * the instrumentation tests.
 * 
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MyTestException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5733336673451228795L;

	public MyTestException() {
		super();
	}

	public MyTestException(String message) {
		super(message);
	}

	public MyTestException(Throwable cause) {
		super(cause);
	}

}
