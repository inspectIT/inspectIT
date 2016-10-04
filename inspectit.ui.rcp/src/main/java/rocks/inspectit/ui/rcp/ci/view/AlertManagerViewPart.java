package rocks.inspectit.ui.rcp.ci.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.listener.IAlertDefinitionChangeListener;
import rocks.inspectit.ui.rcp.ci.wizard.EditAlertDefinitionWizard;
import rocks.inspectit.ui.rcp.model.ci.AlertDefinitionLeaf;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.IRefreshableView;

/**
 * {@link ViewPart} displaying Alert Definitions.
 *
 * @author Alexander Wert
 *
 */
public class AlertManagerViewPart extends ViewPart implements IRefreshableView {

	/**
	 * View id.
	 */
	public static final String VIEW_ID = "rocks.inspectit.ui.rcp.ci.view.alertManager";

	/**
	 * View instance.
	 */
	private AlertManagerView managerView;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		managerView = new AlertManagerView(getSite());
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
	 * View displaying alert definitions.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class AlertManagerView extends AbstractTableBasedManagerView implements IAlertDefinitionChangeListener {

		/**
		 * A list of {@link AlertDefinitionLeaf}s displayed in this view.
		 */
		private List<AlertDefinitionLeaf> alertDefinitions = new ArrayList<>();

		/**
		 * Menu id.
		 */
		public static final String MENU_ID = "rocks.inspectit.ui.rcp.ci.view.alertManager";

		/**
		 * Default constructor.
		 *
		 * @param workbenchPartSite
		 *            The {@link IWorkbenchPartSite} the view is showed in.
		 */
		AlertManagerView(IWorkbenchPartSite workbenchPartSite) {
			super(workbenchPartSite);
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addAlertDefinitionChangeListener(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean matchesContentType(Object object) {
			return object instanceof AlertDefinitionLeaf;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IBaseLabelProvider getLabelProvider() {
			return new AlertLabelProvider();
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
			alertDefinitions.clear();
			if ((null != displayedCmrRepositoryDefinition) && (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE)) {
				List<AlertingDefinition> cmrAlertDefinitions = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getAlertingDefinitions();
				for (AlertingDefinition alertDef : cmrAlertDefinitions) {
					alertDefinitions.add(new AlertDefinitionLeaf(alertDef, displayedCmrRepositoryDefinition));
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<?> getTableInput() {
			return alertDefinitions;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createTableColumns(TableViewer tableViewer) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Name");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Alert definition name.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Metric");
			viewerColumn.getColumn().setWidth(200);
			viewerColumn.getColumn().setToolTipText("Source of the metric the alerting is applied on.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Threshold");
			viewerColumn.getColumn().setWidth(80);
			viewerColumn.getColumn().setToolTipText("Threshold value.");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IDoubleClickListener getDoubleClickListener() {
			return new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					EditAlertDefinitionWizard wizard = new EditAlertDefinitionWizard();
					wizard.init(getWorkbenchSite().getWorkbenchWindow().getWorkbench(), selection);
					WizardDialog wizardDialog = new WizardDialog(getWorkbenchSite().getShell(), wizard);
					wizardDialog.open();
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void alertDefinitionCreated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
			updateContent();
			StructuredSelection selection = new StructuredSelection(new AlertDefinitionLeaf(alertDefinition, repositoryDefinition));
			performUpdate(true, selection);

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void alertDefinitionUpdated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
			updateContent();
			StructuredSelection selection = new StructuredSelection(new AlertDefinitionLeaf(alertDefinition, repositoryDefinition));
			performUpdate(false, selection);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void alertDefinitionDeleted(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
			updateContent();
			performUpdate(false);
		}

	}
}
