package rocks.inspectit.ui.rcp.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryManager;

/**
 * Update online status of all repositories job.
 *
 * @author Ivan Senic
 *
 */
public class UpdateRepositoryJob extends Job {

	/**
	 * CMR to update.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Should job be rescheduled after its execution.
	 */
	private boolean rescheduleJob;

	/**
	 * Default constructor.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to update.
	 * @param rescheduleJob
	 *            If job should be rescheduled after execution.
	 */
	public UpdateRepositoryJob(CmrRepositoryDefinition cmrRepositoryDefinition, boolean rescheduleJob) {
		super("Updating online status of CMR repository " + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort());
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.rescheduleJob = rescheduleJob;
		this.setUser(false);
		this.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_SERVER_REFRESH_SMALL));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			cmrRepositoryDefinition.refreshOnlineStatus();
			return Status.OK_STATUS;
		} finally {
			if (rescheduleJob) {
				this.schedule(CmrRepositoryManager.UPDATE_JOB_REPETITION);
			}
		}
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

}