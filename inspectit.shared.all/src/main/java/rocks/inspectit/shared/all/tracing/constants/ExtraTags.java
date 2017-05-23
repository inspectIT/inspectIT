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

	/**
	 * Exception type tag.
	 */
	String THROWABLE_TYPE = "ext.throw.type";

	/**
	 * Inspectit method id.
	 */
	String INSPECTT_METHOD_ID = "ext.inspectit.method";

	/**
	 * Inspectit sensor id.
	 */
	String INSPECTT_SENSOR_ID = "ext.inspectit.sensor";

	/**
	 * The class name of the traced runnable.
	 */
	String RUNNABLE_TYPE = "ext.runnable.type";
	
	/**
	 * Prefix for storing EUM information captured on dom elements.
	 */
	String INSPECTT_DOM_ELEMENT_PREFIX = "ext.inspectit.domelement.";
}
