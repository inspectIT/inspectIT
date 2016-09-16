package rocks.inspectit.agent.java.proxy.impl;


/**
 * Exception showing that the proxy description was wrong.
 *
 * @author Jonas Kunz
 */
public class InvalidProxyDescriptionException extends Exception {

	/**
	 * UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Private constructor for silencing PMD advice to make this class static.
	 */
	@SuppressWarnings("unused")
	private InvalidProxyDescriptionException() {
	}

	/**
	 * @param message the message
	 */
	public InvalidProxyDescriptionException(String message) {
		super(message);
	}

	/**
	 * Utiltiy method for easy exception throwing. The given format String is
	 * used to print the exception message. However, this method performs some
	 * uility modifications when printing: <br>
	 * a) Class<?> instances will be printed using their canonical names <br>
	 * b) Class<?> arrays will be printed in the form (class1, class2, ...)
	 * using their canonical names
	 *
	 * @param formatMsg the format message
	 * @param args the arguemnts for the format message
	 * @throws InvalidProxyDescriptionException the generated exception
	 */
	public static void throwException(String formatMsg, Object... args) throws InvalidProxyDescriptionException {
		// convert classes to their name
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof Class<?>) {
				Class<?> cl = ((Class<?>) args[i]);
				if (cl.isPrimitive()) {
					args[i] = cl.getName();
				} else {
					args[i] = cl.getCanonicalName();
				}
			} else if (args[i] instanceof Class<?>[]) {
				String printed = "(";
				for (Class<?> cl : (Class<?>[]) args[i]) {
					String name;
					if (cl.isPrimitive()) {
						name = cl.getName();
					} else {
						name = cl.getCanonicalName();
					}
					if ("(".equals(printed)) {
						printed += name;
					} else {
						printed += ", " + name;
					}
				}
				printed += ")";
				args[i] = printed;
			}
		}
		throw new InvalidProxyDescriptionException(String.format(formatMsg,
				args));
	}

}
