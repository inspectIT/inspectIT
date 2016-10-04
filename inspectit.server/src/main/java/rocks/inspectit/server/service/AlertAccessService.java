package rocks.inspectit.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.alerting.AlertRegistry;
import rocks.inspectit.shared.cs.cmr.service.IAlertService;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Access service for alerts.
 *
 * @author Alexander Wert
 *
 */
@Service
public class AlertAccessService implements IAlertService {

	/**
	 * Alert registry.
	 */
	@Autowired
	AlertRegistry alertRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getAlerts() {
		return alertRegistry.getAlerts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getOpenAlerts() {
		return filterAlerts(getAlerts(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getClosedAlerts() {
		return filterAlerts(getAlerts(), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getBusinessTransactionAlerts() {
		return alertRegistry.getBusinessTransactionAlerts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getOpenBusinessTransactionAlerts() {
		return filterAlerts(getBusinessTransactionAlerts(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Alert> getClosedBusinessTransactionAlerts() {
		return filterAlerts(getBusinessTransactionAlerts(), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Alert getAlert(String alertId) {
		return alertRegistry.getAlert(alertId);
	}

	/**
	 * Filters open / closed alerts depending on the given boolean indicator.
	 *
	 * @param alerts
	 *            Alerts to filter.
	 * @param open
	 *            Indicator whether open alerts should stay or closed. If true, the results will
	 *            contain only open alerts, otherwise only closed alerts.
	 * @return Filtered list of alerts.
	 */
	private List<Alert> filterAlerts(List<Alert> alerts, boolean open) {
		List<Alert> filteredAlerts = new ArrayList<>();

		for (Alert alert : alerts) {
			if (alert.isOpen() == open) {
				filteredAlerts.add(alert);
			}
		}

		return filteredAlerts;
	}
}
