package info.novatec.inspectit.agent.sensor.method.remote.extractor.http;

import info.novatec.inspectit.agent.sensor.method.remote.RemoteConstant;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.ReflectionCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Extractor to extract the InspectITHeader from http request. The format of the header is
 * "platformId;uniqueID"
 * 
 * @author Thomas Kluge
 * 
 */
@Component
public class RemoteHttpParameterExtractor {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteHttpParameterExtractor.class);

	/**
	 * Apache specific method name to get the URL.
	 */
	private static final String METHOD_NAME_HEADER = "getHeader";

	/**
	 * String to split inspectIt header into subparts. Each subpart has a different value with
	 * different informations.
	 */
	private static final String CHAR_SPLIT_INSPECTIT_HEADER = ";";

	/**
	 * Number of subparts of the inspectIt header. After Split the inspectIt header by
	 * {@link #CHAR_SPLIT_INSPECTIT_HEADER} the resulting array contains this amount of elements.
	 */
	private static final int INSPECTIT_HEADER_ELEMENT_COUNT = 2;

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
			inspectITHeader = (String) cache.invokeMethod(httpServletRequestClass, METHOD_NAME_HEADER, new Class[] { String.class }, httpServletRequest, new Object[] { RemoteConstant.INSPECTIT_HTTP_HEADER }, null);
		} catch (Exception e) {
			
			LOG.error("Invocation on given object failed.", e);
		}

		if (inspectITHeader != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectIt Header extracted: " + inspectITHeader);
			}

			String[] inspectItHeaderAttributes = inspectITHeader.split(CHAR_SPLIT_INSPECTIT_HEADER);

			if (inspectItHeaderAttributes.length != INSPECTIT_HEADER_ELEMENT_COUNT) {
				LOG.error("InspectIT Header length is not " + INSPECTIT_HEADER_ELEMENT_COUNT);
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
	 * Returns a new {@link RemoteCallData} Objects with extracted information or null.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return {@link RemoteCallData} with identification and remote platform ident or null (if no
	 *         inspectIT Header is available).
	 */
	public RemoteCallData getRemoteCallData(Class<?> httpServletRequestClass, Object httpServletRequest) {

		String[] inspectITHeaderAttributes = this.getInspectITHeader(httpServletRequestClass, httpServletRequest);

		if (inspectITHeaderAttributes != null) {
			RemoteCallData data = new RemoteCallData();

			data.setIdentification(this.getIdentification(inspectITHeaderAttributes));
			data.setRemotePlatformIdent(this.getRemotePlatformIdent(inspectITHeaderAttributes));
			data.setCalling(false);

			return data;
		}

		return null;
	}

	/**
	 * The InspectIt Header is a String which uses ";" to split values. The 2. value is the
	 * identification.
	 * 
	 * @param inspectITHeaderAttributes
	 *            Extracted inspectIT Header Attributes.
	 * @return Unique identification of the remote call.
	 */
	private long getIdentification(String[] inspectITHeaderAttributes) {

		long identification = 0;
		if (inspectITHeaderAttributes != null) {

			try {
				identification = Long.valueOf(inspectITHeaderAttributes[1]);
			} catch (NumberFormatException e) {
				LOG.error("InspectIT Header position 2 is not a number.", e);
				identification = 0;
			}
		}

		return identification;
	}

	/**
	 * 
	 * The inspectIt Header is a String which uses ";" to split values. The 1. value is the platform
	 * ident of the request.
	 * 
	 * @param inspectITHeaderAttributes
	 *            Extracted inspectIT Header Attributes. the object realizing the
	 *            <code> HttpServletRequest </code> interface.
	 * @return Platform ident of the request.
	 */
	private long getRemotePlatformIdent(String[] inspectITHeaderAttributes) {

		long remotePlatformIdent = 0;
		if (inspectITHeaderAttributes != null) {

			try {
				remotePlatformIdent = Long.valueOf(inspectITHeaderAttributes[0]);
			} catch (NumberFormatException e) {
				LOG.error("InspectIT Header position 1 is not a number.", e);
				remotePlatformIdent = 0;
			}
		}

		return remotePlatformIdent;
	}
}
