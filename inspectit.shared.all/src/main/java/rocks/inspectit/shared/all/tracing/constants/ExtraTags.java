package rocks.inspectit.shared.all.tracing.constants;

/**
 * Extra tags key that are not defined by the opentracing and used exclusively by inspectIT to
 * enrich the span information.
 * <p>
 * Not using the opentracing.io Tag implementation due to the default package modifiers.
 *
 * @author Ivan Senic
 *
 */
public interface ExtraTags {

	/**
	 * Name for the propagation type tag we are using.
	 */
	String PROPAGATION_TYPE = "ext.propagation.type";

	/**
	 * Operation name. As op name is used only in user spans, we will have this in our spans as a
	 * tag.
	 */
	String OPERATION_NAME = "ext.operation.name";

	/**
	 * Jms message id tag.
	 */
	String JMS_MESSAGE_ID = "ext.jms.id";

	/**
	 * Jms message destination tag.
	 */
	String JMS_MESSAGE_DESTINATION = "ext.jms.destination";

}
