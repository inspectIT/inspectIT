package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.version.VersionService;

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
	 * {@link VersionService}.
	 */
	@Autowired
	VersionService versionService;

	/**
	 * The status of the CMR.
	 */
	private ServerStatus status = ServerStatus.SERVER_STARTING;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public ServerStatus getServerStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public String getVersion() {
		return versionService.getVersionAsString();
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
