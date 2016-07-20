package rocks.inspectit.agent.listener;

/**
 * Interface for all components that would like to be informed on the agent premain finalization.
 * <p>
 * During the premain following events should happen:
 * <ul>
 * <li>SpringAgent is initialized
 * <li>Already loaded (Java native) classes have been analyzed (and instrumented)
 * <li>Our transformer has been registered with Instrumentation API
 * </ul>
 * Thus, the class implementing this interface can consider that all of the mentioned events have
 * been finished when {@link #afterPremain()} is called.
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
