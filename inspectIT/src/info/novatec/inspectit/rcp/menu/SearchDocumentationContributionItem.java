package info.novatec.inspectit.rcp.menu;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.handlers.OpenUrlHandler.SearchDocumentationHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.services.IEvaluationService;

/**
 * The documentation search contribution item displayed in the main toolbar.
 * 
 * @author Ivan Senic
 * 
 */
public class SearchDocumentationContributionItem extends WorkbenchWindowControlContribution {

	/**
	 * Default text.
	 */
	private static final String DEFAULT_TEXT = "Wiki search";

	/**
	 * Text box for documentation search.
	 */
	private Text searchText;

	/**
	 * Default constructor.
	 */
	public SearchDocumentationContributionItem() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param id
	 *            Id of contribution item
	 */
	public SearchDocumentationContributionItem(String id) {
		super(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		main.setLayout(gl);

		searchText = new Text(main, SWT.SINGLE | SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH);
		setDefaultText();
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gridData.widthHint = 150;
		searchText.setLayoutData(gridData);
		searchText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (Objects.equals(searchText.getText(), DEFAULT_TEXT)) {
					setEmptyText();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (Objects.equals(searchText.getText(), "")) {
					setDefaultText();
				}
			}
		});
		searchText.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					setDefaultText();
					searchText.getParent().forceFocus();
				} else if (e.detail == SWT.TRAVERSE_RETURN) {
					executeSearch();
				}
			}
		});

		return main;
	}

	/**
	 * Sets default text in gray color.
	 */
	private void setDefaultText() {
		searchText.setText(DEFAULT_TEXT);
		searchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
	}

	/**
	 * Empties the text box and set font color to black.
	 */
	private void setEmptyText() {
		searchText.setText("");
		searchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
	}

	/**
	 * Executes the search.
	 */
	private void executeSearch() {
		String searchString = searchText.getText();
		if (StringUtils.isNotBlank(searchString)) {
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
			try {
				Command searchCommand = commandService.getCommand("info.novatec.inspectit.rcp.commands.searchDocumentation");
				Map<String, String> params = new HashMap<String, String>();
				params.put(SearchDocumentationHandler.SEARCH_DOCUMENTATION_PARAMETER, searchString);
				searchCommand.executeWithChecks(new ExecutionEvent(searchCommand, params, searchText, evaluationService.getCurrentState()));
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog("There was an exception executing the wiki search.", e, -1);
			}
		}
	}
}
