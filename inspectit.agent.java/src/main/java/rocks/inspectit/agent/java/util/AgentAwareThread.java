package rocks.inspectit.agent.java.util;

import rocks.inspectit.agent.java.IThreadTransformHelper;

/**
 * Thread that is aware of the agent. If the agent is called from within this thread, it's
 * functionality of transforming the classes will be deactivated.
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
	 * Basic constructor.
	 *
	 * @param threadTransformHelper
	 *            {@link IThreadTransformHelper} to use to disable transformations done in this
	 *            thread.
	 * @see Thread#Thread()
	 */
	public AgentAwareThread(IThreadTransformHelper threadTransformHelper) {
		this.threadTransformHelper = threadTransformHelper;
	}

	/**
	 * Constructor that allows setting of the runnable for the thread.
	 *
	 * @param target
	 *            Runnable to be executed by this thread.
	 * @param threadTransformHelper
	 *            {@link IThreadTransformHelper} to use to disable transformations done in this
	 *            thread.
	 * @see Thread#Thread(Runnable)
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
