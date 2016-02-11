package info.novatec.inspectit.rcp.ci.form.editor;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.form.page.ExcludeRulesPage;
import info.novatec.inspectit.rcp.ci.form.page.MethodSensorDefinitionsPage;
import info.novatec.inspectit.rcp.ci.listener.IProfileChangeListener;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * {@link Profile} editor. This is a {@link FormEditor} consisted out of following pages:
 * <ul>
 * <li>{@link MethodSensorDefinitionsPage}
 * <li>{@link ExcludeRulesPage}
 * </ul>
 * 
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileEditor extends AbstractConfigurationInterfaceFormEditor implements IProfileChangeListener {

	/**
	 * Editor ID.
	 */
	public static final String ID = "info.novatec.inspectit.rcp.ci.editor.profileEditor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof ProfileEditorInput)) {
			throw new PartInitException("Editor input must be of a type: " + ProfileEditorInput.class.getName());
		}

		setSite(site);
		setInput(input);

		ProfileEditorInput profileEditorInput = (ProfileEditorInput) input;
		setPartName(profileEditorInput.getName());
		setTitleImage(ImageFormatter.getProfileImage(profileEditorInput.getProfile()));

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			int index = 0;
			Profile profile = ((ProfileEditorInput) getEditorInput()).getProfile();

			// not display all pages if it is common profile
			// first MethodSensorDefinitionsPage
			if (!profile.isCommonProfile() || CollectionUtils.isNotEmpty(profile.getMethodSensorAssignments()) || CollectionUtils.isNotEmpty(profile.getExceptionSensorAssignments())) {
				addPage(new MethodSensorDefinitionsPage(this));
				setPageImage(index++, InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER));
			}
			// then ExcludeRulesPage
			if (!profile.isCommonProfile() || CollectionUtils.isNotEmpty(profile.getExcludeRules())) {
				addPage(new ExcludeRulesPage(this));
				setPageImage(index++, InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_EXCLUDE));
			}
		} catch (PartInitException e) {
			InspectIT.getDefault().log(IStatus.ERROR, "Error occurred trying to open the Environment editor.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Saving profile..", IProgressMonitor.UNKNOWN);

		if (!checkValid()) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}

		commitPages(true);

		ProfileEditorInput profileEditorInput = (ProfileEditorInput) getEditorInput();
		CmrRepositoryDefinition cmrRepositoryDefinition = profileEditorInput.getCmrRepositoryDefinition();
		Profile profile = profileEditorInput.getProfile();

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				commitPages(true);

				Profile updated = cmrRepositoryDefinition.getConfigurationInterfaceService().updateProfile(profile);

				// notify listeners
				if (null != updated) {
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileUpdated(updated, cmrRepositoryDefinition, false);
				}

				// set no exception and fire dirty state changed
				setExceptionOnSave(false);
				editorDirtyStateChanged();
			} catch (BusinessException e) {
				monitor.setCanceled(true);
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Saving of the profile '" + profile.getName() + "' failed due to the exception on the CMR.", e, -1);
			} catch (Throwable t) { // NOPMD
				monitor.setCanceled(true);
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to save the profile '" + profile.getName() + "'.", t, -1);
			}
		} else {
			monitor.setCanceled(true);
			InspectIT.getDefault().createErrorDialog("Saving of the profile '" + profile.getName() + "' failed because CMR is currently not online.", -1);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		// not interesting
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
		// if our profile is updated, set new input
		ProfileEditorInput input = (ProfileEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getProfile().getId(), profile.getId())) {
			final ProfileEditorInput newInput = new ProfileEditorInput(profile, input.getCmrRepositoryDefinition());
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setPartName(newInput.getName());
					setTitleImage(ImageFormatter.getProfileImage(newInput.getProfile()));
					setInputWithNotify(newInput);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		// if our profile has been deleted just close editor
		ProfileEditorInput input = (ProfileEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getProfile().getId(), profile.getId())) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					close(false);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeProfileChangeListener(this);
		super.dispose();
	}

}
