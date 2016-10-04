package rocks.inspectit.server.alerting.state;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * The registry for alerting states.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AlertingStateRegistry {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * The registry buffer implemented as a {@link HashMap}.
	 */
	private final Map<String, AlertingState> alertingStateMap = new HashMap<>();

	/**
	 * Registers an {@link AlertingDefinition} and creates and associated {@link AlertingState}
	 * which is returned.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} to register
	 * @return {@link AlertingState} associated with the registered {@link AlertingDefinition}.
	 */
	public AlertingState register(AlertingDefinition alertingDefinition) {
		if (alertingStateMap.containsKey(alertingDefinition.getId())) {
			// TODO already registered
			throw new RuntimeException();
		} else {
			AlertingState alertingState = new AlertingState(alertingDefinition);

			alertingStateMap.put(alertingDefinition.getId(), alertingState);
			return alertingState;
		}
	}

	/**
	 * Returns the {@link AlertingState} related to the given {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            the {@AlertingDefinition}
	 * @return {@link AlertingState} associated with the give {@link AlertingDefinition}
	 */
	public AlertingState get(AlertingDefinition alertingDefinition) {
		return alertingStateMap.get(alertingDefinition.getId());
	}
}
