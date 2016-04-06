package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.job.CheckNewVersionJob;
import info.novatec.inspectit.rcp.perspective.AnalyzePerspective;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Our extension to the {@link WorkbenchAdvisor} where we define the workbench related things like
 * default perspective, unchecked exceptions handling, etc.
 * 
 * @author Ivan Senic
 * 
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// save the state of the application regarding its position etc.
		configurer.setSaveAndRestore(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postStartup() {
		super.postStartup();

		// fire up the auto new version check
		Job checkNewVersionJob = new CheckNewVersionJob(false);
		checkNewVersionJob.schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialWindowPerspectiveId() {
		return AnalyzePerspective.PERSPECTIVE_ID;
	}

}
