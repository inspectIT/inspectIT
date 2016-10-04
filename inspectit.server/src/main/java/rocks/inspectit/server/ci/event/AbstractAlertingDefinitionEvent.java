package rocks.inspectit.server.ci.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Base class for all events related to {@link AlertingDefinition}.
 *
 * @author Marius Oehler, Alexander Wert
 *
 */
public abstract class AbstractAlertingDefinitionEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4223794223438737561L;

	/**
	 * Alert event types.
	 *
	 * @author Alexander Wert
	 *
	 */
	public enum AlertDefinitionEventType {
		/**
		 * Alert event types.
		 */
		ADDED, REMOVED, LOADED, UPDATE;
	}

	/**
	 * Affected alerting definitions.
	 */
	private List<AlertingDefinition> alertDefs = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param object
	 *            Object on which the event occured.
	 * @param alertDefs
	 *            Affected alerting definitions.
	 */
	public AbstractAlertingDefinitionEvent(Object object, Collection<AlertingDefinition> alertDefs) {
		super(object);
		this.alertDefs = new ArrayList<>(alertDefs);
	}

	/**
	 * Returns the type of the alert event.
	 *
	 * @return Returns the type of the alert event.
	 */
	public abstract AlertDefinitionEventType getType();

	public List<AlertingDefinition> getAlertingDefinitions() {
		return alertDefs;
	}

	/**
	 * Returns the first affected alerting definition.
	 *
	 * @return Returns the first affected alerting definition.
	 */
	public AlertingDefinition getFirst() {
		if (!alertDefs.isEmpty()) {
			return alertDefs.get(0);
		}
		return null;
	}

	/**
	 * Create event type.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertingDefinitionCreatedEvent extends AbstractAlertingDefinitionEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 342196761043743128L;

		/**
		 * Constructor.
		 *
		 * @param object
		 *            Object on which the event occured.
		 * @param alertDefinition
		 *            Created alerting definition.
		 */
		public AlertingDefinitionCreatedEvent(Object object, AlertingDefinition alertDefinition) {
			super(object, Collections.singleton(alertDefinition));
		}

		@Override
		public AlertDefinitionEventType getType() {
			return AlertDefinitionEventType.ADDED;
		}

	}

	/**
	 * Delete event type.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertingDefinitionDeletedEvent extends AbstractAlertingDefinitionEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 7522053832345580092L;

		/**
		 * Constructor.
		 *
		 * @param object
		 *            Object on which the event occured.
		 * @param alertDefinition
		 *            Deleted alerting definition.
		 */
		public AlertingDefinitionDeletedEvent(Object object, AlertingDefinition alertDefinition) {
			super(object, Collections.singleton(alertDefinition));
		}

		@Override
		public AlertDefinitionEventType getType() {
			return AlertDefinitionEventType.REMOVED;
		}

	}

	/**
	 * Loaded event type.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertingDefinitionLoadedEvent extends AbstractAlertingDefinitionEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 8385604544855440639L;

		/**
		 * Constructor.
		 *
		 * @param object
		 *            Object on which the event occured.
		 * @param alertDefs
		 *            Affected alerting definitions.
		 */
		public AlertingDefinitionLoadedEvent(Object object, Collection<AlertingDefinition> alertDefs) {
			super(object, alertDefs);
		}

		@Override
		public AlertDefinitionEventType getType() {
			return AlertDefinitionEventType.LOADED;
		}

	}

	/**
	 * Update alerting type.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertingDefinitionUpdateEvent extends AbstractAlertingDefinitionEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -4024915646678593399L;

		/**
		 * Constructor.
		 *
		 * @param object
		 *            Object on which the event occured.
		 * @param alertDefinition
		 *            Updated alerting definition.
		 */
		public AlertingDefinitionUpdateEvent(Object object, AlertingDefinition alertDefinition) {
			super(object, Collections.singleton(alertDefinition));
		}

		@Override
		public AlertDefinitionEventType getType() {
			return AlertDefinitionEventType.UPDATE;
		}
	}
}
