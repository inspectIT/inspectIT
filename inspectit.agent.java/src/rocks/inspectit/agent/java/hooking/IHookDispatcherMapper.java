package info.novatec.inspectit.agent.hooking;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;

/**
 * Extended version of the {@link IHookDispatcher} to over come problems of loading the
 * {@link RegisteredSensorConfig} class.
 * 
 * @author Ivan Senic
 * 
 */
public interface IHookDispatcherMapper {

	/**
	 * Adds a method mapping to the dispatcher.
	 * 
	 * @param id
	 *            The id of the mapping.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object of this mapping.
	 */
	void addMethodMapping(long id, RegisteredSensorConfig rsc);

	/**
	 * Adds a constructor mapping to the dispatcher.
	 * 
	 * @param id
	 *            The id of the mapping.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object of this mapping.
	 */
	void addConstructorMapping(long id, RegisteredSensorConfig rsc);
}
