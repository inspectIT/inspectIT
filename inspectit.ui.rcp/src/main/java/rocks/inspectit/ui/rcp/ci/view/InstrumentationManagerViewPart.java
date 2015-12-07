package rocks.inspectit.ui.rcp.ci.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.ViewPart;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.job.OpenEnvironmentJob;
import rocks.inspectit.ui.rcp.ci.job.OpenProfileJob;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IProfileChangeListener;
import rocks.inspectit.ui.rcp.model.ci.EnvironmentLeaf;
import rocks.inspectit.ui.rcp.model.ci.ProfileLeaf;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.IRefreshableView;

/**
 * {@link ViewPart} displaying {@link Profile}s and {@link Environment}s.
 *
 * @author Ivan Senic, Alexander Wert
 *
 */
public class InstrumentationManagerViewPart extends ViewPart implements IRefreshableView {

	/**
	 * View id.
	 */
	public static final String VIEW_ID = "rocks.inspectit.ui.rcp.ci.view.instrumentationManager";

	/**
	 * View instance.
	 */
	private AbstractTableBasedManagerView managerView;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		managerView = new InstrumentationManagerView(getSite());
		managerView.createControls(parent, false);
		getSite().setSelectionProvider(managerView.getSelectionProviderAdapter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		managerView.setFocus();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		managerView.dispose();
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		managerView.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return managerView.canRefresh();
	}

	/**
	 * View displaying {@link Profile}s and {@link Environment}s.
	 *
	 * @author Ivan Senic, Alexander Wert
	 *
	 */
	private static class InstrumentationManagerView extends AbstractTableBasedManagerView implements IProfileChangeListener, IEnvironmentChangeListener {

		/**
		 * Menu to be bounded.
		 */
		private static final String MENU_ID = "rocks.inspectit.ui.rcp.view.instrumentationManager.table";

		/**
		 * Input list of profiles.
		 */
		private List<IProfileProvider> profileLeafs = new ArrayList<>(0);

		/**
		 * Input list of environments.
		 */
		private List<IEnvironmentProvider> environmentLeafs = new ArrayList<>(0);

		/**
		 * Button for environment selection.
		 */
		private Button environmentSelection;

		/**
		 * Button for profile selection.
		 */
		private Button profileSelection;

