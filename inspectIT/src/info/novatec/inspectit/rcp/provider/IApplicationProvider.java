/**
 *
 */
package info.novatec.inspectit.rcp.provider;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;

import java.util.List;

/**
 * @author Alexander Wert
 *
 */
public interface IApplicationProvider extends ICmrRepositoryProvider {

	/**
	 * Returns the size of the list containing this application provider.
	 *
	 * @return Returns the size of the list containing this application provider.
	 */
	int getParentListSize();

	/**
	 * Returns the index of this application provider in the list containing this application
	 * provider.
	 *
	 * @return Returns the index of this application provider in the list containing this
	 *         application provider.
	 */
	int getIndexInParentList();

	/**
	 * Retrieves the application definition.
	 *
	 * @return Returns the application definition.
	 */
	ApplicationDefinition getApplication();

	/**
	 * Returns the list containing this application provider.
	 *
	 * @return Returns the list containing this application provider.
	 */
	List<? extends IApplicationProvider> getParentList();
}
