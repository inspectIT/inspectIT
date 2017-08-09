package rocks.inspectit.ui.rcp.form;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Form which provides basic Properties of a certain element.
 * 
 * @author Tobias Angerstein
 *
 */
public abstract class AbstractPropertyForm implements ISelectionChangedListener {

	protected ManagedForm managedForm; // NOCHK
	protected FormToolkit toolkit; // NOCHK
	protected ScrolledForm form; // NOCHK
	protected Composite mainComposite; // NOCHK

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected AbstractPropertyForm(Composite parent) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
	}

	/**
	 * Sets layout data for the form.
	 *
	 * @param layoutData
	 *            LayoutData.
	 */
	public void setLayoutData(Object layoutData) {
		form.setLayoutData(layoutData);
	}

	/**
	 * Refreshes the property form.
	 */
	public abstract void refresh();

	/**
	 *
	 * @return Returns if the form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the form.
	 */
	public void dispose() {
		form.dispose();
	}

}
