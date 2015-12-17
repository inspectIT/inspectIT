package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.IJmxDataAccessService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;

/**
 * The interface to the repository definition. A repository can be anywhere and anything, the
 * implementation will provide the details on how to access the information.
 * 
 * @author Patrice Bouillet
 */
public interface RepositoryDefinition {

	/**
	 * Returns the IP of the definition.
	 * 
	 * @return The IP.
	 */
	String getIp();

	/**
	 * Returns the port of the definition.
	 * 
	 * @return The port.
	 */
	int getPort();

	/**
	 * Returns the repository symbolic name.
	 * 
	 * @return Returns the repository symbolic name.
	 */
	String getName();

	/**
	 * Returns the invocation data access service for this repository definition.
	 * 
	 * @return The invocation data access service.
	 */
	IInvocationDataAccessService getInvocationDataAccessService();

	/**
	 * Returns the sql data access service for this repository definition.
	 * 
	 * @return The sql data access service.
	 */
	ISqlDataAccessService getSqlDataAccessService();

	/**
	 * Returns the exception data access service for this repository definition.
	 * 
	 * @return The exception data access service.
	 */
	IExceptionDataAccessService getExceptionDataAccessService();

	/**
	 * Returns the global data access service for this repository definition.
	 * 
	 * @return The global data access service.
	 */
	ICachedDataService getCachedDataService();

	/**
	 * Returns the timer data access service for this repository definition.
	 * 
	 * @return The timer data access service.
	 */
	ITimerDataAccessService getTimerDataAccessService();

	/**
	 * Returns the http timer data access service for this repository definition.
	 * 
	 * @return The http timer data access service.
	 */
	IHttpTimerDataAccessService getHttpTimerDataAccessService();

	/**
	 * Returns the {@link IGlobalDataAccessService}.
	 * 
	 * @return Returns the {@link IGlobalDataAccessService}.
	 */
	IGlobalDataAccessService getGlobalDataAccessService();
	
	/**
	 * Returns the {@link IJmxDataAccessService}. 
	 * 
	 * @return Returns the {@link IJmxDataAccessService}.
	 */
	IJmxDataAccessService getJmxDataAccessService();
}