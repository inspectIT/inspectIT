/**
 *
 */
package info.novatec.inspectit.cmr.ci.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Alexander Wert
 *
 */
public class BusinessContextDefinitionUpdateEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8508830940247906867L;

	/**
	 * Default constructor.
	 *
	 * @param source
	 *            the component that published the event (never {@code null})
	 */
	public BusinessContextDefinitionUpdateEvent(Object source) {
		super(source);
	}

}
