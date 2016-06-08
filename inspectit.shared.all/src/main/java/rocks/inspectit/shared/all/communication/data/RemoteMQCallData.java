/**
 *
 */
package rocks.inspectit.shared.all.communication.data;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;

/**
 * Data object holding mq remote call based data.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteMQCallData extends RemoteCallData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2008314999584037319L;

	/**
	 * JMSMessageID.
	 */
	private String messageId;

	/**
	 * JMSDestination.
	 */
	private String messageDestination;

	/**
	 * Gets {@link #messageId}.
	 *
	 * @return {@link #messageId}
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Sets {@link #messageId}.
	 *
	 * @param messageId
	 *            New value for {@link #messageId}
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * Gets {@link #messageDestination}.
	 *
	 * @return {@link #messageDestination}
	 */
	public String getMessageDestination() {
		return messageDestination;
	}

	/**
	 * Sets {@link #messageDestination}.
	 *
	 * @param messageDestination
	 *            New value for {@link #messageDestination}
	 */
	public void setMessageDestination(String messageDestination) {
		this.messageDestination = messageDestination;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);

		size += objectSizes.getSizeOf(messageId);
		size += objectSizes.getSizeOf(messageDestination);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
		result = prime * result + ((messageDestination == null) ? 0 : messageDestination.hashCode());
		return result;
	}

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
		RemoteMQCallData other = (RemoteMQCallData) obj;
		if (messageId == null) {
			if (other.messageId != null) {
				return false;
			}
		} else if (!messageId.equals(other.messageId)) {
			return false;
		}
		if (messageDestination == null) {
			if (other.messageDestination != null) {
				return false;
			}
		} else if (!messageDestination.equals(other.messageDestination)) {
			return false;
		}
		return true;
	}

	@Override
	public String getSpecificData() {
		if (this.isCalling()) {
			return "Message Destination: " + this.messageDestination + " ; Message ID: " + this.messageId;
		} else {
			return super.getSpecificData();
		}

	}

}
