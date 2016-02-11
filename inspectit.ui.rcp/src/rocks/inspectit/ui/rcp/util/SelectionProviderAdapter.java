package rocks.inspectit.ui.rcp.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Simple class that can publish selection of the view to the site.
 * <p>
 * <b>IMPORTANT:</b> The class is licensed under the Eclipse Public License v1.0 as it includes the
 * code from the {@link org.eclipse.ui.internal.part.SelectionProviderAdapter} class belonging to
 * the Eclipse Rich Client Platform. EPL v1.0 license can be found <a
 * href="https://www.eclipse.org/legal/epl-v10.html">here</a>.
 * <p>
 * Please relate to the LICENSEEXCEPTIONS.txt file for more information about license exceptions
 * that apply regarding to InspectIT and Eclipse RCP and/or EPL Components.
 * 
 */
public class SelectionProviderAdapter implements ISelectionProvider {

	/**
	 * List of listeners.
	 */
	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * Current selection.
	 */
	private ISelection theSelection = StructuredSelection.EMPTY;

	/**
	 * {@inheritDoc}
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelection getSelection() {
		return theSelection;
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
	public void setSelection(ISelection selection) {
		theSelection = selection;
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (final ISelectionChangedListener listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					listener.selectionChanged(e);
				}
			});
		}
	}

}
