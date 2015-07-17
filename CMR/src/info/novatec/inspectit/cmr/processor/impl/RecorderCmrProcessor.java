package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.recording.RecordingState;

import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

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
	protected void processData(DefaultData defaultData, StatelessSession session) {
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
