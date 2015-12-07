package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.job.OpenApplicationDefinitionJob;
import info.novatec.inspectit.rcp.ci.listener.IApplicationDefinitionChangeListener;
import info.novatec.inspectit.rcp.model.ci.ApplicationLeaf;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link ViewPart} displaying {@link IApplicationDefinition}s.
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextManagerViewPart extends ViewPart {
	/**
	 * View id.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.ci.view.businessContextManager";

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
	private AbstractTableBasedManagerView managerView;

	@Override
	public void createPartControl(Composite parent) {
		managerView = new BusinessContextManagerView(getSite());
		managerView.createControls(parent, true);
		getSite().setSelectionProvider(managerView.getSelectionProviderAdapter());

	}

	@Override
	public void setFocus() {
		managerView.setFocus();

	}

	@Override
	public void dispose() {
		managerView.dispose();
		super.dispose();
	}

	/**
	 * View displaying {@link IApplicationDefinition}s.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class BusinessContextManagerView extends AbstractTableBasedManagerView implements IApplicationDefinitionChangeListener {

		/**
		 * Menu id.
		 */
		public static final String MENU_ID = "info.novatec.inspectit.rcp.ci.view.businessContextManager";

		/**
		 * List of {@link ApplicationLeaf}s to show.
		 */
		private List<ApplicationLeaf> applications = new ArrayList<ApplicationLeaf>();

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

		@Override
		protected IBaseLabelProvider getLabelProvider() {
			return new ApplicationLabelProvider();
		}

		@Override
		protected String getMenuId() {
			return MENU_ID;
		}

		@Override
		protected void updateContent() {
			applications = new ArrayList<ApplicationLeaf>();
			if (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {

				List<IApplicationDefinition> applicationDefinitions = displayedCmrRepositoryDefinition.getBusinessContextMangementService().getApplicationDefinitions();

				for (IApplicationDefinition appDef : applicationDefinitions) {
					applications.add(new ApplicationLeaf(appDef, applications, displayedCmrRepositoryDefinition));
				}
			}
		}

		@Override
		protected IDoubleClickListener getDoubleClickListener() {
			return new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					ISelection selection = event.getSelection();
					if (!selection.isEmpty()) {
						Object selected = ((StructuredSelection) selection).getFirstElement();
						if (selected instanceof ApplicationLeaf && ((ApplicationLeaf) selected).getApplication().getId() != IApplicationDefinition.DEFAULT_ID) {
							new OpenApplicationDefinitionJob(displayedCmrRepositoryDefinition, ((ApplicationLeaf) selected).getApplication(), getWorkbenchSite().getPage()).schedule();
						} else if (selected instanceof ApplicationLeaf && ((ApplicationLeaf) selected).getApplication().getId() == IApplicationDefinition.DEFAULT_ID) {
							MessageBox confirm = new MessageBox(Display.getCurrent().getActiveShell(), SWT.OK | SWT.ICON_INFORMATION);
							confirm.setText("Edit Mode Not Supported");
							confirm.setMessage("The default application cannot be modified!");
							confirm.open();
						}
					}
				}
			};
		}

		@Override
		protected boolean matchesContentType(Object object) {
			return object instanceof ApplicationLeaf;
		}

		@Override
		protected List<?> getTableInput() {
			return applications;
		}

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

		@Override
		public void applicationCreated(IApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, applications, repositoryDefinition);
			applications.add(positionIndex, leaf);
			StructuredSelection selection = new StructuredSelection(leaf);
			performUpdate(true, selection);
		}

		@Override
		public void applicationMoved(IApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, applications, repositoryDefinition);
			if (oldPositionIndex != newPositionIndex) {
				applications.remove(oldPositionIndex);
				applications.add(newPositionIndex, leaf);
			} else {
				applications.set(newPositionIndex, leaf);
			}
			StructuredSelection selection = new StructuredSelection(leaf);
			performUpdate(false, selection);
		}

		@Override
		public void applicationUpdated(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
			ApplicationLeaf leaf = new ApplicationLeaf(application, applications, repositoryDefinition);
			applications.set(leaf.getIndexInParentList(), leaf);
			StructuredSelection selection = new StructuredSelection(leaf);
			performUpdate(false, selection);
		}

		@Override
		public void applicationDeleted(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
			performUpdate(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeApplicationDefinitionChangeListener(this);
			super.dispose();
		}

	}

}
