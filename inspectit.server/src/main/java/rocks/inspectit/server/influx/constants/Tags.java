package rocks.inspectit.server.influx.constants;

/**
 * @author Ivan Senic
 *
 */
public interface Tags {

	/**
	 * Agent name tag.
	 */
	String AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	String AGENT_ID = "agentId";

	/**
	 * Application name tag.
	 */
	String APPLICATION_NAME = "applicationName";

	/**
	 * Business transaction name tag.
	 */
	String BUSINESS_TRANSACTION_NAME = "businessTxName";

	/**
	 * Simple method name tag.
	 */
	String METHOD_NAME = "methodName";

	/**
	 * Class FQN tag.
	 */
	String CLASS_FQN = "classFqn";

	/**
	 * Fully qualified method signature tag.
	 */
	String METHOD_SIGNATURE = "fqnMethodSignature";

	/**
	 * URI tag.
	 */
	String URI = "uri";

	/**
	 * inspectIT tagging header tag.
	 */
	String INSPECTIT_TAGGING_HEADER = "inspectitTaggingHeader";

	/**
	 * JMX attribute name tag.
	 */
	String JMX_ATTRIBUTE_FULL_NAME = "name";
}
