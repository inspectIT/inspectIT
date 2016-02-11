package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.rcp.model.AgentLeaf;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester for {@link AgentLeaf}.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AgentLeaf) {
			AgentLeaf agentLeaf = (AgentLeaf) receiver;

			if ("canDelete".equals(property)) {
				return null == agentLeaf.getAgentStatusData() || agentLeaf.getAgentStatusData().getAgentConnection() != AgentConnection.CONNECTED;
			}
		}
		return false;
	}

}
