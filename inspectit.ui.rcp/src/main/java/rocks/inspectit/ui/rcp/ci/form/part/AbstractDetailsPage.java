package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.util.RemoveSelection;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * Class holding general functionality for all the details pages that we have.
 *
 * @param <E>
 *            Type of element edited in this detail page.
 * @author Ivan Senic
 *
 */
public abstract class AbstractDetailsPage<E> implements IDetailsPage, IControlValidationListener {

	/**
	 * Managed for part belongs to.
	 */
	protected IManagedForm managedForm;

	/**
	 * Marker for updating the widgets contents when the selection changes, so that mark dirty is
	 * only fired when changes occur as result of user interaction.
	 */
	private boolean updateInProgress;

	/**
	 * {@link IDetailsModifiedListener}.
	 */
	private final IDetailsModifiedListener<E> detailsModifiedListener;

	/**
	 * Validation manager of the master part.
	 */
	private final AbstractValidationManager<E> masterValidationManager;

	/**
	 * List of {@link ValidationControlDecoration}s.
	 */
	private final List<ValidationControlDecoration<?>> validationControlDecorations = new ArrayList<>();

	/**
	 * Listener that marks dirty on any event.
	 */
	protected Listener markDirtyListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (!updateInProgress) {
				commitToInput();

				E input = getInput();
				if (null != detailsModifiedListener && null != input) {
					detailsModifiedListener.contentModified(input);
				}
			}
		}
	};

	/**
	 * Default constructor.
	 *
	 * @param detailsModifiedListener
	 *            listener to inform the master block on changes to the input
	 * @param masterValidationManager
	 *            Validation manager of the master part.
	 */
	public AbstractDetailsPage(IDetailsModifiedListener<E> detailsModifiedListener, AbstractValidationManager<E> masterValidationManager) {
		this.detailsModifiedListener = detailsModifiedListener;
		this.masterValidationManager = masterValidationManager;
	}

	/**
	 * Returns currently displayed input element or <code>null</code> if one does not exists.
	 *
	 * @return Returns currently displayed input element or <code>null</code> if one does not
	 *         exists.
	 */
	protected abstract E getInput();

	/**
	 * Sets the input from the given selection.
	 *
	 * @param selection
	 *            selection
	 */
	protected abstract void setInput(ISelection selection);

	/**
	 * Updates controls from the input.
	 */
	protected abstract void updateFromInput();

	/**
	 * Commits changes in page to input.
	 */
	protected abstract void commitToInput();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}

	/**
	 * Updates the display state with validation.
	 */
	protected void update() {
		updateInProgress = true;
		updateFromInput();
		checkValid(true);
		updateInProgress = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void selectionChanged(IFormPart part, ISelection selection) {
		if (selection instanceof RemoveSelection) {
			boolean currentlyEdited = false;
			for (Object element : ((RemoveSelection) selection).toList()) {
				if (element == getInput()) {
					currentlyEdited = true;
					break;
				}
			}
			if (currentlyEdited) {
				setInput(StructuredSelection.EMPTY);
				update();
			}
		} else {
			setInput(selection);
			update();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		// if we have no input for at the moment we can not notify
		if (null == getInput()) {
			return;
		}

		String message = null;
		if (!valid) {
			IMessagePrefixProvider messagePrefixProvider = managedForm.getMessageManager().getMessagePrefixProvider();
			String prefix = messagePrefixProvider.getPrefix(validationControlDecoration.getControl());
			// don't append if no prefix can be found
			if (!": ".equals(prefix)) {
				message = prefix + validationControlDecoration.getDescriptionText();
			} else {
				message = validationControlDecoration.getDescriptionText();
			}
		}
		ValidationState state = new ValidationState(validationControlDecoration, valid, message);
		masterValidationManager.validationStateChanged(getInput(), state);
	}

	/**
	 * Validates all {@link ValidationControlDecoration} on the page and returns the current valid
	 * state.
	 *
	 *
	 * @param executeValidation
	 *            if each {@link #validationControlDecorations} should execute new validation before
	 *            calculating
	 * @return If the all data in the controls are valid.
	 */
	protected boolean checkValid(boolean executeValidation) {
		boolean valid = true;
		for (ValidationControlDecoration<?> decoration : validationControlDecorations) {
			if (executeValidation) {
				decoration.executeValidation(true);
			}

			if (!decoration.isValid()) {
				valid = false;
				if (!executeValidation) {
					// if we don't need to execute validation of all decorations, then as soon as we
					// find the first invalid we can break out
					break;
				}
			}
		}
		return valid;
	}

	/**
	 * Adds the {@link ValidationControlDecoration} to the list of the decorations. This list is
	 * used for validating if the complete input on the page is correct.
	 *
	 * @param validationControlDecoration
	 *            {@link ValidationControlDecoration}.
	 */
	protected void addValidationControlDecoration(ValidationControlDecoration<?> validationControlDecoration) {
		validationControlDecorations.add(validationControlDecoration);
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 *
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param text
	 *            Information text.
	 * @return created label
	 */
	protected Label createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		return label;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The details page never reports dirty state.
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
