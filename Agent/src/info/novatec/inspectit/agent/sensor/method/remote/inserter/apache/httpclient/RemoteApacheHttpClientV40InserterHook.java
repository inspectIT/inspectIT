package info.novatec.inspectit.agent.sensor.method.remote.inserter.apache.httpclient;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.sensor.method.remote.RemoteConstant;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteDefaultHttpInserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.ReflectionCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implements the {@link RemoteDefaultHttpInserterHook} class for the . It puts the InspectIT
 * header as additional header/attribute to the remote call. The hook invokes the methode
 * {@link #METHOD_NAME} to add the header attribute. 
 * 
 * 
 * The methodes {@link #METHOD_NAME_URL} and
 * {@link #METHOD_NAME_RESPONSE_CODE} are used to extraced the called URL and the Response Code of
 * the Request.
 * 
 * @author Thomas Kluge
 * 
 */
public class RemoteApacheHttpClientV40InserterHook extends RemoteDefaultHttpInserterHook {

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
	 * Apache	 specific method signature to add InspechtITHeader.
	 */
	private static final Class<?>[] METHOD_PARAMETER = new Class<?>[] { String.class, String.class };

	/**
	 * Empty method signature.
	 */
	private static final Class<?>[] METHOD_PARAMETER_EMPTY = new Class<?>[] {};

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Constructor.
	 * 
	 * 
	 * 
	 * @param idManager
	 *            The id manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteApacheHttpClientV40InserterHook(IIdManager idManager, RemoteIdentificationManager remoteIdentificationManager) {
		super(idManager, remoteIdentificationManager);
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
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {
		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(identification);

		try {
			Object httpRequest = parameters[1];
			
			cache.invokeMethod(httpRequest.getClass(), METHOD_NAME, METHOD_PARAMETER, httpRequest, new Object[] { RemoteConstant.INSPECTIT_HTTP_HEADER, inspectItHeader }, null);

			RemoteCallData remoteCallData = new RemoteCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(idManager.getPlatformId());
			this.threadRemoteCallData.set(remoteCallData);

			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader inserted: " + inspectItHeader);
			}
		} catch (Exception e) {
			LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
		}
	}
}
