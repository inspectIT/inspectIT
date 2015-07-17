package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.composite.AbstractCompositeSubView;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.search.ISearchExecutor;
import info.novatec.inspectit.rcp.editor.search.OpenedSearchControlCache;
import info.novatec.inspectit.rcp.editor.search.SearchControl;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler for activating the search box.
 * 
 * @author Ivan Senic
 * 
 */
public class FindHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor instanceof AbstractRootEditor) {
			AbstractRootEditor abstractRootEditor = (AbstractRootEditor) activeEditor;
			ISearchExecutor searchExecutor = null;
			ISubView searchSubView = null;

			if (abstractRootEditor.getActiveSubView() instanceof ISearchExecutor) {
				searchSubView = abstractRootEditor.getActiveSubView();
				searchExecutor = (ISearchExecutor) abstractRootEditor.getActiveSubView();
			} else {
				searchSubView = findSearchExecutorView(abstractRootEditor.getSubView());
				searchExecutor = (ISearchExecutor) searchSubView;
			}

			if (null != searchExecutor && null != searchSubView) {
				ensureNoSearchOpened(abstractRootEditor.getSubView());
				new SearchControl(searchExecutor, HandlerUtil.getActiveShellChecked(event), searchSubView.getControl(), activeEditor);
			}
		}
		return null;
	}

	/**
	 * Ensures that no search control is opened for the given {@link ISubView}. If the view is
	 * composite, than it ensures than no child sub-view has control opened.
	 * 
	 * @param subView
	 *            SubView to check.
	 */
	private void ensureNoSearchOpened(ISubView subView) {
		if (subView instanceof ISearchExecutor) {
			SearchControl searchControl = OpenedSearchControlCache.getSearchControl((ISearchExecutor) subView);
			if (null != searchControl) {
				searchControl.closeControl();
			}
		} else if (subView instanceof AbstractCompositeSubView) {
			AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
			for (ISubView viewInCompositeSubView : compositeSubView.getSubViews()) {
				ensureNoSearchOpened(viewInCompositeSubView);
			}
		}
	}

	/**
	 * Tries to find a {@link ISubView} that implement {@link ISearchExecutor} interface.
	 * 
	 * @param subView
	 *            {@link ISubView} to check.
	 * @return Sub-view.
	 */
	private ISubView findSearchExecutorView(ISubView subView) {
		if (subView instanceof ISearchExecutor) {
			return subView;
		} else if (subView instanceof AbstractCompositeSubView) {
			AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
			for (ISubView viewInCompositeSubView : compositeSubView.getSubViews()) {
				ISubView foundView = findSearchExecutorView(viewInCompositeSubView);
				if (null != foundView) {
					return foundView;
				}
			}
		}
		return null;
	}
}