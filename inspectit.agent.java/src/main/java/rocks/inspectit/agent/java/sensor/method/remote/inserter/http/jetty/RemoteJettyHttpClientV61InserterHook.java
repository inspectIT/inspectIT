package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty;

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
 * The methodes {@link #METHOD_NAME_URI} and {@link #METHOD_NAME_GET_STATUS} are used to extraced
 * the called URL and the Response Code of the Request.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteJettyHttpClientV61InserterHook extends RemoteHttpInserterHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteJettyHttpClientV61InserterHook.class);

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Jetty HttpConnection specific method name to get RequestFields.
	 */
	private static final String METHOD_NAME_GET_REQUEST_FIELDS = "getRequestFields";

	/**
	 * Jetty HttpConnection specific method name to add inspectIT header.
	 */
	private static final String METHOD_NAME = "addRequestHeader";

	/**
	 * Jetty HttpConnection specific method to get uri.
	 */
	private static final String METHOD_NAME_URI = "getURI";

	/**
	 * Jetty HttpConnection specific method name to check if inspectIT header is already in place.
	 */
	private static final String METHOD_NAME_GET_STRING_FIELD = "getStringField";

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteJettyHttpClientV61InserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager) {
		super(platformManager, remoteIdentificationManager);
	}

	@Override
	protected boolean needToInsertInspectItHeader(Object object, Object[] parameters) {
		try {
			Object httpExchange = parameters[0];

			Object httpFields = cache.invokeMethod(httpExchange.getClass(), METHOD_NAME_GET_REQUEST_FIELDS, METHOD_PARAMETER_EMPTY, httpExchange, null, null);

			String inspectITHeader = (String) cache.invokeMethod(httpFields.getClass(), METHOD_NAME_GET_STRING_FIELD, METHOD_PARAMETER_ONE_STRING_FIELD, httpFields,
					new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER }, null);

			return inspectITHeader == null;

		} catch (Exception e) {
			LOG.warn("Check of InspectITHeader was not possible.", e);
			return true;
		}
	}

	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {
		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(identification);

		try {
			Object httpExchange = parameters[0];

			Object result = cache.invokeMethod(httpExchange.getClass(), METHOD_NAME, METHOD_PARAMETER_TWO_STRING_FIELD, httpExchange,
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

	@Override
	protected int readResponseCode(Object object, Object[] parameters, Object result) {
		LOG.warn("Cannot read response code since is asynchronious connection. Currently not supportet.");
		return 0;
	}

	@Override
	protected String readURL(Object object, Object[] parameters, Object result) {
		String returnValue = "";
		try {
			Object httpExchange = parameters[0];

			returnValue = (String) cache.invokeMethod(httpExchange.getClass(), METHOD_NAME_URI, METHOD_PARAMETER_EMPTY, httpExchange, null, null);

			if (LOG.isDebugEnabled()) {
				LOG.debug("URL: " + returnValue);
			}

		} catch (Exception e) {
			LOG.warn("Could not read URL Object from Webrequest. No URL Information available.");
		}

		return returnValue;
	}

}
