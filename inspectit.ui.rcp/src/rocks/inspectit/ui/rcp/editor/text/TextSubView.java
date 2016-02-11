package info.novatec.inspectit.rcp.editor.text;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.text.input.TextInputController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class is for text views.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class TextSubView extends AbstractSubView {

	/**
	 * The {@link Composite}.
	 */
	private Composite composite;

	/**
	 * The {@link TextInputController}.
	 */
	private TextInputController textInputController;

	/**
	 * Defines if a refresh job is currently already executing.
	 */
	private volatile boolean jobInSchedule = false;

	/**
	 * The constructor accepting one parameter.
	 * 
	 * @param textInputController
	 *            An instance of the {@link TextInputController}.
	 */
	public TextSubView(TextInputController textInputController) {
		Assert.isNotNull(textInputController);

		this.textInputController = textInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		textInputController.setInputDefinition(getRootEditor().getInputDefinition());
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(1, false));
		textInputController.createPartControl(composite, toolkit);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		if (checkDisposed()) {
			return;
		}

		if (!jobInSchedule) {
			jobInSchedule = true;

			Job job = new Job(getDataLoadingJobName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						textInputController.doRefresh();
						return Status.OK_STATUS;
					} catch (Throwable throwable) { // NOPMD
						throw new RuntimeException("Unknown exception occurred trying to refresh the view.", throwable);
					} finally {
						jobInSchedule = false;
					}
				}
			};
			job.schedule();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * Returns an instance of a {@link TextInputController}.
	 * 
	 * @return An instance of a {@link TextInputController}.
	 */
	public TextInputController getTextInputController() {
		return textInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		if (checkDisposed()) {
			return;
		}

		textInputController.setDataInput(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISubView getSubViewWithInputController(Class<?> inputControllerClass) {
		if (Objects.equals(inputControllerClass, textInputController.getClass())) {
			return this;
		}
		return null;
	}

	/**
	 * Returns true if the composite in the sub-view is disposed. False otherwise.
	 * 
	 * @return Returns true if the composite in the sub-view is disposed. False otherwise.
	 */
	private boolean checkDisposed() {
		return composite.isDisposed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		textInputController.dispose();
	}

}
