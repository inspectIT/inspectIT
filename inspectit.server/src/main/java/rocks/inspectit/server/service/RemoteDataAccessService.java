package rocks.inspectit.server.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.RemoteCallDataDao;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IRemoteCallDataAccessService;

/**
 * Service to access the {@link RemoteCallData} in CMR.
 *
 * @author Thomas Kluge
 */
@Service
public class RemoteDataAccessService implements IRemoteCallDataAccessService, InitializingBean {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The Dao.
	 */
	@Autowired
	private RemoteCallDataDao remoteCallDatadao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData) {
		return remoteCallDatadao.getRemoteCallDataOverview(remoteCallData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData, Date fromDate, Date toDate) {
		return remoteCallDatadao.getRemoteCallDataOverview(remoteCallData, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteCallData getRemoteCallData(long platformID, long identification, boolean calling) {
		return remoteCallDatadao.getRemoteCallDataByIdentification(platformID, identification, calling);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Remote Data Access Service active...");
		}
	}
}
