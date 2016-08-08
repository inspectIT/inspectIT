package rocks.inspectit.ui.rcp.ci.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.ViewPart;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.job.OpenApplicationDefinitionJob;
import rocks.inspectit.ui.rcp.ci.listener.IApplicationDefinitionChangeListener;
import rocks.inspectit.ui.rcp.model.ci.ApplicationLeaf;
import rocks.inspectit.ui.rcp.provider.IApplicationProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.IRefreshableView;

/**
 * {@link ViewPart} displaying {@link ApplicationDefinition}s.
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextManagerViewPart extends ViewPart implements IRefreshableView {
	/**
	 * View id.
	 */
	public static final String VIEW_ID = "rocks.inspectit.ui.rcp.ci.view.businessContextManager";

	/**
	 * Tool tip text describing the importance of the order of application definitions.
	 */
	public static final String APP_ORDER_INFO_TOOLTIP = "The order of the application definitions impacts the assignment of application labeles to measurement data.\n"
			+ "If there are multiple application definitions that potentially would match a measurement data object,\n" + "then this object is labeled only with the first application in order.\n"
			+ "In general, application definitions with more specific matching rules should be inserted before applications that have rather generic matching rules.";

	/**
	 * Tool tip text describing the importance of the order of business transaction definitions.
	 */
	public static final String B_TX_ORDER_INFO_TOOLTIP = "The order of the business transaction definitions impacts the assignment of business transaction labeles to measurement data.\n"
			+ "If there are multiple business transaction definitions that potentially would match a measurement data object,\n"
			+ "then this object is labeled only with the first business transaction in order.\n"
			+ "In general, business transaction definitions with more specific matching rules should be inserted before business transactions that have rather generic matching rules.";

	/**
	 * View instance.
	 */
	private BusinessContextManagerView managerView;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		managerView = new BusinessContextManagerView(getSite());
		managerView.createControls(parent, true);
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
	 * Select application definition in the table view.
	 *
	 * @param applicationDefinition
	 *            {@link ApplicationDefinition} to select.
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} instance.
	 */
	public void selectApplicationDefinition(ApplicationDefinition applicationDefinition, CmrRepositoryDefinition repositoryDefinition) {
		ApplicationLeaf leaf = new ApplicationLeaf(applicationDefinition, managerView.getApplications(), repositoryDefinition);
		StructuredSelection selection = new StructuredSelection(leaf);
		managerView.select(selection);
	}

	/**
	 * View displaying {@link ApplicationDefinition}s.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class BusinessContextManagerView extends AbstractTableBasedManagerView implements IApplicationDefinitionChangeListener {

		/**
		 * Menu id.
		 */
		public static final String MENU_ID = "rocks.inspectit.ui.rcp.ci.view.businessContextManager";

		/**
		 * List of {@link ApplicationLeaf}s to show.
		 */
		private List<ApplicationLeaf> applications;

		/**
		 * Default constructor.
		 *
		 * @param workbenchPartSite
		 *            The {@link IWorkbenchPartSite} the view is showed in.
		 */
		BusinessContextManagerView(IWorkbenchPartSite workbenchPartSite) {
			super(workbenchPartSite);
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addApplicationDefinitionChangeListener(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applicationCreated(ApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, getApplications(), repositoryDefinition);
			getApplications().add(positionIndex, leaf);
			StructuredSelection selection = new StructuredSelection(leaf);
			performUpdate(true, selection);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applicationMoved(ApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, getApplications(), repositoryDefinition);
			getApplications().remove(oldPositionIndex);
			getApplications().add(newPositionIndex, leaf);
			StructuredSelection selection = new StructuredSelection(leaf);
			performUpdate(false, selection);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applicationUpdated(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, getApplications(), repositoryDefinition);
			getApplications().set(leaf.getIndexInParentList(), leaf);
			performUpdate(false, null);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applicationDeleted(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
			int index = 0;
			for (ApplicationLeaf appLeaf : getApplications()) {
				if (appLeaf.getApplication().getId() == application.getId()) {
					break;
				}
				index++;
			}
			if (index < getApplications().size()) {
				getApplications().remove(index);
			}
			performUpdate(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeApplicationDefinitionChangeListener(this);
			super.dispose();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IBaseLabelProvider getLabelProvider() {
			return new ApplicationLabelProvider();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getMenuId() {
			return MENU_ID;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateContent() {
			getApplications().clear();
			if ((null != displayedCmrRepositoryDefinition) && (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE)) {

				List<ApplicationDefinition> applicationDefinitions = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getApplicationDefinitions();

				for (ApplicationDefinition appDef : applicationDefinitions) {
					getApplications().add(new ApplicationLeaf(appDef, getApplications(), displayedCmrRepositoryDefinition));
				}
			}
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
						if (selected instanceof IApplicationProvider) {
							new OpenApplicationDefinitionJob(displayedCmrRepositoryDefinition, ((IApplicationProvider) selected).getApplication().getId(), getWorkbenchSite().getPage()).schedule();
						}
					}
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean matchesContentType(Object object) {
			return object instanceof ApplicationLeaf;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<?> getTableInput() {
			return getApplications();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createTableColumns(TableViewer tableViewer) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Application");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Application name.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Business Transactions");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Number of business transactions in the application.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Description");
			viewerColumn.getColumn().setWidth(200);
			viewerColumn.getColumn().setToolTipText("Description.");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createHeadClient(Form form) {
			Composite infoComposite = new Composite(form.getHead(), SWT.NONE);
			infoComposite.setLayout(new GridLayout(2, false));
			Label infoLabel = new Label(infoComposite, SWT.NONE);
			infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			infoLabel.setToolTipText(BusinessContextManagerViewPart.APP_ORDER_INFO_TOOLTIP);
			Label infoLabelText = new Label(infoComposite, SWT.NONE);
			infoLabelText.setText("Pay attention to the order of application definitions!");
			infoLabelText.setToolTipText(BusinessContextManagerViewPart.APP_ORDER_INFO_TOOLTIP);
			form.setHeadClient(infoComposite);
		}

		/**
		 * Gets {@link #applications}.
		 *
		 * @return {@link #applications}
		 */
		private List<ApplicationLeaf> getApplications() {
			if (null == applications) {
				applications = new ArrayList<>();
			}
			return applications;
		}
	}

}
