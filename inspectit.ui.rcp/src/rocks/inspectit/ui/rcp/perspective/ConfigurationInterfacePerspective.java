package info.novatec.inspectit.rcp.perspective;

import info.novatec.inspectit.rcp.ci.view.ConfigurationInterfaceManagerView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The perspective for the Configuration Interface.
 * 
 * @author Ivan Senic
 * 
 */
public class ConfigurationInterfacePerspective implements IPerspectiveFactory {

	/**
	 * The ID of this perspective.
	 */
	public static final String PERSPECTIVE_ID = "info.novatec.inspectit.rcp.perspective.configurationinterface";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		float ratio = 0.4f;
		String editorArea = layout.getEditorArea();

		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, ratio, editorArea);
		topLeft.addView(ConfigurationInterfaceManagerView.VIEW_ID);

		layout.getViewLayout(ConfigurationInterfaceManagerView.VIEW_ID).setCloseable(true);
		layout.getViewLayout(ConfigurationInterfaceManagerView.VIEW_ID).setMoveable(true);
	}


}
