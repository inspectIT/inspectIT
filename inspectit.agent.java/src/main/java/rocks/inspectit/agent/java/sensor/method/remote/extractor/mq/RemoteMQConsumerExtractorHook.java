package rocks.inspectit.agent.java.sensor.method.remote.extractor.mq;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteDefaultInserterHook;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * The hook implements the {@link RemoteDefaultInserterHook} class for Message Queue. It puts the
 * InspectIT header as additional header/attribute to the Message. The hook invokes the methode
 * {@link #METHOD_NAME} to add the header attribute.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteMQConsumerExtractorHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteMQConsumerExtractorHook.class);

	/**
	 * The ID manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The extractor.
	 */
	private final RemoteMQParameterExtractor extractor;

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param extractor
	 *            The Extractor
	 */
	public RemoteMQConsumerExtractorHook(IPlatformManager platformManager, RemoteMQParameterExtractor extractor) {
		this.platformManager = platformManager;
		this.extractor = extractor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// nothing

	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// nothing

	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// extract InspectItHeader Informations
		Object message = result;

		RemoteCallData data = extractor.getRemoteCallData(message);

		// just save data if insptectItHeader is available, it makes no sense without the header
		if (data != null) {
			try {
				long platformId = platformManager.getPlatformId();

				data.setPlatformIdent(platformId);
				data.setMethodIdent(methodId);
				data.setSensorTypeIdent(sensorTypeId);
				data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

				// returning gathered information
				coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.nanoTime()), data);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the remote call data because of an unavailable id. " + e.getMessage());
				}
			}
		}
	}

}
