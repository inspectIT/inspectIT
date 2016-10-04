package rocks.inspectit.shared.cs.cmr.service;

import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Service to query existing alerts.
 *
 * @author Alexander Wert
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IAlertService {

	/**
	 * Retrieves all known alerts.
	 *
	 * @return All known alerts.
	 */
	List<Alert> getAlerts();

	/**
	 * Retrieves all open alerts.
	 *
	 * @return All open alerts.
	 */
	List<Alert> getOpenAlerts();

	/**
	 * Retrieves all closed alerts.
	 *
	 * @return All closed alerts.
	 */
	List<Alert> getClosedAlerts();

	/**
	 * Retrieves all business transaction alerts.
	 *
	 * @return All known business transaction alerts.
	 */
	List<Alert> getBusinessTransactionAlerts();

	/**
	 * Retrieves all open business transaction alerts.
	 *
	 * @return All open business transaction alerts.
	 */
	List<Alert> getOpenBusinessTransactionAlerts();

	/**
	 * Retrieves all closed business transaction alerts.
	 *
	 * @return All closed business transaction alerts.
	 */
	List<Alert> getClosedBusinessTransactionAlerts();

	/**
	 * Retrieves the alert for the given id.
	 *
	 * @param alertId
	 *            The alert id.
	 * @return Returns the alert for the given id or <code>null</code> if no alert for that id
	 *         exists.
	 */
	Alert getAlert(String alertId);
}
