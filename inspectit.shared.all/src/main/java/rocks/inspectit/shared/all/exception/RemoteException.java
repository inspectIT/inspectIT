package rocks.inspectit.shared.all.exception;

/**
 * Remote exception that transforms any checked or un-checked exception occurring in the services.
 *
 * @author Ivan Senic
 *
 */
public class RemoteException extends RuntimeException {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6269583794164214516L;

	/**
	 * The canonical name of the original Exception.
	 */
	private final String originalExceptionClass;

	/**
	 * Signature of a server method if one is available.
	 */
	private String serviceMethodSignature;

	/**
	 * No-arg constructor for serialization.
	 */
	protected RemoteException() {
		this.originalExceptionClass = null; // NOPMD
	}

	/**
	 * Constructs a RemoteException using the given original Exception extracting the message, its
	 * canonical name and its original stacktrace.
	 *
	 * @param originalException
	 *            the orginalException used to construct the {@link RemoteException}
	 */
	public RemoteException(Throwable originalException) {
		super(assertNotNull(originalException).getMessage());
		this.originalExceptionClass = originalException.getClass().getCanonicalName();
		super.setStackTrace(originalException.getStackTrace());
	}

	/**
	 * Asserts original exception is not null.
	 *
	 * @param originalException
	 *            the orginalException used to construct the {@link RemoteException}
	 * @return original exception
	 */
	private static Throwable assertNotNull(Throwable originalException) {
		if (originalException == null) {
			throw new IllegalArgumentException("Original Exception for RemoteException must not be null.");
		}
		return originalException;
	}

	/**
	 * Gets {@link #originalExceptionClass}.
	 *
	 * @return {@link #originalExceptionClass}
	 */
	public String getOriginalExceptionClass() {
		return originalExceptionClass;
	}

	/**
	 * Gets {@link #serviceMethodSignature}.
	 *
	 * @return {@link #serviceMethodSignature}
	 */
	public String getServiceMethodSignature() {
		return serviceMethodSignature;
	}

	/**
	 * Sets {@link #serviceMethodSignature}.
	 *
	 * @param serviceMethodSignature
	 *            New value for {@link #serviceMethodSignature}
	 */
	public void setServiceMethodSignature(String serviceMethodSignature) {
		this.serviceMethodSignature = serviceMethodSignature;

		// set also to the cause
		Throwable cause = getCause();
		if (cause instanceof RemoteException) {
			((RemoteException) cause).setServiceMethodSignature(serviceMethodSignature);
		}
	}

}
