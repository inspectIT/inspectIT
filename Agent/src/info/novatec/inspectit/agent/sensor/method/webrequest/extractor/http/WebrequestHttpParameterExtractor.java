package info.novatec.inspectit.agent.sensor.method.webrequest.extractor.http;

import info.novatec.inspectit.util.ReflectionCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor to extract the InspectITHeader from http request. The format of the header is
 * "platformId;registeredSensorTypeId;registeredMethodId;timestamp"
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestHttpParameterExtractor {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebrequestHttpParameterExtractor.class);

	/**
	 * Name of the InspectItHeader.
	 */
	private static final String INSPECTIT_HEADER = "InspectITHeader";
	/**
	 * Apache sepcific method name to get the URL.
	 */
	private static final String METHOD_NAME_HEADER = "getHeader";

	/**
	 * String to split inspectIt header into subparts. Each subpart has a different value with
	 * different informations.
	 */
	private static final String CHAR_SPLITT_INSPECTIT_HEADER = ";";

	/**
	 * Number of subparts of the inspectIt header. After Split the inspectIt header by
	 * {@link #CHAR_SPLITT_INSPECTIT_HEADER} the resulting array contains this amount of elements.
	 */
	private static final int INSPECTIT_HEADER_ELEMENT_COUNT = 4;

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Extracts the InspectIt header from http header. If the http header does not contain the
	 * inspectIt header the methode returns null. If the format of the inspectIt header is wrong the
	 * methode returns null.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the InspectITHeader
	 */
	private String[] getInspectITHeader(Class<?> httpServletRequestClass, Object httpServletRequest) {

		String inspectITHeader = null;
		try {
			inspectITHeader = (String) cache.invokeMethod(httpServletRequestClass, METHOD_NAME_HEADER, new Class[] { String.class }, httpServletRequest, new Object[] { INSPECTIT_HEADER }, null);

		} catch (Exception e) {
			LOG.error("Invocation on given object failed.", e);
		}

		if (inspectITHeader != null) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectIt Header extraced: " + inspectITHeader);
			}

			String[] inspectItHeaderAttributes = inspectITHeader.split(CHAR_SPLITT_INSPECTIT_HEADER);

			if (inspectItHeaderAttributes.length != INSPECTIT_HEADER_ELEMENT_COUNT) {
				LOG.error("InspectIT Header lenght is not " + INSPECTIT_HEADER_ELEMENT_COUNT);
				return null;
			}

			return inspectItHeaderAttributes;
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No InspectIt Header available.");
			}
			return null;
		}
	}

	/**
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return true if the the header contains the InspectIt header.
	 */
	public boolean providesInspectItHeader(Class<?> httpServletRequestClass, Object httpServletRequest) {
		return this.getInspectITHeader(httpServletRequestClass, httpServletRequest) != null;
	}

	/**
	 * 
	 * The InspectIt Header is a String which uses ";" to split values. The 4. value is the
	 * timestamp/identification.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return Unique identification of the remote call.
	 */
	public long getIdentification(Class<?> httpServletRequestClass, Object httpServletRequest) {
		String[] inspectItHeaderAttributes = this.getInspectITHeader(httpServletRequestClass, httpServletRequest);

		long identification = 0;
		if (inspectItHeaderAttributes != null) {

			try {
				identification = Long.valueOf(inspectItHeaderAttributes[3]);
			} catch (NumberFormatException e) {
				LOG.error("InspectIT Header position 4 is not a number.", e);
				identification = 0;
			}
		}

		return identification;
	}

	/**
	 * 
	 * The InspectIt Header is a String which uses ";" to split values. The 1. value is the platform
	 * ident of the request.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return Platform ident of the request.
	 */
	public long getRemotePlatformIdent(Class<?> httpServletRequestClass, Object httpServletRequest) {
		String[] inspectItHeaderAttributes = this.getInspectITHeader(httpServletRequestClass, httpServletRequest);

		long remotePlatformIdent = 0;
		if (inspectItHeaderAttributes != null) {

			try {
				remotePlatformIdent = Long.valueOf(inspectItHeaderAttributes[0]);
			} catch (NumberFormatException e) {
				LOG.error("InspectIT Header position 4 is not a number.", e);
				remotePlatformIdent = 0;
			}
		}

		return remotePlatformIdent;
	}
}
