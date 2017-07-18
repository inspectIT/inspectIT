package rocks.inspectit.agent.java.core;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMData;

/**
 * Interface definition for the core service. The core service is the central point of the Agent
 * where all data is collected, triggered etc.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 *
 */
public interface ICoreService {

	/**
	 * Start this component.
	 */
	void start();

	/**
	 * Stop this component.
	 */
	void stop();

	/**
	 * Adds the default data to the core service. Depending on the core service implementation this
	 * default data would either be sent to the CMR or further processed.
	 *
	 * @param defaultData
	 *            Default data to add. Must not be <code>null</code>.
	 */
	void addDefaultData(DefaultData defaultData);

	/**
	 * Adds a new end user monitoring record to the value storage to be sent.
	 *
	 * @param eumData
	 *            the data to send, must have a valid session id and element ID!
	 */
	void addEUMData(AbstractEUMData eumData);

}
