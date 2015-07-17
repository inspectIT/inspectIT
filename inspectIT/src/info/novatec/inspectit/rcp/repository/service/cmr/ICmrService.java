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
}
