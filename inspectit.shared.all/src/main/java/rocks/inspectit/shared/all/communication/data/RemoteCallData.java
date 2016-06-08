package rocks.inspectit.shared.all.communication.data;

import java.sql.Timestamp;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;

/**
 * Data object holding remote call based data.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteCallData extends InvocationAwareData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5352767733319322767L;

	/**
	 * Unique number to combine the calling invocation sequence and the called. This number must be
	 * the same to combine them.
	 */
	private long identification;

	/**
	 * true if this is the object of the requesting invocation sequence. false if this is the object
	 * of the responding invocation sequence.
	 */
	private boolean calling;

	/**
	 * The platformIdent of the requesting platform.
	 */
	private long remotePlatformIdent;

	/**
	 * Constructor.
	 */
	public RemoteCallData() {
	}

	/**
	 * Constructor.
	 *
	 * @param timeStamp
	 *            the timestamp of this data
	 * @param platformIdent
	 *            the platform identification
	 * @param sensorTypeIdent
	 *            the sensor type
	 * @param methodIdent
	 *            the method this data comes from
	 */
	public RemoteCallData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Generates a String with specific content.
	 *
	 * @return String.
	 */
	public String getSpecificData() {
		return "";
	}

	/**
	 * True if it is the request. Fals if it is the response.
	 *
	 * @return {@link #calling}
	 */
	public boolean isCalling() {
		return calling;
	}

	public void setCalling(boolean isCalling) {
		this.calling = isCalling;
	}

	public long getIdentification() {
		return identification;
	}

	public void setIdentification(long identification) {
		this.identification = identification;
	}

	public long getRemotePlatformIdent() {
		return remotePlatformIdent;
	}

	public void setRemotePlatformIdent(long remotePlatformIdent) {
		this.remotePlatformIdent = remotePlatformIdent;
	}

	@Override
	public double getInvocationAffiliationPercentage() {
		return getObjectsInInvocationsCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);

		size += objectSizes.getPrimitiveTypesSize(0, 1, 0, 0, 2, 0);

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
		result = prime * result + (calling ? 1231 : 1237);
		result = prime * result + (int) (identification ^ (identification >>> 32));
		result = prime * result + (int) (remotePlatformIdent ^ (remotePlatformIdent >>> 32));
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
		RemoteCallData other = (RemoteCallData) obj;
		if (calling != other.calling) {
			return false;
		}
		if (identification != other.identification) {
			return false;
		}
		if (remotePlatformIdent != other.remotePlatformIdent) {
			return false;
		}
		return true;
	}

}