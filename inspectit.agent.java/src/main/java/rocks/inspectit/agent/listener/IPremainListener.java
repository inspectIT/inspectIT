package rocks.inspectit.agent.listener;

/**
 * Interface for all components that would like to be informed on the premain finalization.
 *
 * @see rocks.inspectit.agent.java.javaagent.JavaAgent.premain(String, Instrumentation)
 * @author Ivan Senic
 *
 */
public interface IPremainListener {

	/**
	 * Hook to notify that the premain has been finished.
	 *
	 * @see rocks.inspectit.agent.java.javaagent.JavaAgent.premain(String, Instrumentation)
	 */
	void afterPremain();
}
