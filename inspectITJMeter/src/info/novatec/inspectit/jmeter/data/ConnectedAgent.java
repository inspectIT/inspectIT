package info.novatec.inspectit.jmeter.data;

import info.novatec.inspectit.cmr.model.PlatformIdent;

// NOCHKALL
public class ConnectedAgent extends ResultBase {

	public String name;
	public String status;

	public ConnectedAgent(PlatformIdent platformId, String status) {
		super(platformId.getId());
		this.name = platformId.getAgentName();
		this.status = status;
	}
}
