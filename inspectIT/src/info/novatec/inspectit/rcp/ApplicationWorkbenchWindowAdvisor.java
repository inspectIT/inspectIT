package info.novatec.inspectit.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Here we configure our workbench window.
 * 
 * @author Ivan Senic
 * 
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	/**
	 * Default constructor.
	 * 
	 * @param configurer
	 *            {@link IWorkbenchWindowConfigurer}
	 * @see WorkbenchWindowAdvisor#WorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1280, 960));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
	}

}
