package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Interface for {@link AlertingDefinition} provider. Note that this interface extends the
 * {@link ICmrRepositoryProvider} which in fact denotes to which CMR environment belongs.
 *
 * @author Alexander Wert
 *
 */
public interface IAlertDefinitionProvider extends ICmrRepositoryProvider, Comparable<IAlertDefinitionProvider> {
	/**
	 * Retrieves the alerting definition.
	 *
	 * @return Returns the alerting definition.
	 */
	AlertingDefinition getAlertDefinition();
}
