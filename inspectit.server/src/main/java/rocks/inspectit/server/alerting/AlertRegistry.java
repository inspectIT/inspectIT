package rocks.inspectit.server.alerting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.FifoMap;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * The registry for business transaction alerts.
 *
 * @author Alexander Wert
 *
 */
@Component
public class AlertRegistry {
	/**
	 * The capacity of the registry buffer.
	 */
	private static final int BUFFER_SIZE = 100000;
	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * The registry buffer implemented as a {@link FifoMap}.
	 */
	private final FifoMap<String, Alert> registry = new FifoMap<>(BUFFER_SIZE);

	/**
	 * Returns the {@link Alert} for the given id.
	 *
	 * @param alertId
	 *            the alert identifier.
	 * @return Returns the {@link Alert} for the given id or <code>null</code> if Alert for the
	 *         given id cannot be found.
	 */
	public Alert getAlert(String alertId) {
		return registry.get(alertId);
	}

	/**
	 * Registers the given alert. If the passed {@link Alert} does not refer to the business
	 * transaction metric, then the registration fails.
	 *
	 * @param alert
	 *            the {@link Alert} to register
	 */
	public void registerAlert(Alert alert) {
		registry.put(alert.getId(), alert);
	}

	/**
	 * Returns all alerts in the registry.
	 * 
	 * @return Returns all alerts in the registry.
	 */
	public List<Alert> getAlerts() {
		return new ArrayList<>(registry.values());
	}

	/**
	 * Returns all business transaction alerts in the registry.
	 * 
	 * @return Returns all business transaction alerts in the registry.
	 */
	public List<Alert> getBusinessTransactionAlerts() {
		List<Alert> resultList = new ArrayList<>();
		for (Alert alert : getAlerts()) {
			if (AlertingUtils.isBusinessTransactionAlert(alert.getAlertingDefinition())) {
				resultList.add(alert);
			}
		}
		return resultList;
	}
}
