package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;

/**
 * Service for management of the agents instrumentation.
 *
 * @author Marius Oehler
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IAgentInstrumentationService {

	/**
	 * Triggers the update process of the instrumentation for the given platforms. If a given
	 * platform has pending instrumentation they are applied. The {@link InstrumentationStatus} of
	 * the given platforms are {@link InstrumentationStatus#UP_TO_DATE} after this method returned.
	 *
	 * @param updatePlatformIds
	 *            {@link Collection} of IDs of {@link PlatformIdent}s to update
	 */
	void updateInstrumentation(Collection<Long> updatePlatformIds);

}
