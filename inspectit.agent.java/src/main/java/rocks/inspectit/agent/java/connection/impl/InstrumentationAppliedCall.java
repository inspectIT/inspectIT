package rocks.inspectit.agent.java.connection.impl;

import java.util.Map;

import rocks.inspectit.agent.java.connection.AbstractRemoteMethodCall;
import rocks.inspectit.shared.all.cmr.service.IAgentService;

/**
 * {@link AbstractRemoteMethodCall} for the {@link IAgentService#instrumentationApplied(Map)}.
 *
 * @author Ivan Senic
 *
 */
public class InstrumentationAppliedCall extends AbstractRemoteMethodCall<IAgentService, Void> {

	/**
	 * Map containing method id as key and applied sensor IDs.
	 */
	private final Map<Long, long[]> methodToSensorMap;

	/**
	 * Default constructor.
	 *
	 * @param remoteObject
	 *            {@link IAgentService} object
	 * @param methodToSensorMap
	 *            map containing method id as key and applied sensor IDs
	 */
	public InstrumentationAppliedCall(IAgentService remoteObject, Map<Long, long[]> methodToSensorMap) {
		super(remoteObject);

		this.methodToSensorMap = methodToSensorMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void performRemoteCall(IAgentService remoteObject) throws Exception {
		remoteObject.instrumentationApplied(methodToSensorMap);
		return null;
	}

}
