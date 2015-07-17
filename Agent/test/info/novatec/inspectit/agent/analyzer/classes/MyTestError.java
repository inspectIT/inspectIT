package info.novatec.inspectit.agent.analyzer.classes;

@SuppressWarnings("PMD")
public class MyTestError extends Error {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5830128876783922090L;

	MyTestError() {
		super();
	}

	MyTestError(String message) {
		super(message);
	}

	MyTestError(Throwable cause) {
		super(cause);
	}

}
