package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.VmArgumentData;

import java.util.Set;

import org.hibernate.StatelessSession;

/**
 * Processor that correctly saves the {@link SystemInformationData} to the database.
 * 
 * @author Ivan Senic
 * 
 */
public class SystemInformationDataCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		SystemInformationData info = (SystemInformationData) defaultData;
		long systemInformationId = ((Long) session.insert(info)).longValue();
		Set<VmArgumentData> vmSet = info.getVmSet();
		for (VmArgumentData argumentData : vmSet) {
			argumentData.setSystemInformationId(systemInformationId);
			session.insert(argumentData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof SystemInformationData;
	}

}
