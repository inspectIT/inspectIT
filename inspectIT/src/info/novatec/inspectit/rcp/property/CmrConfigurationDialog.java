package info.novatec.inspectit.rcp.property;

import info.novatec.inspectit.cmr.property.configuration.AbstractProperty;
import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipse.ui.internal.dialogs.PreferenceNodeFilter;

/**
 * Dialog for displaying the CMR configuration.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("restriction")
public class CmrConfigurationDialog extends FilteredPreferenceDialog {

	/**
	 * CMR to configure.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Configuration update that will be available after OK has been clicked if any updates were
	 * made.
	 */
	private ConfigurationUpdate configurationUpdate;

	/**
	 * If updates defined in the dialog need a server restart.
	 */
	private boolean serverRestartRequired;

	/**
	 * Filter for the advanced pages.
	 */
	private ViewerFilter preferenceNodeFilter = new PreferenceNodeFilter(new String[0]);

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Shell to use.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to create dialog for.
	 */
	public CmrConfigurationDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell, getPreferenceManager(cmrRepositoryDefinition));
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Configure CMR '" + cmrRepositoryDefinition.getName() + "' (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Button showAdv = new Button(composite, SWT.CHECK);
		showAdv.setText("Show advanced properties");
		showAdv.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		showAdv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showAdvanced(showAdv.getSelection());
			}
		});

		super.createButtonBar(composite);

		// executed to handle hiding of pages with only advanced properties
		filteredTree.getViewer().setExpandPreCheckFilters(true);
		showAdvanced(false);

		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void okPressed() {
		Set<IPropertyUpdate<?>> propertyUpdates = new HashSet<>();
		Iterator<IPreferenceNode> nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = nodes.next();
			IPreferencePage page = node.getPage();
			if (page instanceof PropertyPreferencePage) {
				PropertyPreferencePage propertyPreferencePage = (PropertyPreferencePage) page;
				propertyUpdates.addAll(propertyPreferencePage.getPropertyUpdates());
				if (propertyPreferencePage.isServerRestartRequired()) {
					serverRestartRequired = true;
				}
			}
		}

		if (CollectionUtils.isNotEmpty(propertyUpdates)) {
			configurationUpdate = new ConfigurationUpdate();
			configurationUpdate.setPropertyUpdates(propertyUpdates);
		}

		super.okPressed();
	}

	/**
	 * Show/hide advanced properties on the pages.
	 * 
	 * @param show
	 *            True if advanced should be shown, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	private void showAdvanced(boolean show) {
		List<String> idsToShow = new ArrayList<>();
		Iterator<IPreferenceNode> nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = nodes.next();
			IPreferencePage page = node.getPage();
			if (page instanceof PropertyPreferencePage) {
				PropertyPreferencePage preferencePage = (PropertyPreferencePage) page;
				preferencePage.showAdvanced(show);
				if (show || showIfNoAdvanced(node)) {
					idsToShow.add(node.getId());
				}
			}
		}

		if (null != preferenceNodeFilter) {
			filteredTree.getViewer().removeFilter(preferenceNodeFilter);
		}
		preferenceNodeFilter = new PreferenceNodeFilter(idsToShow.toArray(new String[idsToShow.size()]));
		filteredTree.getViewer().addFilter(preferenceNodeFilter);

		// switch to another page if currently displayed has only advanced properties
		Collections.sort(idsToShow);
		PropertyPreferencePage currentPage = (PropertyPreferencePage) getCurrentPage();
		if (null != currentPage) {
			if (!show && currentPage.isAllAdvancedProperties()) {
				// display first page
				setCurrentPageId(idsToShow.get(0));
			}
		}
	}

	/**
	 * Defines if node should be displayed if no advanced is selected. This method is recursive.
	 * 
	 * @param node
	 *            Node to check.
	 * @return <code>true</code> this node should be displayed if only normal properties are
	 *         defined.
	 */
	private boolean showIfNoAdvanced(IPreferenceNode node) {
		if (node.getPage() instanceof PropertyPreferencePage) {
			if (!((PropertyPreferencePage) node.getPage()).isAllAdvancedProperties()) {
				return true;
			}
		}

		if (ArrayUtils.isNotEmpty(node.getSubNodes())) {
			for (IPreferenceNode subNode : node.getSubNodes()) {
				if (showIfNoAdvanced(subNode)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets {@link #configurationUpdate}. Note that this method will return <code>null</code> if no
	 * updates were created in the dialog.
	 * 
	 * @return {@link #configurationUpdate}
	 */
	public ConfigurationUpdate getConfigurationUpdate() {
		return configurationUpdate;
	}

	/**
	 * Gets {@link #serverRestartRequired}.
	 * 
	 * @return {@link #serverRestartRequired}
	 */
	public boolean isServerRestartRequired() {
		return serverRestartRequired;
	}

	/**
	 * Creates preference manager for the given {@link CmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return {@link PreferenceManager}.
	 * @throw {@link IllegalArgumentException} If given CMR is <code>null</code>, off-line or can
	 *        not provide active properties.
	 */
	public static PreferenceManager getPreferenceManager(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (null == cmrRepositoryDefinition) {
			throw new IllegalArgumentException("Can not create Preference Manager because repository is null.");
		} else if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			throw new IllegalArgumentException("Can not create Preference Manager because repository is off-line.");
		}

		Collection<PropertySection> sections = cmrRepositoryDefinition.getCmrManagementService().getConfigurationPropertySections();
		if (CollectionUtils.isEmpty(sections)) {
			throw new IllegalArgumentException("Can not create CMR Preference Manager because repository can not provide active properties.");
		}

		PreferenceManager preferenceManager = new PreferenceManager();
		for (PropertySection section : sections) {
			List<IPreferenceNode> subNodes = new ArrayList<>();
			List<SingleProperty<?>> singleProperties = new ArrayList<>();
			for (AbstractProperty property : section.getProperties()) {
				if (property instanceof GroupedProperty) {
					GroupedProperty groupedProperty = (GroupedProperty) property;
					GroupedPropertyPreferencePage preferencePage = new GroupedPropertyPreferencePage(groupedProperty);
					PreferenceNode preferenceNode = new PreferenceNode(groupedProperty.getName(), preferencePage);
					subNodes.add(preferenceNode);
				} else if (property instanceof SingleProperty<?>) {
					singleProperties.add((SingleProperty<?>) property);
				}
			}

			PropertyPreferencePage preferencePage = new PropertyPreferencePage(section.getName(), singleProperties);
			PreferenceNode preferenceNode = new PreferenceNode(section.getName(), preferencePage);
			preferenceManager.addToRoot(preferenceNode);

			for (IPreferenceNode subNode : subNodes) {
				preferenceManager.addTo(section.getName(), subNode);
			}
		}

		return preferenceManager;
	}

}
