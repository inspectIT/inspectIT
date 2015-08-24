package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.versioning.FileBasedVersioningServiceImpl;
import info.novatec.inspectit.versioning.IVersioningService;
import info.novatec.inspectit.versioning.UnknownVersionException;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
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
		 * Versioning service.
		 */
		protected IVersioningService versionService = new FileBasedVersioningServiceImpl();

		/**
		 * Link to the documentation page that refers to the concrete
		 * documentation of a version. This can be used if no version can be
		 * read.
		 */
		public static final String DOCUMENTATION_ENDUSER_HOME = "https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home";

		/**
		 * Link to the documentation page of a concrete version. Note that the
		 * major version needs to be added, like DOC15 for version 1.5.
		 */
		public static final String DOCUMENTATION_ENDUSER_SPECIFICVERSION = "https://inspectit-performance.atlassian.net/wiki/display/DOC";

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			try {
				return DOCUMENTATION_ENDUSER_SPECIFICVERSION + versionService.getMajorVersionNoDots() + "/Home";
			} catch (UnknownVersionException e) {
				return DOCUMENTATION_ENDUSER_HOME;
			}
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
		 * The search URL for the public inspectIT documentation.
		 */
		protected static final String DOCUMENTATION_SEARCH_URL = "https://inspectit-performance.atlassian.net/wiki/dosearchsite.action?searchQuery.queryString=";

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getUrlString(ExecutionEvent event) {
			String param = event.getParameter(SEARCH_DOCUMENTATION_PARAMETER);

			if (StringUtils.isNotEmpty(param)) {
				StringBuilder stringBuilder = new StringBuilder(DOCUMENTATION_SEARCH_URL);
				String[] words = StringUtils.split(param);
				for (int i = 0; i < words.length; i++) {
					stringBuilder.append(words[i]);
					if (i < words.length - 1) {
						stringBuilder.append('+');
					}
				}

				try {
					// if we know our version, we can restrict the search into
					// the correct documentation space
					String version = versionService.getMajorVersionNoDots();
					stringBuilder.append("&searchQuery.spaceKey=DOC");
					stringBuilder.append(version);
				} catch (UnknownVersionException e) { // NOPMD NOCHK
					// we cannot read the version, thus we just use the
					// unspecific search without specifying the concrete
					// documentation space.
				}

				return stringBuilder.toString();
			}
			return super.getUrlString(event);
		}
	}
}
