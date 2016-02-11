package info.novatec.inspectit.agent.config;

/**
 * Defines an interface for the configuration files. Every configuration syntax (plain text, XML,
 * ...) should have its own implementation.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IConfigurationReader {

	/**
	 * Loads the configuration of this reader from the underlying storage.
	 * 
	 * @throws ParserException
	 *             Thrown if there was an exception caught when loading the configuration.
	 */
	void load() throws ParserException;

}
