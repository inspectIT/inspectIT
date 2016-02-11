package rocks.inspectit.shared.all.exception;

/**
 * This is base exception class for all the exception we throw, thus expected exceptions or business
 * exceptions in fact.
 * 
 * @author Ivan Senic
 * 
 */
public class BusinessException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3961488831742178282L;

	/**
	 * Description about action being performed.
	 */
	private final String actionPerformed;

	/**
	 * Error code describing the exception.
	 */
	private final IErrorCode errorCode;

	/**
	 * Signature of a server method if one is available.
	 */
	private String serviceMethodSignature;

	/**
	 * No-arg constructor for serialization.
	 */
	protected BusinessException() {
		this(null);
	}

	/**
	 * Default constructor.
	 * 
	 * @param errorCode
	 *            Error code describing the exception.
	 */
	public BusinessException(IErrorCode errorCode) {
		this(null, errorCode);
	}

	/**
	 * Constructor that allows definition of the action.
	 * 
	 * @param actionPerformed
	 *            Action being performed when exception occurred.
	 * @param errorCode
	 *            Error code describing the exception.
	 */
	public BusinessException(String actionPerformed, IErrorCode errorCode) {
		super(generateMessage(actionPerformed, errorCode));
		this.actionPerformed = actionPerformed;
		this.errorCode = errorCode;
	}

	/**
	 * Creates a exception message based on the action performed and {@link IErrorCode}.
	 * 
	 * @param actionPerformed
	 *            Action being performed when exception occurred.
	 * @param errorCode
	 *            Error code describing the exception.
	 * @return Exception message.
	 */
	private static String generateMessage(String actionPerformed, IErrorCode errorCode) {
		if (null == errorCode) {
			return null;
		}

		String s = "The '" + errorCode.getName() + "' exception occurred on the '" + errorCode.getComponent() + "' component.";
		if (null != actionPerformed) {
			s += " Exception occurred executing the action: '" + actionPerformed + "'.";
		}
		return s;
	}

	/**
	 * Gets {@link #actionPerformed}.
	 * 
	 * @return {@link #actionPerformed}
	 */
	public String getActionPerformed() {
		return actionPerformed;
	}

	/**
	 * Gets {@link #errorCode}.
	 * 
	 * @return {@link #errorCode}
	 */
	public IErrorCode getErrorCode() {
		return errorCode;
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
	}

}
