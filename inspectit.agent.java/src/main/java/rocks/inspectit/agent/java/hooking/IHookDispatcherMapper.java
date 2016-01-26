package rocks.inspectit.agent.java.hooking;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;

/**
 * Extended version of the {@link IHookDispatcher} to over come problems of loading the
 * {@link RegisteredSensorConfig} class.
 * 
 * @author Ivan Senic
 * 
 */
public interface IHookDispatcherMapper {

	/**
	 * Adds a method or constructor mapping to the dispatcher.
	 * 
	 * @param id
	 *            The id of the mapping.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object of this mapping.
	 */
	void addMapping(long id, RegisteredSensorConfig rsc);
}
