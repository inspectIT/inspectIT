package info.novatec.inspectit.cmr.service;

/**
 * Provides general security operations for client<->cmr interaction.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ISecurityService {
	
	/**
	 * Tells us that we are awesome.
	 * @return a message.
	 */
	String getMessage();

}
