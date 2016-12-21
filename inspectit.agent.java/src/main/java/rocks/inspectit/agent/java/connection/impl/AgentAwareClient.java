package rocks.inspectit.agent.java.connection.impl;

import rocks.inspectit.agent.java.IThreadTransformHelper;
import rocks.inspectit.shared.all.kryonet.Client;
import rocks.inspectit.shared.all.kryonet.IExtendedSerialization;
import rocks.inspectit.shared.all.storage.nio.stream.StreamProvider;

/**
 * Small extension to the KryoNet client that sets
 * {@link rocks.inspectit.agent.java.IAgent#setThreadTransformDisabled(true)} when entering the
 * {@link #run()} method. This will disable any class transformation from the client thread.
 *
 * @author Ivan Senic
 *
 */
public class AgentAwareClient extends Client {

	/**
	 * {@link IThreadTransformHelper} to use to disable transformations done in this thread.
	 */
	private IThreadTransformHelper threadTransformHelper;

	/**
	 * Default constructor.
	 *
	 * @param serialization
	 *            {@link IExtendedSerialization} to use.
	 * @param streamProvider
	 *            {@link StreamProvider} for streams.
	 * @param threadTransformHelper
	 *            {@link IThreadTransformHelper} to use to disable transformations done in this
	 *            thread.
	 */
	public AgentAwareClient(IExtendedSerialization serialization, StreamProvider streamProvider, IThreadTransformHelper threadTransformHelper) {
		super(serialization, streamProvider);
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
