package rocks.inspectit.shared.all.tracing.constants;

/**
 * @author Ivan Senic
 *
 */
public interface ExtraTags {

	/**
	 * Name for the propagation type tag we are using.
	 */
	String PROPAGATION_TYPE = "ext.propagation_type";

	/**
	 * Operation name. As op name is used only in user spans, we will have this in our spans as a
	 * tag.
	 */
	String OPERATION_NAME = "ext.operation_name";

	/**
	 * Jms message id tag.
	 */
	String JMS_MESSAGE_ID = "ext.jms.id";

	/**
	 * Jms message destination tag.
	 */
	String JMS_MESSAGE_DESTINATION = "ext.jms.destination";

}
