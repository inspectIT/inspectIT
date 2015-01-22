package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;

/**
 * 
 * Data object holding remote call based timer data. All timer related information are inherited
 * from the super class.
 * 
 * 
 * @author Thomas Kluge
 * 
 */
public class RemoteCallData extends TimerData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5352767733319322767L;

	/**
	 * Unique number to combine the calling invocation sequenze and the called. This number must be
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
	 * The response code of the http request.
	 */
	private int responseCode;

	/**
	 * The url of the http request.
	 */
	private String url;

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
		RemoteCallData other = (RemoteCallData) obj;
		if (identification != other.identification) {
			return false;
		}
		if (calling != other.calling) {
			return false;
		}
		if (responseCode != other.responseCode) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	public long getIdentification() {
		return identification;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);

		size += objectSizes.getPrimitiveTypesSize(0, 1, 1, 0, 2, 0);
		size += objectSizes.getSizeOf(url);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * Http response code.
	 * 
	 * @return {@link #responseCode}
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * Requested url.
	 * 
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * True if it is the request. Fals if it is the response.
	 * 
	 * @return {@link #calling}
	 */
	public boolean isCalling() {
		return calling;
	}

	public boolean getCalling() {
		return calling;
	}

	public void setCalling(boolean isCalling) {
		this.calling = isCalling;
	}

	public void setIdentification(long identification) {
		this.identification = identification;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp = identification;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (calling ? 1231 : 1237);
		result = prime * result + (int) (remotePlatformIdent ^ (remotePlatformIdent >>> 32));
		temp = responseCode;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	public long getRemotePlatformIdent() {
		return remotePlatformIdent;
	}

	public void setRemotePlatformIdent(long remotePlatformIdent) {
		this.remotePlatformIdent = remotePlatformIdent;
	}

}
