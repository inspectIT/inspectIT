package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for all CMR services.
 * 
 * @author Ivan Senic
 * 
 */
public interface ICmrService {

	/**
	 * Returns {@link CmrRepositoryDefinition} that service is bounded to.
	 * 
	 * @return Returns {@link CmrRepositoryDefinition} that service is bounded to.
	 */
	CmrRepositoryDefinition getCmrRepositoryDefinition();

	/**
	 * Initializes the service.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	void initService(CmrRepositoryDefinition cmrRepositoryDefinition);

	/**
	 * Returns the service object.
	 * 
	 * @return Returns the service object.
	 */
	Object getService();

	/**
	 * Defines if the default value should be returned when communication errors occurs in the
	 * invocation of the service. What will be the default value is defined in the
	 * {@link info.novatec.inspectit.rcp.repository.service.cmr.proxy.InterceptorUtils#getDefaultReturnValue(org.aopalliance.intercept.MethodInvocation)}
	 * 
	 * @return <code>true</code> if default value should be returned
	 */
	boolean isDefaultValueOnError();
}
