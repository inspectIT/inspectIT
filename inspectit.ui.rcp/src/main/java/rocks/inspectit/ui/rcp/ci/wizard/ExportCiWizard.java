package rocks.inspectit.ui.rcp.ci.wizard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.esotericsoftware.kryo.io.Output;
import com.sun.org.apache.xerces.internal.utils.Objects;

import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceExportData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectEnvironmentsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectProfilesWizardPage;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.wizard.page.SelectFileWizardPage;

/**
 * Wizard for exporting CI environments and/or profiles.
 *
 * @author Ivan Senic
 *
 */
public class ExportCiWizard extends Wizard implements IExportWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Export Instrumentation Configuration";

	/**
	 * {@link CmrRepositoryDefinition} to export data from.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Flag to denote only profiles export.
	 */
	private boolean profilesOnly;

	/**
	 * Existing environments on the CMR.
	 */
	private Collection<Environment> environments = Collections.emptyList();

	/**
	 * Existing profiles on the CMR.
	 */
	private Collection<Profile> profiles = Collections.emptyList();

	/**
	 * Selected environments.
	 */
	private final Collection<String> selectedEnvironmentsIds = new HashSet<>();

	/**
	 * Selected profiles.
	 */
	private final Collection<String> selectedProfilesIds = new HashSet<>();

	/**
	 * {@link SelectEnvironmentsWizardPage}.
	 */
	private SelectEnvironmentsWizardPage selectEnvironmentsWizardPage;

	/**
	 * {@link SelectProfilesWizardPage}.
	 */
	private SelectProfilesWizardPage selectProfilesWizardPage;

	/**
	 * {@link SelectFileWizardPage}.
	 */
	private SelectFileWizardPage selectFileWizardPage;

	/**
	 * Default constructor.
	 */
	public ExportCiWizard() {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_EXPORT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
			environments = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
			profiles = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();

			// remove common profiles as they can not be exported
			for (Iterator<Profile> it = profiles.iterator(); it.hasNext();) {
				if (it.next().isCommonProfile()) {
					it.remove();
				}

			}
		}

		for (Object selected : structuredSelection.toArray()) {
			if (selected instanceof IProfileProvider) {
				profilesOnly = true;
				selectedProfilesIds.add(((IProfileProvider) selected).getProfile().getId());
			}
		}

		for (Object selected : structuredSelection.toArray()) {
			if (selected instanceof IEnvironmentProvider) {
				profilesOnly = false;
				selectedEnvironmentsIds.add(((IEnvironmentProvider) selected).getEnvironment().getId());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		if (!profilesOnly) {
			selectEnvironmentsWizardPage = new SelectEnvironmentsWizardPage("Export Environment(s)", "Select environments to be exported.", environments, selectedEnvironmentsIds);
			addPage(selectEnvironmentsWizardPage);
		}

		selectProfilesWizardPage = new SelectProfilesWizardPage("Export Profile(s)", "Select profiles to be exported.", profiles, selectedProfilesIds);
		addPage(selectProfilesWizardPage);

		selectFileWizardPage = new SelectFileWizardPage("Export Location", "Define file to export data to.", new String[] { "*" + ConfigurationInterfaceExportData.FILE_EXTENSION }, "", SWT.SAVE);
		addPage(selectFileWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (Objects.equals(page, selectEnvironmentsWizardPage)) {
			Collection<String> selectedIds = new HashSet<>(selectedProfilesIds);
			for (Environment environment : selectEnvironmentsWizardPage.getEnvironments()) {
				selectedIds.addAll(environment.getProfileIds());
			}
			selectProfilesWizardPage.setSelectedIds(selectedIds);
		}

		return super.getNextPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		ConfigurationInterfaceExportData exportData = new ConfigurationInterfaceExportData();
		exportData.setProfiles(selectProfilesWizardPage.getProfiles());
		if (!profilesOnly) {
			exportData.setEnvironments(selectEnvironmentsWizardPage.getEnvironments());
		}

		String fileName = selectFileWizardPage.getFileName();
		SerializationManagerProvider serializationManagerProvider = InspectIT.getService(SerializationManagerProvider.class);
		SerializationManager serializer = serializationManagerProvider.createSerializer();
		try (Output output = new Output(Files.newOutputStream(Paths.get(fileName), StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
			serializer.serialize(exportData, output);
		} catch (IOException | SerializationException e) {
			InspectIT.getDefault().createErrorDialog("Error exporting instrumentation configuration data.", e, -1);
			return false;
		}

		return true;
	}

}
