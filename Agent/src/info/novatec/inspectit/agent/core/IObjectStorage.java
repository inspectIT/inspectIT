package info.novatec.inspectit.agent.core;

import info.novatec.inspectit.communication.DefaultData;

/**
 * The purpose of an object storage is to handle the data from the sensors and create an appropriate
 * data object which can be transmitted to the Repository.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IObjectStorage {

	/**
	 * This method call will finish all pending actions and create the value object. Adding values
	 * after this method is called is not an option!
	 * 
	 * @return Returns a {@link DefaultData} which can be transmitted to the Repository.
	 */
	DefaultData finalizeDataObject();

}
