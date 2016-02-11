package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.ui.rcp.model.AgentLeaf;

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
