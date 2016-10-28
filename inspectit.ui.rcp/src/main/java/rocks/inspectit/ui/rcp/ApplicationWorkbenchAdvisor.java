package rocks.inspectit.ui.rcp;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import rocks.inspectit.ui.rcp.ci.listener.impl.InstrumentationUpdateListener;
import rocks.inspectit.ui.rcp.job.CheckNewVersionJob;
import rocks.inspectit.ui.rcp.perspective.AnalyzePerspective;

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

		// register the instr. update listener
		InstrumentationUpdateListener updateListener = new InstrumentationUpdateListener();
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addAgentMappingsChangeListener(updateListener);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(updateListener);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(updateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInitialWindowPerspectiveId() {
		return AnalyzePerspective.PERSPECTIVE_ID;
	}

}
