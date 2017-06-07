package rocks.inspectit.ui.rcp.job;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import rocks.inspectit.ui.rcp.InspectIT;

/**
 * @author Marius Oehler
 *
 */
public class BlockingJob<E> {

	private E result;

	private String jobname;

	private Callable<E> task;

	public BlockingJob(String jobname, Callable<E> task) {
		this.jobname = jobname;
		this.task = task;
	}

	public E scheduleAndJoin() {
		Job job = new Job(jobname) {
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
		};

		job.schedule();

		try {
			job.join();
		} catch (InterruptedException e) {
			return null;
		}

		return result;
	}

}
