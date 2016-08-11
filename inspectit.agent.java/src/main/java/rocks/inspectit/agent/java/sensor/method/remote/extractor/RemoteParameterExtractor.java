package rocks.inspectit.agent.java.sensor.method.remote.extractor;

import org.slf4j.Logger;

import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Extractor to extract the InspectITHeader from http request. The format of the header is
 * "platformId;uniqueID"
 *
 * @author Thomas Kluge
 *
 * @param <T>
 */
public abstract class RemoteParameterExtractor<T extends RemoteCallData> {

	/**
	 * The logger of the class.
	 */
	@Log
	private Logger log;

	/**
	 * Returns a new {@link RemoteCallData} Objects with extracted information or null.
	 *
	 * @param object
	 *            The object containing the inspectIt header.
	 * @return {@link RemoteCallData} with identification and remote platform ident or null (if no
	 *         inspectIT Header is available).
	 */
	public T getRemoteCallData(Object object) {

		String[] inspectITHeaderAttributes = this.getInspectITHeader(object);

		if (inspectITHeaderAttributes != null) {
			try {
				T data = getRemoteCallDataClass().newInstance();
				data.setIdentification(this.getIdentification(inspectITHeaderAttributes));
				data.setRemotePlatformIdent(this.getRemotePlatformIdent(inspectITHeaderAttributes));
				data.setCalling(false);

				return data;
			} catch (InstantiationException e) {
				log.warn("Could not instanciate class " + getRemoteCallDataClass(), e);
			} catch (IllegalAccessException e) {
				log.warn("Could not access class " + getRemoteCallDataClass(), e);
			}

		}

		return null;
	}

	/**
	 * Instantiate an Extractor specifiv instance of the {@link RemoteCallData} object.
	 *
	 * @return A {@link RemoteCallData} object.
	 */
	protected abstract Class<T> getRemoteCallDataClass();

	/**
	 * Extracts the inspectIt header from the given object.
	 *
	 * @param object
	 *            The object.
	 * @return Array with platformId and uniqueId (tracking id).
	 */
	protected abstract String[] getInspectITHeader(Object object);

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
				log.error("InspectIT Header position 2 is not a number.", e);
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
				log.error("InspectIT Header position 1 is not a number.", e);
				remotePlatformIdent = 0;
			}
		}

		return remotePlatformIdent;
	}

}
