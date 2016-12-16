package rocks.inspectit.agent.java.util;

import rocks.inspectit.agent.java.IThreadTransformHelper;

/**
 * Thread that is aware of the agent and is not doing any transformations.
 *
 * @author Ivan Senic
 *
 */
public class AgentAwareThread extends Thread {

	/**
	 * {@link IThreadTransformHelper} to use to disable transformations done in this thread.
	 */
	private final IThreadTransformHelper threadTransformHelper;

	/**
	 * Default constructor.
	 * 
	 * @param target
	 *            Runnable to be executed by this thread.
	 * @param threadTransformHelper
	 *            {@link IThreadTransformHelper} to use to disable transformations done in this
	 *            thread.
	 */
	public AgentAwareThread(Runnable target, IThreadTransformHelper threadTransformHelper) {
		super(target);
		this.threadTransformHelper = threadTransformHelper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// never do any transformation with this thread
		threadTransformHelper.setThreadTransformDisabled(true);

		super.run();
	};

}
