package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.Sizeable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Entity for holding HTTP info information.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@Table(indexes = { @Index(name = "uri_idx", columnList = "uri"), @Index(name = "tag_idx", columnList = "inspectItTaggingHeaderValue") })
public class HttpInfo implements Sizeable, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6649412114779075805L;

	/**
	 * String used to represent an unset <code>uri</code> or <code>requestMethod</code>.
	 */
	public static final String UNDEFINED = "n.a.";

	/**
	 * Max URI chars size.
	 */
	private static final int MAX_URI_SIZE = 1000;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long id;

	/**
	 * The uri.
	 */
	@Column(length = 1000)
	private String uri = UNDEFINED;

	/**
	 * The request method.
	 */
	private String requestMethod = UNDEFINED;

	/**
	 * The inspectIT tag.
	 */
	private String inspectItTaggingHeaderValue;

	/**
	 * No-arg constructors.
	 */
	public HttpInfo() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param uri
	 *            The uri.
	 * @param requestMethod
	 *            The request method.
	 * @param inspectItTaggingHeaderValue
	 *            The inspectIT tag.
	 */
	public HttpInfo(String uri, String requestMethod, String inspectItTaggingHeaderValue) {
		this.uri = uri;
		this.requestMethod = requestMethod;
		this.inspectItTaggingHeaderValue = inspectItTaggingHeaderValue;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns if the URI is defined for this instance.
	 * 
	 * @return True if {@link #uri} is not null and is different from {@value HttpInfo#UNDEFINED}.
	 */
	public boolean isUriDefined() {
		return uri != null && !HttpInfo.UNDEFINED.equals(uri);
	}

	/**
	 * Gets {@link #uri}.
	 * 
	 * @return {@link #uri}
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets {@link #uri}.
	 * 
	 * @param uri
	 *            New value for {@link #uri}
	 */
	public void setUri(String uri) {
		if (null != uri) {
			if (uri.length() > MAX_URI_SIZE) {
				this.uri = uri.substring(0, MAX_URI_SIZE);
			} else {
				this.uri = uri;
			}
		}
	}

	/**
	 * Gets {@link #requestMethod}.
	 * 
	 * @return {@link #requestMethod}
	 */
	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * Sets {@link #requestMethod}.
	 * 
	 * @param requestMethod
	 *            New value for {@link #requestMethod}
	 */
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * Checks if this data has the inspectIT tagging header set.
	 * 
	 * @return if this data has the inspectIT tagging header set.
	 */
	public boolean hasInspectItTaggingHeader() {
		return null != getInspectItTaggingHeaderValue();
	}

	/**
	 * Gets {@link #inspectItTaggingHeaderValue}.
	 * 
	 * @return {@link #inspectItTaggingHeaderValue}
	 */
	public String getInspectItTaggingHeaderValue() {
		return inspectItTaggingHeaderValue;
	}

	/**
	 * Sets {@link #inspectItTaggingHeaderValue}.
	 * 
	 * @param inspectItTaggingHeaderValue
	 *            New value for {@link #inspectItTaggingHeaderValue}
	 */
	public void setInspectItTaggingHeaderValue(String inspectItTaggingHeaderValue) {
		this.inspectItTaggingHeaderValue = inspectItTaggingHeaderValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		return getObjectSize(objectSizes, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(3, 0, 0, 0, 1, 0);
		size += objectSizes.getSizeOf(uri, requestMethod, inspectItTaggingHeaderValue);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((inspectItTaggingHeaderValue == null) ? 0 : inspectItTaggingHeaderValue.hashCode());
		result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpInfo other = (HttpInfo) obj;
		if (id != other.id) {
			return false;
		}
		if (inspectItTaggingHeaderValue == null) {
			if (other.inspectItTaggingHeaderValue != null) {
				return false;
			}
		} else if (!inspectItTaggingHeaderValue.equals(other.inspectItTaggingHeaderValue)) {
			return false;
		}
		if (requestMethod == null) {
			if (other.requestMethod != null) {
				return false;
			}
		} else if (!requestMethod.equals(other.requestMethod)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
