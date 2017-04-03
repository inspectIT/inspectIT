package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Enumeration of the retransformation strategies that the agent can use to modify classes.
 *
 * @author Marius Oehler
 *
 */
public enum RetransformationStrategy {

	/**
	 * Class retransformation is always used.
	 */
	ALWAYS_USE_RETRANSFORMATION("Always use retransformation"),

	/**
	 * Class retransfomration will never be used.
	 */
	NEVER_USER_RETRANSFORMATION("Never use retransformation"),

	/**
	 * Class retransformation will be used but not if the underlying JVM is an IBM JVM.
	 */
	DISABLE_RETRANSFORMATION_ON_IBM_JVM("Disable retransformation on IBM JVMs");

	/**
	 * The beatified enumeration name.
	 */
	private final String beautifiedName;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            the beautified name
	 */
	RetransformationStrategy(String name) {
		this.beautifiedName = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return beautifiedName;
	}
}
