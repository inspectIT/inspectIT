package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.storage.CmrStorageManager;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.storage.recording.RecordingState;

/**
 * Simple {@link AbstractCmrDataProcessor} that passes data to be recorded if recording is "ON" on
 * the CMR.
 * 
 * @author Ivan Senic
 * 
 */
public class RecorderCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link CmrStorageManager}.
	 */
	@Autowired
	CmrStorageManager storageManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		if (storageManager.getRecordingState() == RecordingState.ON) {
			storageManager.record(defaultData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return null != defaultData;
	}

}
