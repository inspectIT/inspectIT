package info.novatec.inspectit.jmeter.data;

import java.util.List;

// NOCHKALL
public class ConnectedAgents implements InspectITResultMarker {

	private List<ConnectedAgent> connectedAgents; // NOPMD: Cannot change as tests already depend on
													// that.

	public ConnectedAgents(List<ConnectedAgent> agents) {
		super();
		this.connectedAgents = agents;
	}

	public List<ConnectedAgent> getAgents() {
		return connectedAgents;
	}

	public void setAgents(List<ConnectedAgent> agents) {
		this.connectedAgents = agents;
	}

}
