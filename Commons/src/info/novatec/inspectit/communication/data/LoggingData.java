package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * Data class for logging data capturing.
 * 
 * @author Stefan Siegl
 */
public class LoggingData extends InvocationAwareData {

	/** serial version id. */
	private static final long serialVersionUID = 6428356462914363539L;
	/** the logging level. */
	private String level;
	/** the message that was logged. */
	private String message;
	/** number of instances. */
	private long count;

	public LoggingData() {
	}

	/**
	 * @param level
	 * @param message
	 */
	public LoggingData(String level, String message) {
		super();
		this.level = level;
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	public double getInvocationAffiliationPercentage() {
		// TODO: wtf?
		return 1.0d;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * increases the count by 1.
	 */
	public void increaseCount() {
		this.count++;
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		if (count != other.count) {
			return false;
		}
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
	public long getObjectSize(IObjectSizes objectSizes) {
		// TODO Auto-generated method stub
		return super.getObjectSize(objectSizes);
	}

}
