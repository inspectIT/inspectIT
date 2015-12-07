package info.novatec.inspectit.cmr.configuration.business;

import info.novatec.inspectit.exception.BusinessException;

import java.io.Serializable;
import java.util.List;

/**
 * The {@link IBusinessTransactionDefinition} holds the information about the business context (such
 * as application, business transaction, etc.).
 *
 * @author Alexander Wert
 *
 */
public interface IBusinessContextDefinition extends Serializable {

	/**
	 * Returns an unmodifiable list of all {@link IApplicationDefinition} instances known in this
	 * business context.
	 *
	 * @return unmodifiable list of all {@link IApplicationDefinition} instances known in this
	 *         business context
	 */
	List<IApplicationDefinition> getApplicationDefinitions();

	/**
	 * Retrieves the {@link IApplicationDefinition} with the given identifier.
	 *
	 * @param id
	 *            unique id identifying the application definition to retrieve
	 * @return Return the {@link IApplicationDefinition} with the given id, or null if no
	 *         application definition with the passed id could be found.
	 * @throws BusinessException
	 *             if an application with the given id does not exist.
	 */
	IApplicationDefinition getApplicationDefinition(long id) throws BusinessException;

	/**
	 * Adds application definition to the business context.
	 *
	 * @param appDefinition
	 *            {@link IApplicationDefinition} instance to add
	 * @throws BusinessException
	 *             if application cannot be added.
	 */
	void addApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Adds application definition to the business context. Inserts it to the list before the
	 * element with the passed index.
	 *
	 * @param appDefinition
	 *            {@link IApplicationDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             if application cannot be added.
	 */
	void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException;

	/**
	 * Updates the given {@link IApplicationDefinition}.
	 *
	 * @param appDefinition
	 *            {@link IApplicationDefinition} to update
	 *
	 * @throws BusinessException
	 *             If update fails.
	 */
	void updateApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Deletes the {@link IApplicationDefinition} from the business context.
	 *
	 * @param appDefinition
	 *            {@link IApplicationDefinition} to delete
	 *
	 * @return Returns true if the business context contained the application
	 */
	boolean deleteApplicationDefinition(IApplicationDefinition appDefinition);

	/**
	 * Moves the {@link IApplicationDefinition} to a different position specified by the index
	 * parameter.
	 *
	 * @param appDefinition
	 *            {@link IApplicationDefinition} to move
	 * @param index
	 *            position to move the {@link IApplicationDefinition} to
	 * @throws BusinessException
	 *             If moving fails.
	 */
	void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) throws BusinessException;

	/**
	 * Returns the default {@link IApplicationDefinition}.
	 *
	 * @return Returns the default {@link IApplicationDefinition}.
	 */
	IApplicationDefinition getDefaultApplicationDefinition();

}
