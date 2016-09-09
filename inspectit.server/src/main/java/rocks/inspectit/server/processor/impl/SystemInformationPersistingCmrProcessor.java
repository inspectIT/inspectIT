package rocks.inspectit.server.processor.impl;

import java.util.Collections;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;

/**
 * Persisting processor for the {@link SystemInformationData} as this one will be saved always to
 * the database, even when influx is active as it's only sent once from the agent.
 *
 * @author Ivan Senic
 *
 */
public class SystemInformationPersistingCmrProcessor extends PersistingCmrProcessor {

	/**
	 * Default constructor.
	 */
	public SystemInformationPersistingCmrProcessor() {
		super(Collections.<Class<? extends DefaultData>> emptyList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof SystemInformationData;
	}

}
