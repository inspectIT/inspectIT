package rocks.inspectit.ui.rcp.repository.service;

import java.util.Objects;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * The {@link CachedDataService} to be used on the UI. When refresh of idents is triggered, all
 * editors on the given {@link CmrRepositoryDefinition} are refreshed so that correct data is
 * displayed.
 *
 * @author Ivan Senic
 *
 */
public class RefreshEditorsCachedDataService extends CachedDataService {

	/**
	 * Repository definition.
	 */
	private CmrRepositoryDefinition repositoryDefinition;

	/**
	 * @param globalDataAccessService
	 *            {@link IGlobalDataAccessService}
	 * @param businessContextService
	 *            {@link IBusinessContextManagementService}
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}
	 */
	public RefreshEditorsCachedDataService(IGlobalDataAccessService globalDataAccessService, IBusinessContextManagementService businessContextService, CmrRepositoryDefinition repositoryDefinition) {
		super(globalDataAccessService, businessContextService);
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postRefreshIdents() {
		// execute refresh of all opened editors since data is not valid any more
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IEditorReference[] editors = workbenchWindow.getActivePage().getEditorReferences();
				for (IEditorReference editor : editors) {
					IEditorPart editorPart = editor.getEditor(false);
					if (editorPart instanceof IRootEditor) {
						IRootEditor rootEditor = (IRootEditor) editorPart;
						if (Objects.equals(rootEditor.getInputDefinition().getRepositoryDefinition(), repositoryDefinition)) {
							rootEditor.doRefresh();
						}
					}
				}
			}
		});
	}

}
