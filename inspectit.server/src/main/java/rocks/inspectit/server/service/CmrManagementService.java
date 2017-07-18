package rocks.inspectit.server.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.cache.IBuffer;
import rocks.inspectit.server.externalservice.IExternalService;
import rocks.inspectit.server.property.PropertyManager;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.server.util.ShutdownService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.cmr.CmrStatusData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.property.configuration.PropertySection;
import rocks.inspectit.shared.cs.cmr.property.update.configuration.ConfigurationUpdate;
import rocks.inspectit.shared.cs.cmr.service.ICmrManagementService;
import rocks.inspectit.shared.cs.storage.StorageManager;

/**
 * Implementation of the {@link ICmrManagementService}. Provides general management of the CMR.
 *
 * @author Ivan Senic
 *
 */
@Service
public class CmrManagementService implements ICmrManagementService {

	/**
	 * Name of the folder where database is stored.
	 */
	private static final String DATABASE_FOLDER = "db";

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Buffer data dao.
	 */
	@Autowired
	private IBuffer<DefaultData> buffer;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * {@link PropertyManager}.
	 */
	@Autowired
	private PropertyManager propertyManager;

	/**
	 * {@link ShutdownService}.
	 */
	@Autowired
	private ShutdownService shutdownService;

	/**
	 * List of {@link IExternalService}s.
	 */
	@Autowired
	private List<IExternalService> services;

	/**
	 * Time in milliseconds when the CMR has started.
	 */
	private long timeStarted;

	/**
	 * Date when the CMR has started.
	 */
	private Date dateStarted;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restart() {
		shutdownService.restart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() {
		shutdownService.shutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void clearBuffer() {
		buffer.clearAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public CmrStatusData getCmrStatusData() {
		// cmr status data should always report in bytes!
		CmrStatusData cmrStatusData = new CmrStatusData();
		cmrStatusData.setCurrentBufferSize(buffer.getCurrentSize());
		cmrStatusData.setMaxBufferSize(buffer.getMaxSize());
		cmrStatusData.setBufferOldestElement(buffer.getOldestElement());
		cmrStatusData.setBufferNewestElement(buffer.getNewestElement());
		cmrStatusData.setStorageDataSpaceLeft(storageManager.getBytesHardDriveOccupancyLeft());
		cmrStatusData.setStorageMaxDataSpace(storageManager.getMaxBytesHardDriveOccupancy());
		cmrStatusData.setWarnSpaceLeftActive(storageManager.isSpaceWarnActive());
		cmrStatusData.setCanWriteMore(storageManager.canWriteMore());
		cmrStatusData.setUpTime(System.currentTimeMillis() - timeStarted);
		cmrStatusData.setDateStarted(dateStarted);
		cmrStatusData.setDatabaseSize(getDatabaseSize());

		for (IExternalService service : services) {
			cmrStatusData.getExternalServiceStatusMap().put(service.getServiceType(), service.getServiceStatus());
		}

		return cmrStatusData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<PropertySection> getConfigurationPropertySections() {
		return propertyManager.getConfigurationPropertySections();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateConfiguration(ConfigurationUpdate configurationUpdate, boolean executeRestart) throws Exception {
		propertyManager.updateConfiguration(configurationUpdate, executeRestart);
	}

	/**
	 * Returns the {@link Long} holding the size of the database folder or <code>null</code> if
	 * database folder does not exists or calculation of size fails.
	 *
	 * @return Returns the {@link Long} holding the size of the database folder or <code>null</code>
	 *         if database folder does not exists or calculation of size fails.
	 */
	private Long getDatabaseSize() {
		Path databaseFolder = Paths.get(DATABASE_FOLDER);
		if (Files.notExists(databaseFolder) || !Files.isDirectory(databaseFolder)) {
			return null;
		}
		final MutableLong size = new MutableLong();
		try {
			Files.walkFileTree(databaseFolder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					size.add(attrs.size());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			return null;
		}
		return Long.valueOf(size.longValue());
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 *
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		timeStarted = System.currentTimeMillis();
		dateStarted = new Date(timeStarted);
		if (log.isInfoEnabled()) {
			log.info("|-CMR Management Service active...");
		}
	}

}
