package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteDefaultInserterHook;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.RemoteHttpInserterHook;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;

/**
 * The hook implements the {@link RemoteDefaultInserterHook} class for the . It puts the InspectIT
 * header as additional header/attribute to the remote call. The hook invokes the methode
 * {@link #METHOD_NAME} to add the header attribute.
 *
 *
 * The methodes {@link #METHOD_NAME_URL} and {@link #METHOD_NAME_RESPONSE_CODE} are used to extraced
 * the called URL and the Response Code of the Request.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteApacheHttpClientV40InserterHook extends RemoteHttpInserterHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteApacheHttpClientV40InserterHook.class);

	/**
	 * Apache specific method name to add inspectIT header.
	 */
	private static final String METHOD_NAME = "addHeader";

	/**
	 * Apache specific method to get request line.
	 */
	private static final String METHOD_NAME_REQUEST_LINE = "getRequestLine";

	/**
	 * Apache specific method to get uri.
	 */
	private static final String METHOD_NAME_URI = "getUri";

	/**
	 * Apache specific method to get status line.
	 */
	private static final String METHOD_NAME_STATUS_LINE = "getStatusLine";

	/**
	 * Apache specific method to get status code.
	 */
	private static final String METHOD_NAME_STATUS_CODE = "getStatusCode";

	/**
	 * Apache specific method name to check if inspectIT header is already in place.
	 */
	private static final String METHOD_NAME_GET_HEADER = "getHeaders";

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteApacheHttpClientV40InserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager) {
		super(platformManager, remoteIdentificationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String readURL(Object object, Object[] parameters, Object result) {
		String returnValue = "";
		try {
			Object httpRequest = parameters[1];

			Object requestLine = cache.invokeMethod(httpRequest.getClass(), METHOD_NAME_REQUEST_LINE, METHOD_PARAMETER_EMPTY, httpRequest, null, null);

			Object uri = cache.invokeMethod(requestLine.getClass(), METHOD_NAME_URI, METHOD_PARAMETER_EMPTY, requestLine, null, null);

			returnValue = (String) uri;

			if (LOG.isDebugEnabled()) {
				LOG.debug("URL: " + returnValue);
			}

		} catch (Exception e) {
			LOG.warn("Could not read URL Object from Webrequest. No URL Information available.");
		}

		return returnValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int readResponseCode(Object object, Object[] parameters, Object result) {

		int returnValue = 0;
		try {
			Object statusLine = cache.invokeMethod(result.getClass(), METHOD_NAME_STATUS_LINE, METHOD_PARAMETER_EMPTY, result, null, null);

			Object statusCode = cache.invokeMethod(statusLine.getClass(), METHOD_NAME_STATUS_CODE, METHOD_PARAMETER_EMPTY, statusLine, null, null);

			returnValue = (Integer) statusCode;

			if (LOG.isDebugEnabled()) {
				LOG.debug("ResponseCode: " + returnValue);
			}

		} catch (Exception e) {
			LOG.warn("Could not read response code from Webrequest.");
		}

		return returnValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needToInsertInspectItHeader(Object object, Object[] parameters) {
		try {
			Object httpRequest = parameters[1];

			Object[] inspectITHeader = (Object[]) cache.invokeMethod(httpRequest.getClass(), METHOD_NAME_GET_HEADER, METHOD_PARAMETER_ONE_STRING_FIELD, httpRequest,
					new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER }, null);

			return inspectITHeader == null || inspectITHeader.length == 0;

		} catch (Exception e) {
			LOG.warn("Check of InspectITHeader was not possible.", e);
			return true;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {
		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(identification);

		try {
			Object httpRequest = parameters[1];

			Object result = cache.invokeMethod(httpRequest.getClass(), METHOD_NAME, METHOD_PARAMETER_TWO_STRING_FIELD, httpRequest,
					new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER, inspectItHeader }, Boolean.FALSE);

			if (result instanceof Boolean && result.equals(Boolean.FALSE)) {
				throw new Exception();
			}

			RemoteHttpCallData remoteCallData = new RemoteHttpCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(0);
			this.threadRemoteCallData.set(remoteCallData);

			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader inserted: " + inspectItHeader);
			}
		} catch (Exception e) {
			LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
		}
	}
}
