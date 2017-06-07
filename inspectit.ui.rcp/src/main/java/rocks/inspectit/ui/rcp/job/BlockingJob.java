package rocks.inspectit.ui.rcp.job;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import rocks.inspectit.ui.rcp.InspectIT;

/**
 * This class is an extended job which performs a task (represented by a {@link Callable} which is
 * able to return a result object.
 *
 * @param <E>
 *            The type of the result object
 *
 * @author Marius Oehler
 *
 */
public class BlockingJob<E> extends Job {

	/**
	 * The result object.
	 */
	private E result;

	/**
	 * The callable to execute.
	 */
	private Callable<E> task;

	/**
	 * Constructor.
	 *
	 * @param jobname
	 *            the name of the job
	 * @param task
	 *            the {@link Callable} to execute
	 */
	public BlockingJob(String jobname, Callable<E> task) {
		super(jobname);
		this.task = task;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			result = task.call();
		} catch (Exception e) {
			InspectIT.getDefault().log(IStatus.ERROR, "An unexpected exception occured.", e);
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Executes the job and returns the result. This method blocks until the job has finished.
	 *
	 * @return the result of {@link #task}
	 */
	public E scheduleAndJoin() {
		schedule();

		try {
			join();
		} catch (InterruptedException e) {
			Thread.interrupted();
			return null;
		}

		return result;
	}
}
