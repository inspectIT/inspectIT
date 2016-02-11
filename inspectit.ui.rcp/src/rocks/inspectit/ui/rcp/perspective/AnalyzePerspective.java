package info.novatec.inspectit.rcp.perspective;

import info.novatec.inspectit.rcp.view.impl.DataExplorerView;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The default perspective and layout of the InspectIT UI.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AnalyzePerspective implements IPerspectiveFactory {

	/**
	 * The ID of this perspective.
	 */
	public static final String PERSPECTIVE_ID = "info.novatec.inspectit.rcp.perspective.analyze";

	/**
	 * {@inheritDoc}
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		float ratio = 0.4f;
		String editorArea = layout.getEditorArea();

		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, ratio, editorArea);
		topLeft.addView(RepositoryManagerView.VIEW_ID);
		topLeft.addView(StorageManagerView.VIEW_ID);
		topLeft.addView(DataExplorerView.VIEW_ID);

		layout.getViewLayout(RepositoryManagerView.VIEW_ID).setCloseable(true);
		layout.getViewLayout(RepositoryManagerView.VIEW_ID).setMoveable(true);
		layout.getViewLayout(StorageManagerView.VIEW_ID).setCloseable(true);
		layout.getViewLayout(StorageManagerView.VIEW_ID).setMoveable(true);
		layout.getViewLayout(DataExplorerView.VIEW_ID).setCloseable(true);
		layout.getViewLayout(DataExplorerView.VIEW_ID).setMoveable(true);
	}
}