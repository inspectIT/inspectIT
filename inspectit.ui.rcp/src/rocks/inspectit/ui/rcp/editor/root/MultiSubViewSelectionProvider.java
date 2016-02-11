package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.rcp.editor.ISubView;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Patrice Bouillet
 * 
 */
public class MultiSubViewSelectionProvider implements IPostSelectionProvider {

	/**
	 * Registered selection changed listeners (element type: <code>ISelectionChangedListener</code>
	 * ).
	 */
	private ListenerList listeners = new ListenerList();

	/**
	 * Registered post selection changed listeners.
	 */
	private ListenerList postListeners = new ListenerList();

	/**
	 * The root editor.
	 */
	private AbstractRootEditor rootEditor;

	/**
	 * Constructor needs a root editor.
	 * 
	 * @param rootEditor
	 *            The root editor.
	 */
	public MultiSubViewSelectionProvider(AbstractRootEditor rootEditor) {
		Assert.isNotNull(rootEditor);

		this.rootEditor = rootEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		postListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
		postListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelection getSelection() {
		ISubView subView = rootEditor.getActiveSubView();
		if (null != subView) {
			ISelectionProvider selectionProvider = subView.getSelectionProvider();
			if (null != selectionProvider) {
				return selectionProvider.getSelection();
			}
		}

		return StructuredSelection.EMPTY;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSelection(ISelection selection) {
		ISubView subView = rootEditor.getActiveSubView();
		if (null != subView) {
			ISelectionProvider selectionProvider = subView.getSelectionProvider();
			if (null != selectionProvider) {
				selectionProvider.setSelection(selection);
			}
		}
	}

	/**
	 * Notifies all registered selection changed listeners that the editor's selection has changed.
	 * Only listeners registered at the time this method is called are notified.
	 * 
	 * @param event
	 *            the selection changed event
	 */
	public void fireSelectionChanged(final SelectionChangedEvent event) {
		Object[] listeners = this.listeners.getListeners();
		fireEventChange(event, listeners);
	}

	/**
	 * Notifies all post selection changed listeners that the editor's selection has changed.
	 * 
	 * @param event
	 *            the event to propagate.
	 */
	public void firePostSelectionChanged(final SelectionChangedEvent event) {
		Object[] listeners = postListeners.getListeners();
		fireEventChange(event, listeners);
	}

	/**
	 * Fires the actual event.
	 * 
	 * @param event
	 *            The event to fire.
	 * @param listeners
	 *            All the registered listeners.
	 */
	private void fireEventChange(final SelectionChangedEvent event, Object[] listeners) {
		for (int i = 0; i < listeners.length; ++i) {
			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

}
