package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.jmeter.data.ConnectedAgent;
import info.novatec.inspectit.jmeter.data.ConnectedAgents;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sampler to get all connected agents.
 * 
 * @author Stefan Siegl
 */
public class InspectITGetConnectedAgents extends InspectITSamplerBase {

	/** Connected agents. */
	private Map<PlatformIdent, AgentStatusData> agentStatus;

	@Override
	public Configuration[] getRequiredConfig() {
		return new Configuration[] {};
	}

	@Override
	public void run() throws Throwable {
		agentStatus = repository.getGlobalDataAccessService().getAgentsOverview();
	}

	@Override
	public InspectITResultMarker getResult() {
		List<ConnectedAgent> agentList = new ArrayList<ConnectedAgent>();
		for (Map.Entry<PlatformIdent, AgentStatusData> entry : agentStatus.entrySet()) {
			if (null == entry.getValue()) {
				// This nice agent is not connected, thank you service documentation for not
				// mentioning that :)
				continue;
			}

			agentList.add(new ConnectedAgent(entry.getKey(), convertAgentStatus(entry.getValue().getAgentConnection())));
		}

		return new ConnectedAgents(agentList);
	}

	/**
	 * Convert agent status to readable format.
	 * 
	 * @param status
	 *            the status.
	 * @return readable format.
	 */
	private String convertAgentStatus(AgentConnection status) {
		if (status.equals(AgentConnection.NEVER_CONNECTED)) {
			return "neverConnected";
		} else if (status.equals(AgentConnection.CONNECTED)) {
			return "connected";
		} else if (status.equals(AgentConnection.DISCONNECTED)) {
			return "disconnected";
		} else {
			return "unknown";
		}
	}

	@Override
	public void setup() {
		// not needed
	}
}
