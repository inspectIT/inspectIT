package rocks.inspectit.server.alerting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.alerting.state.AlertingStateRegistry;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

/**
 * Scheduler for the {@link ThresholdChecker} to check the threshold specified by the
 * {@link AlertingDefinition}.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AlertingScheduler implements InitializingBean, Runnable {

	/**
	 * The execution interval of this runnable.
	 */
	private static final long CHECK_INTERVAL = 1L;

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	/**
	 * Activation state of this service.
	 */
	@Value("${alerting.active}")
	boolean active;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * {@link IConfigurationInterfaceService} implementation.
	 */
	@Autowired
	private IConfigurationInterfaceService configurationInterfaceService;

	/**
	 * {@link ThresholdChecker} instance.
	 */
	@Autowired
	private ThresholdChecker thresholdChecker;

	/**
	 * {@link AlertingStateRegistry} instance.
	 */
	@Autowired
	private AlertingStateRegistry alertingStateRegistry;

	/**
	 * {@link ScheduledFuture} of the currently executed {@link AlertingScheduler}.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * Map to keep track of the relation between alerting definition id and its iteration count.
	 */
	private final Map<String, Integer> iterationCounter = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		updateState();

		if (log.isInfoEnabled()) {
			log.info("|-Alerting scheduler initialized..");
		}
	}

	/**
	 * Updates the state of this {@link AlertingDefinition} instance. It is getting enabled or
	 * disabled according to the {@link #active} field.
	 */
	@PropertyUpdate(properties = { "alerting.active" })
	public void updateState() {
		if (active) {
			if ((scheduledFuture == null) || scheduledFuture.isDone()) {
				scheduledFuture = executorService.scheduleAtFixedRate(this, 0L, CHECK_INTERVAL, TimeUnit.MINUTES);
			}
		} else {
			if (scheduledFuture != null) {
				scheduledFuture.cancel(false);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (log.isDebugEnabled()) {
			log.debug("|-Checking alert definitions...");
		}

		List<AlertingDefinition> alertingDefinitions = configurationInterfaceService.getAlertingDefinitions();

		for (AlertingDefinition alertingDefinition : alertingDefinitions) {
			String id = alertingDefinition.getId();

			int currentCount;
			if (!iterationCounter.containsKey(id)) {
				iterationCounter.put(id, 0);
				currentCount = 0;
			} else {
				currentCount = iterationCounter.get(id);
			}

			if (currentCount >= alertingDefinition.getTimerange()) {
				try {
					AlertingState alertingState = alertingStateRegistry.get(alertingDefinition);
					if (alertingState == null) {
						alertingState = alertingStateRegistry.register(alertingDefinition);
					}
					// check threshold
					thresholdChecker.checkThreshold(alertingState);
				} catch (Exception e) {
					if (log.isErrorEnabled()) {
						log.error("Unexpected exception occured.", e);
					}
				}
				currentCount = 0;
			}

			iterationCounter.put(id, currentCount + 1);
		}
	}
}
