package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.RemoteCallDataDao;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData) {
		return remoteCallDatadao.getRemoteCallDataOverview(remoteCallData);
	}

	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData, Date fromDate, Date toDate) {
		return remoteCallDatadao.getRemoteCallDataOverview(remoteCallData, fromDate, toDate);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Remote Data Access Service active...");
		}
	}

	@Override
	public RemoteCallData getRemoteCallData(long platformID, long identification, boolean calling) {
		return remoteCallDatadao.getRemoteCallDataByIdentification(platformID, identification, calling);
	}

}
