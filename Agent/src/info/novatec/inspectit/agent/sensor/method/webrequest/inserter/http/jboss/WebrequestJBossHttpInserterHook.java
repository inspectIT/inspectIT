package info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.jboss;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.WebrequestDefaultHttpInserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implements the {@link WebrequestJBossHttpInserterHook} class. It puts the InspectIT
 * header as additional header/attribute to the remote call.
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestJBossHttpInserterHook extends WebrequestDefaultHttpInserterHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebrequestJBossHttpInserterHook.class);

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
	public WebrequestJBossHttpInserterHook(IIdManager idManager, Timer timer, RemoteIdentificationManager remoteIdentificationManager, ThreadMXBean threadMXBean) {
		super(idManager, timer, remoteIdentificationManager, threadMXBean);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {

		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(methodId, sensorTypeId, identification);

		try {
			// 4. parameter is HeaderMap
			Object additionalHeaderObject = parameters[3];

			@SuppressWarnings("unchecked")
			Map<String, Object> additionalHeader = (Map<String, Object>) additionalHeaderObject;

			additionalHeader.put(INSPECTIT_HEADER, inspectItHeader);

			RemoteCallData remoteCallData = new RemoteCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(idManager.getPlatformId());
			this.threadRemoteCallData.set(remoteCallData);

		} catch (Exception e) {
			LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String readURL(Object object) {
		// TODO Auto-generated method stub
		return "";

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int readResponseCode(Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

}
