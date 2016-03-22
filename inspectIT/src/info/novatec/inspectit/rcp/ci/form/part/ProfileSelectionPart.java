package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.ci.job.OpenProfileJob;
import info.novatec.inspectit.rcp.ci.listener.IProfileChangeListener;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.filter.FilterComposite;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Profile selection for the environment.
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileSelectionPart extends SectionPart implements IProfileChangeListener, IPropertyListener {

	/**
	 * Repository needed for loading all {@link Profile}s.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * Profiles environment can be linked to.
	 */
	private List<Profile> profiles;

	/**
	 * {@link FormPage} section belongs to.
	 */
	private FormPage formPage;

	/**
	 * Table displaying the profiles.
	 */
	private TableViewer tableViewer;

	/**
	 * Default constructor.
	 * 
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public ProfileSelectionPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.cmrRepositoryDefinition = input.getCmrRepositoryDefinition();
		this.environment = input.getEnvironment();
		this.profiles = new ArrayList<>(input.getProfiles());
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createClient(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Profiles");
		Label label = toolkit.createLabel(getSection(), "Select profiles to use within environment");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);

		// profile change listener
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(this);
	}

	/**
	 * Creates complete client.
	 * 
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createClient(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		mainComposite.setLayout(new GridLayout(1, true));
		section.setClient(mainComposite);

		// filter first
		FilterProfileComposite filterProfileComposite = new FilterProfileComposite(mainComposite, SWT.NONE);
		filterProfileComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toolkit.adapt(filterProfileComposite);

		// table
		Table table = toolkit.createTable(mainComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new ProfileLabelProvider());
		tableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Profile && e2 instanceof Profile) {
					int res = Boolean.compare(((Profile) e1).isCommonProfile(), ((Profile) e2).isCommonProfile());
					if (0 != res) {
						return res;
					}

					return ((Profile) e1).getName().compareToIgnoreCase(((Profile) e2).getName());
				}
				return 0;
			}
		});
		tableViewer.setInput(profiles);
		tableViewer.refresh();

		updateCheckedItems();

		// filter
		tableViewer.addFilter(filterProfileComposite.getFilter());

		// dirty listener
		tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK && !isDirty()) {
					markDirty();
				}
			}
		});

		MenuManager menuManager = new MenuManager();
		menuManager.add(new EditProfileAction());
		Menu menu = menuManager.createContextMenu(table);
		table.setMenu(menu);
	}

	/**
	 * Creates columns for table.
	 */
	private void createColumns() {
		TableViewerColumn selectedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		selectedColumn.getColumn().setResizable(false);
		selectedColumn.getColumn().setWidth(40);
		selectedColumn.getColumn().setText("Selected");
		selectedColumn.getColumn().setToolTipText("If profile is included in the Environment.");

		TableViewerColumn profileNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		profileNameColumn.getColumn().setResizable(true);
		profileNameColumn.getColumn().setWidth(250);
		profileNameColumn.getColumn().setText("Profile");
		profileNameColumn.getColumn().setToolTipText("Profile name.");

		TableViewerColumn activeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		activeColumn.getColumn().setMoveable(true);
		activeColumn.getColumn().setResizable(true);
		activeColumn.getColumn().setText("Active");
		activeColumn.getColumn().setWidth(60);
		activeColumn.getColumn().setToolTipText("If profile is active or not, note that deactivated profile will not be considered during the instrumentation even if it's a part of an Environment.");

		TableViewerColumn defaultColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		defaultColumn.getColumn().setMoveable(true);
		defaultColumn.getColumn().setResizable(true);
		defaultColumn.getColumn().setText("Default");
		defaultColumn.getColumn().setWidth(60);
		defaultColumn.getColumn().setToolTipText("If profile is default or not, note that default profile will be added to any new created Environment.");

		TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		descriptionColumn.getColumn().setMoveable(true);
		descriptionColumn.getColumn().setResizable(true);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.getColumn().setWidth(150);
		descriptionColumn.getColumn().setToolTipText("Profile description.");
	}

	/**
	 * Updates states of the check boxes next to the elements.
	 */
	private void updateCheckedItems() {
		for (TableItem item : tableViewer.getTable().getItems()) {
			Profile data = (Profile) item.getData();
			item.setChecked(environment.getProfileIds().contains(data.getId()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			Set<String> profileIds = new HashSet<>(1);
			for (TableItem item : tableViewer.getTable().getItems()) {
				if (item.getChecked()) {
					profileIds.add(((Profile) item.getData()).getId());
				}
			}
			environment.setProfileIds(profileIds);
		}
	}

	/**
	 * Profile label provider.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class ProfileLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Empty.
		 */
		private static final StyledString EMPTY = new StyledString();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			if (element instanceof Profile) {
				Profile profile = ((Profile) element);
				switch (index) {
				case 1:
					return new StyledString(profile.getName());
				case 4:
					return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(profile.getDescription()));
				default:
					return EMPTY;
				}
			}
			return EMPTY;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			if (element instanceof Profile) {
				Profile profile = ((Profile) element);
				switch (index) {
				case 1:
					return ImageFormatter.getProfileImage(profile);
				case 2:
					return profile.isActive() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
				case 3:
					return profile.isDefaultProfile() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
				default:
					return super.getColumnImage(element, index);
				}
			}
			return super.getColumnImage(element, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getForeground(Object element, int index) {
			if (element instanceof Profile) {
				Profile profile = ((Profile) element);
				if (profile.isCommonProfile()) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
				}
			}
			return super.getForeground(element, index);
		}
	}

	/**
	 * Action for editing the profile.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class EditProfileAction extends Action {

		/**
		 * Default constructor.
		 */
		public EditProfileAction() {
			setText("Edit");
			setToolTipText("Edit Profile");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
			if (!selection.isEmpty()) {
				Profile profile = (Profile) selection.getFirstElement();
				new OpenProfileJob(cmrRepositoryDefinition, profile.getId(), formPage.getSite().getPage()).schedule();
			}
		};
	}

	/**
	 * Implementation of the filter for the profiles.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class FilterProfileComposite extends FilterComposite {

		/**
		 * String to be filtered.
		 */
		private String filterString = "";

		/**
		 * Filter.
		 */
		private ViewerFilter filter = new ViewerFilter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Objects.equals("", filterString)) {
					return true;
				} else {
					if (element instanceof Profile) {
						return select(((Profile) element));
					}
					return true;
				}
			}

			/**
			 * Does a filter select on {@link Profile}.
			 * 
			 * @param profile
			 *            {@link Profile}
			 * @return True if data in {@link Profile} fits the filter string.
			 */
			private boolean select(Profile profile) {
				return StringUtils.containsIgnoreCase(profile.getName(), filterString);
			}

		};

		/**
		 * @param parent
		 *            Parent
		 * @param style
		 *            Style
		 */
		public FilterProfileComposite(Composite parent, int style) {
			super(parent, style, "Filter profiles");
			((GridLayout) getLayout()).marginWidth = 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeCancel() {
			this.filterString = "";
			tableViewer.refresh();
			updateCheckedItems();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeFilter(String filterString) {
			this.filterString = filterString;
			tableViewer.refresh();
			updateCheckedItems();
		}

		/**
		 * Gets {@link #filter}.
		 * 
		 * @return {@link #filter}
		 */
		public ViewerFilter getFilter() {
			return filter;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		CmrRepositoryDefinition displayedCmrRepositoryDefinition = ((EnvironmentEditorInput) formPage.getEditor().getEditorInput()).getCmrRepositoryDefinition();
		if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
			return;
		}

		profiles.add(profile);
		SafeExecutor.asyncExec(new Runnable() {
			@Override
			public void run() {
				tableViewer.refresh();
				updateCheckedItems();
			}
		}, tableViewer.getTable());
	}

	@Override
	public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
		CmrRepositoryDefinition displayedCmrRepositoryDefinition = ((EnvironmentEditorInput) formPage.getEditor().getEditorInput()).getCmrRepositoryDefinition();
		if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
			return;
		}

		for (Iterator<Profile> it = profiles.iterator(); it.hasNext();) {
			Profile displayed = it.next();
			if (Objects.equals(displayed.getId(), profile.getId())) {
				it.remove();
				profiles.add(profile);

				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						tableViewer.refresh();
						updateCheckedItems();
					}
				}, tableViewer.getTable());

				break;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		CmrRepositoryDefinition displayedCmrRepositoryDefinition = ((EnvironmentEditorInput) formPage.getEditor().getEditorInput()).getCmrRepositoryDefinition();
		if (!Objects.equals(repositoryDefinition, displayedCmrRepositoryDefinition)) {
			return;
		}

		for (Iterator<Profile> it = profiles.iterator(); it.hasNext();) {
			final Profile displayed = it.next();
			if (Objects.equals(displayed.getId(), profile.getId())) {
				it.remove();

				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						tableViewer.remove(displayed);
					}
				}, tableViewer.getTable());

				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeProfileChangeListener(this);
		super.dispose();
	}

}
