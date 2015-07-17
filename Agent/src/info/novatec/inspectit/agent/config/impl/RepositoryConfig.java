package info.novatec.inspectit.agent.config.impl;

/**
 * Class used by the {@link ConfigurationStorage} implementation to store the information of a
 * repository.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RepositoryConfig {

	/**
	 * The host name / ip.
	 */
	private String host;

	/**
	 * The port of the host.
	 */
	private int port;

	/**
	 * Default constructor accepting two parameters.
	 * 
	 * @param host
	 *            The host name / ip.
	 * @param port
	 *            The port of the host.
	 */
	public RepositoryConfig(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Returns the name / ip of the host.
	 * 
	 * @return The name / ip of the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port of the host.
	 * 
	 * @return The port of the host.
	 */
	public int getPort() {
		return port;
	}

}
