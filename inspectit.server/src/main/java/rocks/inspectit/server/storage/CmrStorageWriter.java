package rocks.inspectit.server.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.dao.impl.PlatformIdentDaoImpl;
import rocks.inspectit.server.service.BusinessContextManagementService;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.TimeFrame;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.shared.cs.storage.StorageWriter;
import rocks.inspectit.shared.cs.storage.label.ObjectStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.impl.DataTimeFrameLabelType;

/**
 * {@link StorageWriter} implementation for the CMR.
 *
 * @author Ivan Senic
 *
 */
@Component("cmrStorageWriter")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class CmrStorageWriter extends StorageWriter {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Set of involved Agents, used after recording to store proper Agent information.
	 */
	private Set<Long> involvedAgentsSet = new HashSet<>();

	/**
	 * {@link AtomicLong} holding the time-stamp value of the oldest data written in the storage.
	 */
	private final AtomicLong oldestDataTimestamp = new AtomicLong(Long.MAX_VALUE);

	/**
	 * {@link AtomicLong} holding the time-stamp value of the newest data written in the storage.
	 */
	private final AtomicLong newestDataTimestamp = new AtomicLong(0);

	/**
	 * Platform ident dao.
	 */
	@Autowired
	private PlatformIdentDaoImpl platformIdentDao;

	/**
	 * {@link BusinessContextManagementService}.
	 */
	@Autowired
	private IBusinessContextManagementService businessContextService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<Void> write(DefaultData defaultData) {
		Future<Void> future = super.write(defaultData);
		postWriteOperations(defaultData);
		return future;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<Void> write(DefaultData defaultData, Map<?, ?> kryoPreferences) {
		Future<Void> future = super.write(defaultData, kryoPreferences);
		postWriteOperations(defaultData);
		return future;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	protected void writeAgentData() throws IOException {
		List<PlatformIdent> involvedPlatformIdents = platformIdentDao.findAllInitialized(involvedAgentsSet);
		for (PlatformIdent agent : involvedPlatformIdents) {
			super.writeNonDefaultDataObject(agent, agent.getId() + StorageFileType.AGENT_FILE.getExtension());
		}
	}

	/**
	 * Writes current {@link BusinessContextDefinition} data of the corresponding CMR to storage.
	 *
	 * @throws IOException
	 *             thrown if storing business context fails
	 */
	protected void writeBusinessContextData() throws IOException {
		Collection<BusinessTransactionData> businessTransactions = businessContextService.getBusinessTransactions();
		super.writeNonDefaultDataObject(businessTransactions, StorageFileType.BUSINESS_CONTEXT_FILE.getDefaultFileName() + StorageFileType.BUSINESS_CONTEXT_FILE.getExtension());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void finalizeWrite() {
		try {
			writeAgentData();
			writeBusinessContextData();
		} catch (IOException e) {
			log.error("Exception trying to write agent data to disk.", e);
		}
		super.finalizeWrite();

		if ((newestDataTimestamp.get() > 0) && (oldestDataTimestamp.get() < Long.MAX_VALUE)) {
			TimeFrame timeFrame = new TimeFrame(new Date(oldestDataTimestamp.get()), new Date(newestDataTimestamp.get()));
			ObjectStorageLabel<TimeFrame> timeframeLabel = new ObjectStorageLabel<>(timeFrame, new DataTimeFrameLabelType());
			getStorageData().addLabel(timeframeLabel, true);
		}
	}

	/**
	 * Executes post write operations:
	 *
	 * <ul>
	 * <li>Remembers the platform id of the written data
	 * <li>Updates the {@link #newestDataTimestamp} and the {@link #oldestDataTimestamp} if needed.
	 * </ul>
	 *
	 * @param defaultData
	 *            {@link DefaultData} that has been written.
	 */
	private void postWriteOperations(DefaultData defaultData) {
		involvedAgentsSet.add(defaultData.getPlatformIdent());

		while (true) {
			long oldestData = oldestDataTimestamp.get();
			if (oldestData > defaultData.getTimeStamp().getTime()) {
				if (oldestDataTimestamp.compareAndSet(oldestData, defaultData.getTimeStamp().getTime())) {
					break;
				}
			} else {
				break;
			}
		}

		while (true) {
			long newestData = newestDataTimestamp.get();
			if (newestData < defaultData.getTimeStamp().getTime()) {
				if (newestDataTimestamp.compareAndSet(newestData, defaultData.getTimeStamp().getTime())) {
					break;
				}
			} else {
				break;
			}
		}
	}

	/**
	 * @param platformIdentDao
	 *            the platformIdentDao to set
	 */
	public void setPlatformIdentDao(PlatformIdentDaoImpl platformIdentDao) {
		this.platformIdentDao = platformIdentDao;
	}

}
