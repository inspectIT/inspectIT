package info.novatec.inspectit.rcp.ci.form.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Abstract {@link FormEditor} for the configuration interface editors.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractConfigurationInterfaceFormEditor extends FormEditor {

	/**
	 * Denotes if the exception on save occurred.
	 */
	private boolean exceptionOnSave;

	/**
	 * {@inheritDoc}
	 * <p>
	 * A small fix so that the tabs are not displayed if only one page is existing in the editor.
	 */
	@Override
	protected void createPages() {
		super.createPages();
		if (getPageCount() == 1 && getContainer() instanceof CTabFolder) {
			((CTabFolder) getContainer()).setTabHeight(0);
		}
	}

	/**
	 * Checks if the editor has valid input. Displays appropriate message if not.
	 * <p>
	 * Sub-classes should call this method prior to saving and abort saving if this method returns
	 * <code>false</code>
	 * 
	 * @return <code>false</code> if editor contains any page with any invalid part
	 */
	protected boolean checkValid() {
		Collection<IManagedForm> invalidPages = getInvalidForms();
		if (CollectionUtils.isNotEmpty(invalidPages)) {
			StringBuilder stringBuilder = new StringBuilder("Save can not be performed as one or more pages contain invalid input: \n");

			for (IManagedForm managedForm : invalidPages) {
				stringBuilder.append("\n" + managedForm.getForm().getText() + ": " + managedForm.getForm().getMessage());
			}

			MessageDialog.openError(getSite().getShell(), "Invalid Input", stringBuilder.toString());
			return false;
		}
		return true;
	}

	/**
	 * Returns the pages that contain at least one part that is not valid.
	 * 
	 * @return Returns the pages that contain at least one part that is not valid.
	 */
	private Collection<IManagedForm> getInvalidForms() {
		if (pages != null) {
			List<IManagedForm> invalidForms = new ArrayList<IManagedForm>();
			for (int i = 0; i < pages.size(); i++) {
				Object page = pages.get(i);
				if (page instanceof IFormPage) {
					IFormPage formPage = (IFormPage) page;
					IManagedForm managedForm = formPage.getManagedForm();

					if (null != managedForm && managedForm.getForm().getMessageType() == IMessageProvider.ERROR) {
						invalidForms.add(managedForm);
					}
				}
			}
			return invalidForms;
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return exceptionOnSave || super.isDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		getActivePageInstance().setFocus();
	}

	/**
	 * Sets {@link #exceptionOnSave}.
	 * 
	 * @param exceptionOnSave
	 *            New value for {@link #exceptionOnSave}
	 */
	protected void setExceptionOnSave(boolean exceptionOnSave) {
		this.exceptionOnSave = exceptionOnSave;
	}

}
