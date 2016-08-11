package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.extractor.RemoteParameterExtractor;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Extractor to extract the InspectITHeader from http request. The format of the header is
 * "platformId;uniqueID"
 *
 * @author Thomas Kluge
 *
 */
@Component
public class RemoteHttpParameterExtractor extends RemoteParameterExtractor<RemoteHttpCallData> {

	/**
	 * The logger of the class.
	 */
	@Log
	private Logger log;

	/**
	 * Apache specific method name to get the URL.
	 */
	private static final String METHOD_NAME_HEADER = "getHeader";

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<RemoteHttpCallData> getRemoteCallDataClass() {
		return RemoteHttpCallData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getInspectITHeader(Object object) {
		String inspectITHeader = (String) cache.invokeMethod(object.getClass(), METHOD_NAME_HEADER, new Class[] { String.class }, object, new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER }, null);

		if (inspectITHeader != null) {
			if (log.isDebugEnabled()) {
				log.debug("InspectIt Header extracted: " + inspectITHeader);
			}

			String[] inspectItHeaderAttributes = StringUtils.split(inspectITHeader, RemoteConstants.CHAR_SPLIT_INSPECTIT_HEADER);

			if (inspectItHeaderAttributes.length != RemoteConstants.INSPECTIT_HEADER_ELEMENT_COUNT) {
				log.error("InspectIT Header length is not " + RemoteConstants.INSPECTIT_HEADER_ELEMENT_COUNT);
				return null;
			}

			return inspectItHeaderAttributes;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("No InspectIt Header available.");
			}
			return null;
		}
	}

}