		/**
		 * Default constructor.
		 *
		 * @param workbenchPartSite
		 *            The {@link IWorkbenchPartSite} the view is showed in.
		 */
		InstrumentationManagerView(IWorkbenchPartSite workbenchPartSite) {
			super(workbenchPartSite);
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(this);
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void createControls(Composite parent, boolean multiSelection) {
			super.createControls(parent, multiSelection);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createHeadClient(Form form) {
			Composite headClient = new Composite(form.getHead(), SWT.NONE);
			GridLayout gl = new GridLayout(3, false);
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			headClient.setLayout(gl);

			new Label(headClient, SWT.NONE).setText("Show:");

			environmentSelection = new Button(headClient, SWT.RADIO);
			environmentSelection.setText("Environments");
			environmentSelection.setSelection(true);

			profileSelection = new Button(headClient, SWT.RADIO);
			profileSelection.setText("Profiles");

			SelectionAdapter listener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (null != displayedCmrRepositoryDefinition) {
						selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
					}
					performUpdate(false);
				}
			};
			environmentSelection.addSelectionListener(listener);
			profileSelection.addSelectionListener(listener);

			form.setHeadClient(headClient);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createTableColumns(TableViewer tableViewer) {
			if (isShowEnvironments()) {
				// columns when environments are displayed
				TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Environment");
				viewerColumn.getColumn().setWidth(150);
				viewerColumn.getColumn().setToolTipText("Environment name.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Updated");
				viewerColumn.getColumn().setWidth(80);
				viewerColumn.getColumn().setToolTipText("The date and time when the environment was last time updated.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Profiles");
				viewerColumn.getColumn().setWidth(60);
				viewerColumn.getColumn().setToolTipText("Number of linked profiles in the Environment.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Description");
				viewerColumn.getColumn().setWidth(150);
				viewerColumn.getColumn().setToolTipText("Environment description.");

			} else if (isShowProfiles()) {
				// columns when profiles are displayed
				TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Profile");
				viewerColumn.getColumn().setWidth(150);
				viewerColumn.getColumn().setToolTipText("Profile name.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Updated");
				viewerColumn.getColumn().setWidth(80);
				viewerColumn.getColumn().setToolTipText("The date and time when the profile was last time updated.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Active");
				viewerColumn.getColumn().setWidth(40);
				viewerColumn.getColumn()
				.setToolTipText("If profile is active or not, note that deactivated profile will not be considered during the instrumentation even if it's a part of an Environment.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Default");
				viewerColumn.getColumn().setWidth(40);
				viewerColumn.getColumn().setToolTipText("If profile is default or not, note that default profile will be added to any new created Environment.");

				TableViewerColumn typeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				typeColumn.getColumn().setMoveable(true);
				typeColumn.getColumn().setResizable(true);
				typeColumn.getColumn().setText("Type");
				typeColumn.getColumn().setWidth(40);
				typeColumn.getColumn().setToolTipText("Type of data profile is holding.");

				viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				viewerColumn.getColumn().setMoveable(true);
				viewerColumn.getColumn().setResizable(true);
				viewerColumn.getColumn().setText("Description");
				viewerColumn.getColumn().setWidth(250);
				viewerColumn.getColumn().setToolTipText("Profile description.");
			}
			tableViewer.setLabelProvider(getLabelProvider());
			ViewerComparator comparator = getViewerComparator();
			if (null != comparator) {
				tableViewer.setComparator(getViewerComparator());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IBaseLabelProvider getLabelProvider() {
			if (isShowEnvironments()) {
				return new EnvironmentLabelProvider();
			} else if (isShowProfiles()) {
				return new ProfileLabelProvider();
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IDoubleClickListener getDoubleClickListener() {
			return new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					ISelection selection = event.getSelection();
					if (!selection.isEmpty()) {
						Object selected = ((StructuredSelection) selection).getFirstElement();
						if (selected instanceof IProfileProvider) {
							// open profile job
							IProfileProvider profileProvider = (IProfileProvider) selected;
							new OpenProfileJob(profileProvider.getCmrRepositoryDefinition(), profileProvider.getProfile().getId(), getWorkbenchSite().getPage()).schedule();
						} else if (selected instanceof IEnvironmentProvider) {
							IEnvironmentProvider environmentProvider = (IEnvironmentProvider) selected;
							new OpenEnvironmentJob(environmentProvider.getCmrRepositoryDefinition(), environmentProvider.getEnvironment().getId(), getWorkbenchSite().getPage()).schedule();
						}
					}
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			ProfileLeaf profileLeaf = new ProfileLeaf(profile, repositoryDefinition);
			profileLeafs.add(profileLeaf);
			showProfiles(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			for (Iterator<IProfileProvider> it = profileLeafs.iterator(); it.hasNext();) {
				IProfileProvider provider = it.next();
				if (Objects.equals(profile.getId(), provider.getProfile().getId())) {
					it.remove();
					profileLeafs.add(new ProfileLeaf(profile, provider.getCmrRepositoryDefinition()));

					performUpdate(false);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			for (Iterator<IProfileProvider> it = profileLeafs.iterator(); it.hasNext();) {
				IProfileProvider provider = it.next();
				if (Objects.equals(profile.getId(), provider.getProfile().getId())) {
					it.remove();

					performUpdate(false);
					break;
				}
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			EnvironmentLeaf environmentLeaf = new EnvironmentLeaf(environment, repositoryDefinition);
			environmentLeafs.add(environmentLeaf);
			showEnvironments(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			for (Iterator<IEnvironmentProvider> it = environmentLeafs.iterator(); it.hasNext();) {
				IEnvironmentProvider provider = it.next();
				if (Objects.equals(environment.getId(), provider.getEnvironment().getId())) {
					it.remove();
					environmentLeafs.add(new EnvironmentLeaf(environment, repositoryDefinition));

					performUpdate(false);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
			if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
				return;
			}

			for (Iterator<IEnvironmentProvider> it = environmentLeafs.iterator(); it.hasNext();) {
				IEnvironmentProvider provider = it.next();
				if (Objects.equals(environment.getId(), provider.getEnvironment().getId())) {
					it.remove();

					performUpdate(false);
					break;
				}
			}
		}

		/**
		 * @return Are environments displayed.
		 */
		private boolean isShowEnvironments() {
			return environmentSelection.getSelection();
		}

		/**
		 * @return Are profiles displayed.
		 */
		private boolean isShowProfiles() {
			return profileSelection.getSelection();
		}

		/**
		 * Makes the view show the environments.
		 *
		 * @param update
		 *            If update of the input should be made.
		 */
		public void showEnvironments(boolean update) {
			if (!isShowEnvironments()) {
				environmentSelection.setSelection(true);
				profileSelection.setSelection(false);
			}

			performUpdate(update);
		}

		/**
		 * Makes the view show the profiles.
		 *
		 * @param update
		 *            If update of the input should be made.
		 */
		public void showProfiles(boolean update) {
			if (!isShowProfiles()) {
				profileSelection.setSelection(true);
				environmentSelection.setSelection(false);
			}

			performUpdate(update);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getMenuId() {
			return MENU_ID;
		};

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeProfileChangeListener(this);
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
			super.dispose();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean matchesContentType(Object object) {
			return (object instanceof IProfileProvider) || (object instanceof IEnvironmentProvider);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateContent() {
			// reset lists
			profileLeafs = new ArrayList<>();
			environmentLeafs = new ArrayList<>();
			if (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {

				// profiles
				List<Profile> profiles = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
				for (Profile profile : profiles) {
					profileLeafs.add(new ProfileLeaf(profile, displayedCmrRepositoryDefinition));
				}

				// environment
				Collection<Environment> environments = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
				for (Environment environment : environments) {
					environmentLeafs.add(new EnvironmentLeaf(environment, displayedCmrRepositoryDefinition));
				}
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<?> getTableInput() {
			if (isShowEnvironments()) {
				return environmentLeafs;
			} else if (isShowProfiles()) {
				return profileLeafs;
			}
			return null;
		}

	}
}
