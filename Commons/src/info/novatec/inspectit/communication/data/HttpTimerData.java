package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Data object holding http based timer data. All timer related information are inherited from the
 * super class.
 * 
 * <b> Be careful when adding new attributes. Do not forget to add them to the size
 * calculation. </b>
 * 
 * @author Stefan Siegl
 */
@Table(indexes = { @Index(name = "uri_idx", columnList = "uri"), @Index(name = "tag_idx", columnList = "inspectItTaggingHeaderValue") })
@Entity
public class HttpTimerData extends TimerData implements Cloneable {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -7868876342858232388L;

	/**
	 * The default header for tagged requests.
	 */
	public static final String INSPECTIT_TAGGING_HEADER = "inspectit";

	/**
	 * String used to represent an unset <code>uri</code> or <code>requestMethod</code>.
	 */
	public static final String UNDEFINED = "n.a.";

	/**
	 * String used to represent multiple request methods in an aggregation.
	 */
	public static final String REQUEST_METHOD_MULTIPLE = "MULTIPLE";
	/**
	 * Max URI chars size.
	 */
	private static final int MAX_URI_SIZE = 1000;

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
	 * Map is String-String[].
	 */
	@Transient
	private Map<String, String[]> parameters = null;

	/**
	 * Map is String-String.
	 */
	@Transient
	private Map<String, String> attributes = null;

	/**
	 * Map is String-String.
	 */
	@Transient
	private Map<String, String> headers = null;

	/**
	 * Map is String-String.
	 */
	@Transient
	private Map<String, String> sessionAttributes = null;

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
	public HttpTimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Constructor.
	 */
	public HttpTimerData() {
	}

	/**
	 * Checks if this data has the inspectIT tagging header set.
	 * 
	 * @return if this data has the inspectIT tagging header set.
	 */
	public boolean hasInspectItTaggingHeader() {
		return null != inspectItTaggingHeaderValue;
	}

	/**
	 * Retrieves the value of the inspectit tagging header.
	 * 
	 * @return the value of the inspectit tagging header.
	 */
	public String getInspectItTaggingHeaderValue() {
		return inspectItTaggingHeaderValue;
	}

	/**
	 * Sets the value for the inspectIT header.
	 * 
	 * @param value
	 *            the value for the inspectIT header.
	 */
	public void setInspectItTaggingHeaderValue(String value) {
		this.inspectItTaggingHeaderValue = value;
	}

	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 * 
	 * @param uri
	 *            the uri.
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
	 * Returns if the URI is defined for this instance.
	 * 
	 * @return True if {@link #uri} is not null and is different from {@value #UNDEFINED}.
	 */
	public boolean isUriDefined() {
		return uri != null && !UNDEFINED.equals(uri);
	}

	/**
	 * Gets {@link #parameters}.
	 * 
	 * @return {@link #parameters}
	 */
	public Map<String, String[]> getParameters() {
		return parameters;
	}

	/**
	 * Sets {@link #parameters}.
	 * 
	 * @param parameters
	 *            New value for {@link #parameters}
	 */
	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets {@link #attributes}.
	 * 
	 * @return {@link #attributes}
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets {@link #attributes}.
	 * 
	 * @param attributes
	 *            New value for {@link #attributes}
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets {@link #headers}.
	 * 
	 * @return {@link #headers}
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Sets {@link #headers}.
	 * 
	 * @param headers
	 *            New value for {@link #headers}
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;

		// set tag value if it exists
		if (null != headers) {
			this.inspectItTaggingHeaderValue = headers.get(INSPECTIT_TAGGING_HEADER);
		} else {
			this.inspectItTaggingHeaderValue = UNDEFINED;
		}
	}

	/**
	 * Gets {@link #sessionAttributes}.
	 * 
	 * @return {@link #sessionAttributes}
	 */
	public Map<String, String> getSessionAttributes() {
		return sessionAttributes;
	}

	/**
	 * Sets {@link #sessionAttributes}.
	 * 
	 * @param sessionAttributes
	 *            New value for {@link #sessionAttributes}
	 */
	public void setSessionAttributes(Map<String, String> sessionAttributes) {
		this.sessionAttributes = sessionAttributes;
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
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(7, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOf(uri, requestMethod, inspectItTaggingHeaderValue);

		if (null != parameters) {
			size += objectSizes.getSizeOfHashMap(parameters.size());
			for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				String[] values = entry.getValue();
				size += objectSizes.getSizeOfArray(values.length);
				for (int i = 0; i < values.length; i++) {
					size += objectSizes.getSizeOf(values[i]);
				}
			}
		}

		if (null != attributes) {
			size += objectSizes.getSizeOfHashMap(attributes.size());
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

		if (null != headers) {
			size += objectSizes.getSizeOfHashMap(headers.size());
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

		if (null != sessionAttributes) {
			size += objectSizes.getSizeOfHashMap(sessionAttributes.size());
			for (Map.Entry<String, String> entry : sessionAttributes.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

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
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
		result = prime * result + ((sessionAttributes == null) ? 0 : sessionAttributes.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpTimerData other = (HttpTimerData) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (requestMethod == null) {
			if (other.requestMethod != null) {
				return false;
			}
		} else if (!requestMethod.equals(other.requestMethod)) {
			return false;
		}
		if (sessionAttributes == null) {
			if (other.sessionAttributes != null) {
				return false;
			}
		} else if (!sessionAttributes.equals(other.sessionAttributes)) {
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

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String sup = super.toString();
		return sup + "HttpTimerData [uri=" + uri + ", parameters=" + parameters + ", attributes=" + attributes + ", headers=" + headers + "]";
	}

}
