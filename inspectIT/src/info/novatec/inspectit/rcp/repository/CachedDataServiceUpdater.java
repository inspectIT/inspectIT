/**
 *
 */
package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.listener.IApplicationDefinitionChangeListener;

/**
 * The {@link CachedDataServiceUpdater} is responsible for notifying a {@link CachedDataService} if
 * the cache has been invalidated.
 *
 * @author Alexander Wert
 *
 */
public class CachedDataServiceUpdater implements IApplicationDefinitionChangeListener {

	/**
	 * The {@link CachedDataService} to update.
	 */
	private final CachedDataService cachedDataService;

	/**
	 * Default constructor.
	 *
	 * @param cachedDataService
	 *            The {@link CachedDataService} to update.
	 */
	public CachedDataServiceUpdater(CachedDataService cachedDataService) {
		this.cachedDataService = cachedDataService;
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addApplicationDefinitionChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationCreated(IApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition) {
		cachedDataService.invalidateBusinessContextCache();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationMoved(IApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition) {
		cachedDataService.invalidateBusinessContextCache();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationUpdated(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		cachedDataService.invalidateBusinessContextCache();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationDeleted(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		cachedDataService.invalidateBusinessContextCache();
	}

}
