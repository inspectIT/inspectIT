package rocks.inspectit.server.alerting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.ci.event.AlertingDefinitionCreatedEvent;
import rocks.inspectit.server.ci.event.AlertingDefinitionDeletedEvent;
import rocks.inspectit.server.ci.event.AlertingDefinitionEvent;
import rocks.inspectit.server.ci.event.AlertingDefinitionLoadedEvent;
import rocks.inspectit.server.ci.event.AlertingDefinitionUpdateEvent;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Scheduler for the {@link ThresholdChecker} to check the threshold specified by the
 * {@link AlertingDefinition}.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AlertingScheduler implements Runnable, ApplicationListener<AlertingDefinitionEvent> {

	/**
	 * The execution interval in minutes of this runnable.
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
	 * {@link ThresholdChecker} instance.
	 */
	@Autowired
	private ThresholdChecker thresholdChecker;

	/**
	 * {@link ScheduledFuture} of the currently executed {@link AlertingScheduler}.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * The currently {@link AlertingState}s holding the existing {@link AlertingDefinition}s..
	 */
	private final List<AlertingState> alertingStates = new ArrayList<>();;

	/**
	 * Triggers the initial update step to activate the scheduler if necessary.
	 */
	@PostConstruct
	public void init() {
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

		long currentTime = System.currentTimeMillis();

		for (AlertingState alertingState : alertingStates) {
			try {
				long nextCheckTime = alertingState.getLastCheckTime() + alertingState.getAlertingDefinition().getTimeRange(TimeUnit.MILLISECONDS);

				if (nextCheckTime <= currentTime) {
					thresholdChecker.checkThreshold(alertingState);
				}
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Unexpected exception occured.", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AlertingDefinitionEvent event) {
		if (event instanceof AlertingDefinitionLoadedEvent) {
			loadedAlertingDefinitions((AlertingDefinitionLoadedEvent) event);
		} else if (event instanceof AlertingDefinitionCreatedEvent) {
			createdAlertingDefinition((AlertingDefinitionCreatedEvent) event);
		} else if (event instanceof AlertingDefinitionUpdateEvent) {
			updatedAlertingDefinition((AlertingDefinitionUpdateEvent) event);
		} else if (event instanceof AlertingDefinitionDeletedEvent) {
			deletedAlertingDefinition((AlertingDefinitionDeletedEvent) event);
		}
	}

	/**
	 * Handle the {@link AlertingDefinitionLoadedEvent}.
	 *
	 * @param event
	 *            the received {@link AlertingDefinitionLoadedEvent}
	 */
	private void loadedAlertingDefinitions(AlertingDefinitionLoadedEvent event) {
		alertingStates.clear();

		for (AlertingDefinition definition : event.getLoadedAlertingDefinitions()) {
			alertingStates.add(new AlertingState(definition));
		}
	}

	/**
	 * Handle the {@link AlertingDefinitionCreatedEvent}.
	 *
	 * @param event
	 *            the received {@link AlertingDefinitionCreatedEvent}
	 */
	private void createdAlertingDefinition(AlertingDefinitionCreatedEvent event) {
		alertingStates.add(new AlertingState(event.getCreatedAlertingDefinition()));
	}

	/**
	 * Handle the {@link AlertingDefinitionDeletedEvent}.
	 *
	 * @param event
	 *            the received {@link AlertingDefinitionDeletedEvent}
	 */
	private void deletedAlertingDefinition(AlertingDefinitionDeletedEvent event) {
		Iterator<AlertingState> iterator = alertingStates.iterator();

		while (iterator.hasNext()) {
			AlertingState state = iterator.next();

			if (state.getAlertingDefinition().equals(event.getRemovedAlertingDefinition())) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * Handle the {@link AlertingDefinitionUpdateEvent}.
	 *
	 * @param event
	 *            the received {@link AlertingDefinitionUpdateEvent}
	 */
	private void updatedAlertingDefinition(AlertingDefinitionUpdateEvent event) {
		Iterator<AlertingState> iterator = alertingStates.iterator();

		while (iterator.hasNext()) {
			AlertingState state = iterator.next();

			if (state.getAlertingDefinition().getId().equals(event.getUpdateAlertingDefinition().getId())) {
				state.setAlertingDefinition(event.getUpdateAlertingDefinition());
				break;
			}
		}
	}
}
