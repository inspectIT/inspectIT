package info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.apache;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.WebrequestDefaultHttpInserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.ReflectionCache;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implements the {@link WebrequestDefaultHttpInserterHook} class. It puts the InspectIT
 * header as additional header/attribute to the remote call.
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestApacheHttpInserterHook extends WebrequestDefaultHttpInserterHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebrequestApacheHttpInserterHook.class);
	/**
	 * Apache specific method name to add InspectITHeader.
	 */
	private static final String METHOD_NAME = "addRequestProperty";

	/**
	 * Apache sepcific method name to get the URL.
	 */
	private static final String METHOD_NAME_URL = "getURL";
	/**
	 * Apache sepcific method name to get the response code.
	 */
	private static final String METHOD_NAME_RESPONSE_CODE = "getResponseCode";

	/**
	 * Apache specific methode signature to add InspechtITHeader.
	 */
	private static final Class<?>[] METHOD_PARAMETER = new Class<?>[] { String.class, String.class };
	/**
	 * Empty methode signature.
	 */
	private static final Class<?>[] METHOD_PARAMETER_EMPTY = new Class<?>[] {};

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Constructor.
	 * 
	 * * @param timer The timer
	 * 
	 * @param idManager
	 *            The id manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 * @param threadMXBean
	 *            the threadMx Bean for cpu timing
	 */
	public WebrequestApacheHttpInserterHook(IIdManager idManager, Timer timer, RemoteIdentificationManager remoteIdentificationManager, ThreadMXBean threadMXBean) {
		super(idManager, timer, remoteIdentificationManager, threadMXBean);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String readURL(Object object) {

		URL url = null;
		try {
			Object returnValue = cache.invokeMethod(object.getClass(), METHOD_NAME_URL, METHOD_PARAMETER_EMPTY, object, null, null);

			url = (URL) returnValue;

			if (LOG.isDebugEnabled()) {
				LOG.debug("URL: " + url.toString());
			}

		} catch (Exception e) {
			LOG.warn("Could not read URL Object from Webrequest. No URL Information available.");
		}

		if (url != null) {
			return url.toString();
		} else {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int readResponseCode(Object object) {

		int responseCode = 0;
		try {
			Object returnValue = cache.invokeMethod(object.getClass(), METHOD_NAME_RESPONSE_CODE, METHOD_PARAMETER_EMPTY, object, null, null);

			responseCode = (Integer) returnValue;

			if (LOG.isDebugEnabled()) {
				LOG.debug("ResponseCode: " + responseCode);
			}

		} catch (Exception e) {
			LOG.warn("Could not read response code from Webrequest.");
		}

		return responseCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {

		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(methodId, sensorTypeId, identification);

		try {
			cache.invokeMethod(object.getClass(), METHOD_NAME, METHOD_PARAMETER, object, new Object[] { INSPECTIT_HEADER, inspectItHeader }, null);

			RemoteCallData remoteCallData = new RemoteCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(idManager.getPlatformId());
			this.threadRemoteCallData.set(remoteCallData);

		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
				LOG.warn(e.getMessage());
			}
		}

		if (this.threadRemoteCallData.get() != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader inserted: " + inspectItHeader);
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader not inserted: " + inspectItHeader);
			}
		}
	}
}
