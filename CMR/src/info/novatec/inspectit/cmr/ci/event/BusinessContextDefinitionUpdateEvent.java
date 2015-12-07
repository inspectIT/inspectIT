package info.novatec.inspectit.cmr.ci.event;

import info.novatec.inspectit.ci.BusinessContextDefinition;

import org.springframework.context.ApplicationEvent;

/**
 * An update event triggered whenever the {@link BusinessContextDefinition} on the CMR has been
 * updated.
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextDefinitionUpdateEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8508830940247906867L;

	/**
	 * The updated {@link BusinessContextDefinition}.
	 */
	private final BusinessContextDefinition updatedBusinessContextDefinition;

	/**
	 * Default constructor.
	 *
	 * @param source
	 *            the component that published the event (never {@code null}).
	 * @param updatedBusinessContextDefinition
	 *            the updated {@link BusinessContextDefinition}.
	 */
	public BusinessContextDefinitionUpdateEvent(Object source, BusinessContextDefinition updatedBusinessContextDefinition) {
		super(source);
		this.updatedBusinessContextDefinition = updatedBusinessContextDefinition;
	}

	/**
	 * Gets {@link #updatedBusinessContextDefinition}.
	 *
	 * @return {@link #updatedBusinessContextDefinition}
	 */
	public BusinessContextDefinition getUpdatedBusinessContextDefinition() {
		return updatedBusinessContextDefinition;
	}

}
