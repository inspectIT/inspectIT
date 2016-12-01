package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
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

import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.profile.data.ExcludeRulesProfileData;
import rocks.inspectit.ui.rcp.ci.form.input.ProfileEditorInput;
import rocks.inspectit.ui.rcp.validation.validator.FqnWildcardValidator;

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
	private final List<ExcludeRule> excludeRules = new ArrayList<>();

	/**
	 * {@link Profile} being edited.
	 */
	private Profile profile;

	/**
	 * Profile data.
	 */
	private ExcludeRulesProfileData profileData;

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
	 * Table viewer for displaying the exception to the exclude rules.
	 */
	private TableViewer exceptionTableViewer;

	/**
	 * Add exception button.
	 */
	private Button addExceptionButton;

	/**
	 * Remove exception button.
	 */
	private Button removeExceptionButton;

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

		checkAndGetEditorInput();

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
		if (CollectionUtils.isNotEmpty(profileData.getExcludeRules())) {
			excludeRules.addAll(profileData.getExcludeRules());
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
					tableViewer.setSelection(new StructuredSelection(excludeRule));
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
				boolean empty = tableViewer.getSelection().isEmpty();
				removeButton.setEnabled(!empty && canEdit);

				// exception part
				setExceptionPartEnabled(!empty);
				ExcludeRule selected = (ExcludeRule) ((StructuredSelection) tableViewer.getSelection()).getFirstElement();
				if (null != selected) {
					exceptionTableViewer.setInput(selected.getExceptions());
					exceptionTableViewer.refresh();
				}
			}
		});

		// exception to the rules
		Table exceptionTable = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 200;
		exceptionTable.setLayoutData(gd);
		exceptionTable.setHeaderVisible(true);
		exceptionTable.setLinesVisible(true);

		exceptionTableViewer = new TableViewer(exceptionTable);
		createExceptionColumns();
		exceptionTableViewer.setContentProvider(new ArrayContentProvider());

		buttonComposite = toolkit.createComposite(mainComposite);
		buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		addExceptionButton = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
		addExceptionButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addExceptionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog inputDialog = new InputDialog(getManagedForm().getForm().getShell(), "New Exception to the Exclude Rule", "Enter class name or pattern:", "",
						new FqnWildcardValidator(false, false));
				if (inputDialog.open() == Window.OK) {
					ExcludeRule selected = (ExcludeRule) ((StructuredSelection) tableViewer.getSelection()).getFirstElement();
					if (null != selected) {
						selected.getExceptions().add(inputDialog.getValue());
					}
					exceptionTableViewer.refresh();
					showHideFormMessage();
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});

		removeExceptionButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		removeExceptionButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeExceptionButton.setEnabled(false);
		removeExceptionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!exceptionTableViewer.getSelection().isEmpty()) {
					StructuredSelection selection = (StructuredSelection) exceptionTableViewer.getSelection();
					for (Object selected : selection.toList()) {
						ExcludeRule selectedExcludeRule = (ExcludeRule) ((StructuredSelection) tableViewer.getSelection()).getFirstElement();
						if (null != selectedExcludeRule) {
							selectedExcludeRule.getExceptions().remove(selected);
						}
					}
					exceptionTableViewer.refresh();
					showHideFormMessage();
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});

		exceptionTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				boolean empty = exceptionTableViewer.getSelection().isEmpty();
				removeExceptionButton.setEnabled(!empty && canEdit);
			}
		});

		setExceptionPartEnabled(false);
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
			setExceptionPartEnabled(false);
		}
	}

	/**
	 * Changes enabled state of the exception part.
	 *
	 * @param enabled
	 *            enabled
	 */
	private void setExceptionPartEnabled(boolean enabled) {
		exceptionTableViewer.getTable().setEnabled(enabled && canEdit);
		addExceptionButton.setEnabled(enabled && canEdit);
		removeExceptionButton.setEnabled(enabled && canEdit);
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
		if (inputDialog.open() == Window.OK) {
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
		column.getColumn().setToolTipText("The fully qualified name of the class or set of classes to be excluded. The wildcard * can be used to match any length of characters.");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExcludeRule) element).getClassName();
			}
		});
	}

	/**
	 * Creates columns for exception table.
	 */
	private void createExceptionColumns() {
		TableViewerColumn column = new TableViewerColumn(exceptionTableViewer, SWT.NONE);
		column.getColumn().setResizable(false);
		column.getColumn().setWidth(400);
		column.getColumn().setText("Exceptions to the Rule");
		column.getColumn()
		.setToolTipText("The fully qualified name of the class or set of classes to act as exception to the selected rule. The wildcard * can be used to match any length of characters.");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
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

			profileData.setExcludeRules(new ArrayList<>(excludeRules));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			checkAndGetEditorInput();
		}

	}

	/**
	 * Checks that the editor input has profile with the {@link ExcludeRulesProfileData}. If so,
	 * sets the {@link #profile} and {@link #profileData}.
	 */
	private void checkAndGetEditorInput() {
		ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();

		Assert.isNotNull(input.getProfile());
		Assert.isNotNull(input.getProfile().getProfileData());
		Assert.isLegal(input.getProfile().getProfileData().isOfType(ExcludeRulesProfileData.class), "Given profile can not be opened with the exclude rules part.");

		profile = input.getProfile();
		profileData = profile.getProfileData().getIfInstance(ExcludeRulesProfileData.class);
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
