package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

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
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IDeferredWorkbenchAdapter.class };
	}

}
