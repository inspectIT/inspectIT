package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.Objects;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}
	 */
	public RefreshEditorsCachedDataService(IGlobalDataAccessService globalDataAccessService, CmrRepositoryDefinition repositoryDefinition) {
		super(globalDataAccessService);
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
					IRootEditor rootEditor = (IRootEditor) editor.getEditor(false);
					if (Objects.equals(rootEditor.getInputDefinition().getRepositoryDefinition(), repositoryDefinition)) {
						rootEditor.doRefresh();
					}
				}
			}
		});
	}

}
