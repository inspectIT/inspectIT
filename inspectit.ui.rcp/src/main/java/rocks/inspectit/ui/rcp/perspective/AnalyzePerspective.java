package rocks.inspectit.ui.rcp.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import rocks.inspectit.ui.rcp.view.impl.DataExplorerView;
import rocks.inspectit.ui.rcp.view.impl.RepositoryManagerView;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;

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
	public static final String PERSPECTIVE_ID = "rocks.inspectit.ui.rcp.perspective.analyze";

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