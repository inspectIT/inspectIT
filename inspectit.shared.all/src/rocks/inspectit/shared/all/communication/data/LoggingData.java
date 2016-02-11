package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * Data class for logging data capturing.
 *
 * @author Stefan Siegl
 */
public class LoggingData extends InvocationAwareData {

	/** Serial version id. */
	private static final long serialVersionUID = 6428356462914363539L;

	/** The logging level. */
	private String level;

	/** The message that was logged. */
	private String message;

	/**
	 * Default constructor.
	 */
	public LoggingData() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param level
	 *            the logging level/severity.
	 * @param message
	 *            the message.
	 */
	public LoggingData(String level, String message) {
		super();
		this.level = level;
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getInvocationAffiliationPercentage() {
		return 1.0d;
	}

	/**
	 * Gets {@link #level}.
	 *
	 * @return {@link #level}
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * Sets {@link #level}.
	 *
	 * @param level
	 *            New value for {@link #level}
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * Gets {@link #message}.
	 *
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets {@link #message}.
	 *
	 * @param message
	 *            New value for {@link #message}
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((level == null) ? 0 : level.hashCode());
		result = (prime * result) + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LoggingData other = (LoggingData) obj;
		if (level == null) {
			if (other.level != null) {
				return false;
			}
		} else if (!level.equals(other.level)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "LoggingData [level=" + level + ", message=" + message + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(3, 0, 0, 0, 0, 1);
		size += objectSizes.getSizeOf(message);
		size += objectSizes.getSizeOf(level);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
