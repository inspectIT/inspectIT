package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.validation.validator.FqnWildcardValidator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part for defining the exclude rules.
 * 
 * @author Ivan Senic
 * 
 */
public class ExcludeRulesPart extends AbstractFormPart implements IPropertyListener {

	/**
	 * If edit can be executed.
	 */
	private boolean canEdit = true;

	/**
	 * List of exclude rules.
	 */
	private List<ExcludeRule> excludeRules = new ArrayList<>();

	/**
	 * {@link Profile} being edited.
	 */
	private Profile profile;

	/**
	 * {@link FormPage} part is being created on.
	 */
	protected FormPage formPage;

	/**
	 * Table viewer for displaying the exclude rules.
	 */
	private TableViewer tableViewer;

	/**
	 * Add rule button.
	 */
	private Button addButton;

	/**
	 * Remove selected rule(s) button.
	 */
	private Button removeButton;

	/**
	 * Default constructor.
	 * 
	 * @param formPage
	 *            {@link FormPage} creating the part.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	public ExcludeRulesPart(FormPage formPage, Composite parent, FormToolkit toolkit) {
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);
		ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();
		this.profile = input.getProfile();
		createPart(parent, toolkit);
	}

	/**
	 * Creates complete client.
	 * 
	 * @param parent
	 *            {@link Composite}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createPart(Composite parent, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(parent);
		mainComposite.setLayout(new GridLayout(2, false));

		Table table = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 400;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		if (CollectionUtils.isNotEmpty(profile.getExcludeRules())) {
			excludeRules.addAll(profile.getExcludeRules());
		}
		tableViewer.setInput(excludeRules);
		tableViewer.refresh();

		// buttons
		Composite buttonComposite = toolkit.createComposite(mainComposite);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		addButton = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExcludeRule excludeRule = addRequested();
				if (null != excludeRule) {
					excludeRules.add(excludeRule);
					tableViewer.refresh();
					showHideFormMessage();
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});

		removeButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!tableViewer.getSelection().isEmpty()) {
					StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
					for (Object selected : selection.toList()) {
						excludeRules.remove(selected);
					}
					tableViewer.refresh();
					showHideFormMessage();
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!tableViewer.getSelection().isEmpty() && canEdit);
			}
		});

		canEditCheck();

		// for message
		showHideFormMessage();
	}

	/**
	 * Executes the can edit check.
	 */
	private void canEditCheck() {
		canEdit = !profile.isCommonProfile(); // NOPMD
		if (!canEdit) {
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
	}

	/**
	 * Shows or hides the table info decoration.
	 */
	private void showHideFormMessage() {
		if (profile.isCommonProfile()) {
			formPage.getManagedForm().getMessageManager().addMessage(this, "Common profiles can not be edited", null, IMessageProvider.NONE);
		} else if (excludeRules.isEmpty()) {
			formPage.getManagedForm().getMessageManager().addMessage(this, "No exclude rule defined", null, IMessageProvider.INFORMATION);
		} else {
			formPage.getManagedForm().getMessageManager().removeMessage(this);
		}
	}

	/**
	 * To be called when add is requested to the table.
	 * 
	 * @return Returns new {@link ExcludeRule} or <code>null</code> if definition of one has been
	 *         canceled.
	 */
	private ExcludeRule addRequested() {
		InputDialog inputDialog = new InputDialog(getManagedForm().getForm().getShell(), "New Exclude Rule", "Enter class name or pattern:", "", new FqnWildcardValidator(false, false));
		if (inputDialog.open() == Dialog.OK) {
			ExcludeRule excludeRule = new ExcludeRule();
			excludeRule.setClassName(inputDialog.getValue());
			return excludeRule;
		}
		return null;
	}

	/**
	 * Creates columns for table.
	 */
	private void createColumns() {
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setResizable(false);
		column.getColumn().setWidth(400);
		column.getColumn().setText("Rule");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExcludeRule) element).getClassName();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			profile.setExcludeRules(new ArrayList<>(excludeRules));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();
			profile = input.getProfile();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}
