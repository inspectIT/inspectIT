package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.documentation.DocumentationService;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Handler that opens that InspectIT Documentation page on Confluence.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class OpenUrlHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = browserSupport.createBrowser(null);
			URL url = new URL(getUrlString(event));
			browser.openURL(url);
		} catch (PartInitException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
		} catch (MalformedURLException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
		}
		return null;
	}

	/**
	 * Implementing classes should return the correct URL string to open.
	 * 
	 * @param event
	 *            {@link ExecutionEvent} that activated the handler.
	 * @return URL as a string.
	 */
	protected abstract String getUrlString(ExecutionEvent event);

	/**
	 * Handler for opening the Confluence documentation.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class OpenDocumentationHandler extends OpenUrlHandler {

		/**
		 * Documentation Service.
		 */
		protected DocumentationService documentationService = InspectIT.getService(DocumentationService.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			return documentationService.getDocumentationUrl();
		}
	}

	/**
	 * Handler for staring the feedback email.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class GiveFeedbackHandler extends OpenUrlHandler {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			return "mailto:info.inspectit@novatec-gmbh.de&subject=Feedback";
		}
	}

	/**
	 * Handler for staring the support email.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class RequestSupportHandler extends OpenUrlHandler {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			return "mailto:support.inspectit@novatec-gmbh.de&subject=Support%20needed";
		}
	}

	/**
	 * Handler for searching the documentation.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class SearchDocumentationHandler extends OpenDocumentationHandler {

		/**
		 * Parameter for the SearchDocumentationHandler.
		 */
		public static final String SEARCH_DOCUMENTATION_PARAMETER = "info.novatec.inspectit.rcp.commands.searchDocumentation.searchString";

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			String param = event.getParameter(SEARCH_DOCUMENTATION_PARAMETER);
			return documentationService.getSearchUrlFor(param);
		}
	}
}
