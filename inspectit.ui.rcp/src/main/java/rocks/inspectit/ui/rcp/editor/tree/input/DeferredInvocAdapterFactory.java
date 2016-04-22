package rocks.inspectit.ui.rcp.editor.tree.input;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Adapter Factory which is used to create the {@link DeferredInvoc} objects if the adaptable object
 * is of type {@link InvocationSequenceData}.
 *
 * @author Patrice Bouillet
 *
 */
public class DeferredInvocAdapterFactory implements IAdapterFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IDeferredWorkbenchAdapter.class == adapterType) {
			if (adaptableObject instanceof InvocationSequenceData) {
				return new DeferredInvoc();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IDeferredWorkbenchAdapter.class };
	}

}
