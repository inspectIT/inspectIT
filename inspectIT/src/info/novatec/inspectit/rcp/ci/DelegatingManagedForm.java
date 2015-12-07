package info.novatec.inspectit.rcp.ci;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;

/**
 * This managed form delegates dirtyState changes to a {@link ManagedForm} form.
 *
 * @author Alexander Wert
 *
 */
public class DelegatingManagedForm extends ManagedForm {
	/**
	 * The parent {@link ManagedForm}.
	 */
	private final IManagedForm parentManagedForm;

	/**
	 * Constructor.
	 *
	 * @param parentManagedForm
	 *            the parent {@link ManagedForm}.
	 * @param parent
	 *            parent {@link Composite}
	 */
	public DelegatingManagedForm(IManagedForm parentManagedForm, Composite parent) {
		super(parent);
		this.parentManagedForm = parentManagedForm;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dirtyStateChanged() {
		super.dirtyStateChanged();
		getParentManagedForm().dirtyStateChanged();
	}

	/**
	 * Gets {@link #parentManagedForm}.
	 *
	 * @return {@link #parentManagedForm}
	 */
	public IManagedForm getParentManagedForm() {
		return parentManagedForm;
	}
}