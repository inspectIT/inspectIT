package rocks.inspectit.agent.java.connection.impl;

import java.util.Map;

import rocks.inspectit.agent.java.connection.AbstractRemoteMethodCall;
import rocks.inspectit.shared.all.cmr.service.IAgentService;

/**
 * {@link AbstractRemoteMethodCall} for the {@link IAgentService#instrumentationApplied(long, Map)}.
 *
 * @author Ivan Senic
 *
 */
public class InstrumentationAppliedCall extends AbstractRemoteMethodCall<IAgentService, Void> {

	/**
	 * Platform id.
	 */
	private final long platformIdent;

	/**
	 * Map containing method id as key and applied sensor IDs.
	 */
	private final Map<Long, long[]> methodToSensorMap;

	/**
	 * Default constructor.
	 *
	 * @param remoteObject
	 *            {@link IAgentService} object
	 * @param platformIdent
	 *            Platform id.
	 * @param methodToSensorMap
	 *            map containing method id as key and applied sensor IDs
	 */
	public InstrumentationAppliedCall(IAgentService remoteObject, long platformIdent, Map<Long, long[]> methodToSensorMap) {
		super(remoteObject);
		this.platformIdent = platformIdent;
		this.methodToSensorMap = methodToSensorMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void performRemoteCall(IAgentService remoteObject) throws Exception {
		remoteObject.instrumentationApplied(platformIdent, methodToSensorMap);
		return null;
	}

}
