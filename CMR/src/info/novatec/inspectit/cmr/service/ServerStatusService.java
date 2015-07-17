package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link IServerStatusService} interface to provide information about the
 * current status of the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
public class ServerStatusService implements IServerStatusService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The status of the CMR.
	 */
	private ServerStatus status = ServerStatus.SERVER_STARTING;

	/**
	 * The versioning Service.
	 */
	@Autowired
	private IVersioningService versioning;

	/**
	 * We will only log once that the version information can not be obtained, since the UI is
	 * checking this periodically. Even in debug level it is not wanted to be logged all the time.
	 */
	private boolean versionNotFoundLogged = false;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public ServerStatus getServerStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public String getVersion() {
		try {
			return versioning.getVersion();
		} catch (IOException e) {
			if (!versionNotFoundLogged && log.isDebugEnabled()) {
				log.debug("Cannot obtain current version", e);
				versionNotFoundLogged = true;
			}
			return IServerStatusService.VERSION_NOT_AVAILABLE;
		}
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		status = ServerStatus.SERVER_ONLINE;
		status.setRegistrationIdsValidationKey(UUID.randomUUID().toString());

		if (log.isInfoEnabled()) {
			log.info("|-Server Status Service active...");
		}
	}

}
