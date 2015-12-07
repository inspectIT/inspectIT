/**
 *
 */
package rocks.inspectit.ui.rcp.provider;

import java.util.List;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;

/**
 * @author Alexander Wert
 *
 */
public interface IApplicationProvider extends ICmrRepositoryProvider {
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
