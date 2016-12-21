package rocks.inspectit.agent.java;

/**
 * Helper that each thread can use to enable/disable transformation for itself.
 *
 * @author Ivan Senic
 *
 */
public interface IThreadTransformHelper {

	/**
	 * Returns if the instrumentation transform method for the thread calling this method is
	 * currently disabled.
	 *
	 * @return Returns true if the transform is disabled for current thread, otherwise
	 *         <code>false</code>.
	 */
	boolean isThreadTransformDisabled();

	/**
	 * Sets if the instrumentation transform method for the thread calling this method is currently
	 * disabled.
	 *
	 * @param disabled
	 *            <code>true</code> to disable, <code>false</code> otherwise
	 */
	void setThreadTransformDisabled(boolean disabled);

}