package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.cmr.dao.impl.PlatformIdentDaoImpl;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.StorageFileType;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.label.ObjectStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.DataTimeFrameLabelType;
import info.novatec.inspectit.util.TimeFrame;

import java.io.IOException;
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
	private Set<Long> involvedAgentsSet = new HashSet<Long>();

	/**
	 * {@link AtomicLong} holding the time-stamp value of the oldest data written in the storage.
	 */
	private AtomicLong oldestDataTimestamp = new AtomicLong(Long.MAX_VALUE);

	/**
	 * {@link AtomicLong} holding the time-stamp value of the newest data written in the storage.
	 */
	private AtomicLong newestDataTimestamp = new AtomicLong(0);

	/**
	 * Platform ident dao.
	 */
	@Autowired
	private PlatformIdentDaoImpl platformIdentDao;

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
			super.writeNonDefaultDataObject(agent, agent.getId() + StorageFileType.AGENT_FILE.getExtension(), false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void finalizeWrite() {
		try {
			writeAgentData();
		} catch (IOException e) {
			log.error("Exception trying to write agent data to disk.", e);
		}
		super.finalizeWrite();

		if (newestDataTimestamp.get() > 0 && oldestDataTimestamp.get() < Long.MAX_VALUE) {
			TimeFrame timeFrame = new TimeFrame(new Date(oldestDataTimestamp.get()), new Date(newestDataTimestamp.get()));
			ObjectStorageLabel<TimeFrame> timeframeLabel = new ObjectStorageLabel<TimeFrame>(timeFrame, new DataTimeFrameLabelType());
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
