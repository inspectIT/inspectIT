package rocks.inspectit.agent.java.eum.instrumentation;

/**
 * Simple interface allow the wrapping of the modification of HTML headers using the command
 * pattern. This allows us to "postpone" the commands regarding the content size when performing the
 * EUM instrumentation.
 *
 * @author Jonas Kunz
 *
 */
public interface HeaderModificationCommand {

	/**
	 * Executes the command, resulting in a modification of the HTML headers.
	 */
	void execute();

}
