package rocks.inspectit.server.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.server.util.Converter;
import rocks.inspectit.shared.all.cmr.service.IAgentStorageService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * The default implementation of the {@link IAgentStorageService} interface. Uses an implementation
 * of the {@link DefaultDataDao} interface to save and retrieve the data objects from the database.
 *
 * @author Patrice Bouillet
 *
 */
@Service
public class AgentStorageService implements IAgentStorageService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The default data DAO.
	 */
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider platformIdentDateSaver;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void addDataObjects(final List<? extends DefaultData> dataObjects) {
		if (CollectionUtils.isNotEmpty(dataObjects)) {
			platformIdentDateSaver.registerDataSent(dataObjects.get(0).getPlatformIdent());

			long time = 0;
			if (log.isDebugEnabled()) {
				time = System.nanoTime();
			}

			defaultDataDao.saveAll(dataObjects);

			if (log.isDebugEnabled()) {
				log.debug("Data Objects count: " + dataObjects.size() + " Save duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
			}
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
		if (log.isInfoEnabled()) {
			log.info("|-Agent Storage Service active...");
		}

	}

}
