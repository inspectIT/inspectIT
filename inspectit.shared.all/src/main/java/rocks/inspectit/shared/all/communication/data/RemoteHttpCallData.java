package rocks.inspectit.shared.all.communication.data;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;

/**
 * Data object holding http remote call based data.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpCallData extends RemoteCallData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8975257310429279701L;

	/**
	 * The response code of the http request.
	 */
	private int responseCode;

	/**
	 * The url of the http request.
	 */
	private String url;

	/**
	 * Http response code.
	 *
	 * @return {@link #responseCode}
	 */
	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Requested url.
	 *
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);

		size += objectSizes.getPrimitiveTypesSize(0, 0, 0, 0, 1, 0);
		size += objectSizes.getSizeOf(url);

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
		result = prime * result + responseCode;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		RemoteHttpCallData other = (RemoteHttpCallData) obj;
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

	@Override
	public String getSpecificData() {
		if (this.isCalling()) {
			return "URL: " + this.url + " ; Response Code: " + this.responseCode;
		} else {
			return super.getSpecificData();
		}

	}

}