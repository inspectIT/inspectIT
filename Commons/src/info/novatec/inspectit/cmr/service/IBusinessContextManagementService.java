package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessContextDefinition;
import info.novatec.inspectit.exception.BusinessException;

import java.util.List;

/**
 * Service interface which defines the methods to manage the business context (i.e. applications,
 * business transactions, SLAs, etc.) of invocation sequences.
 * 
 * @author Alexander Wert
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IBusinessContextManagementService {

	/**
	 * Returns the currently existing {@link IBusinessContextDefinition} from the CMR configuration.
	 * 
	 * @return Returns the currently existing {@link IBusinessContextDefinition} from the CMR
	 *         configuration.
	 */
	IBusinessContextDefinition getBusinessContextDefinition();

	/**
	 * Returns an unmodifiable list of all {@link IApplicationDefinition} instances.
	 * 
	 * @return unmodifiable list of all {@link IApplicationDefinition} instances.
	 */
	List<IApplicationDefinition> getApplicationDefinitions();

	/**
	 * Adds application definition to the business context.
	 * 
	 * @param appDefinition
	 *            {@link IApplicationDefinition} instance to add
	 * @throws BusinessException
	 *             if adding {@link IApplicationDefinition} fails.
	 */
	void addApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Adds application definition to the business context. Inserts it to the list before the
	 * element with the passed index.
	 * 
	 * @param appDefinition
	 *            {@link ApplicationDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             if adding {@link IApplicationDefinition} fails.
	 */
	void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException;

	/**
	 * Deletes the {@link ApplicationDefinition} from the business context.
	 * 
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to delete
	 * @throws BusinessException
	 *             if deleting {@link IApplicationDefinition} fails.
	 */
	void deleteApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Moves the {@link ApplicationDefinition} to a different position specified by the index
	 * parameter.
	 * 
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to move
	 * @param index
	 *            position to move the {@link ApplicationDefinition} to
	 * @throws BusinessException
	 *             if moving {@link IApplicationDefinition} fails.
	 */
	void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) throws BusinessException;

	/**
	 * Updates the given {@link IApplicationDefinition}.
	 * 
	 * @param appDefinition
	 *            {@link IApplicationDefinition} to update
	 * @throws BusinessException
	 *             if update of {@link IApplicationDefinition} fails
	 * @throws BusinessException
	 *             if updating {@link IApplicationDefinition} fails.
	 */
	void updateApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException;
}
