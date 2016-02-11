package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Data object holding http based timer data. All timer related information are inherited from the
 * super class.
 * 
 * <b> Be careful when adding new attributes. Do not forget to add them to the size calculation.
 * </b>
 * 
 * @author Stefan Siegl
 */
@Entity
public class HttpTimerData extends TimerData {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -7868876342858232388L;

	/**
	 * The default header for tagged requests.
	 */
	public static final String INSPECTIT_TAGGING_HEADER = "inspectit";

	/**
	 * String used to represent multiple request methods in an aggregation.
	 */
	public static final String REQUEST_METHOD_MULTIPLE = "MULTIPLE";

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
	 * Http info for optimizing saving to the DB.
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private HttpInfo httpInfo = new HttpInfo();

	/**
	 * No-args constructor.
	 */
	public HttpTimerData() {
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
	public HttpTimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
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
			httpInfo.setInspectItTaggingHeaderValue(headers.get(INSPECTIT_TAGGING_HEADER));
		} else {
			httpInfo.setInspectItTaggingHeaderValue(HttpInfo.UNDEFINED);
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
	 * Gets {@link #httpInfo}.
	 * 
	 * @return {@link #httpInfo}
	 */
	public HttpInfo getHttpInfo() {
		return httpInfo;
	}

	/**
	 * Sets {@link #httpInfo}.
	 * 
	 * @param httpInfo
	 *            New value for {@link #httpInfo}
	 */
	public void setHttpInfo(HttpInfo httpInfo) {
		this.httpInfo = httpInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(5, 0, 0, 0, 0, 0);

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

		size += objectSizes.getSizeOf(httpInfo);

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
		int result = super.hashCode();
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((httpInfo == null) ? 0 : httpInfo.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((sessionAttributes == null) ? 0 : sessionAttributes.hashCode());
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
		if (httpInfo == null) {
			if (other.httpInfo != null) {
				return false;
			}
		} else if (!httpInfo.equals(other.httpInfo)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (sessionAttributes == null) {
			if (other.sessionAttributes != null) {
				return false;
			}
		} else if (!sessionAttributes.equals(other.sessionAttributes)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String sup = super.toString();
		return sup + "HttpTimerData [uri=" + (null != httpInfo ? httpInfo.getUri() : HttpInfo.UNDEFINED) + ", parameters=" + parameters + ", attributes=" + attributes + ", headers=" + headers + "]";
	}
}
