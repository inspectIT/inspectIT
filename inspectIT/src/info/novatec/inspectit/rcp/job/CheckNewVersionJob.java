package info.novatec.inspectit.rcp.job;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.osgi.framework.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Job for checking if a new version of inspectIT exists on the FTP.
 * 
 * @author Ivan Senic
 * 
 */
public class CheckNewVersionJob extends Job {

	/**
	 * URI of the GitHub API GET method for the releases.
	 */
	private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/inspectIT/inspectIT/releases";

	/**
	 * Prefix for the link to the release on the GitHub. Should be extended by the tag name.
	 */
	private static final String GITHUB_RELEASES_WEB_PREFIX = "https://github.com/inspectIT/inspectIT/releases/tag/";

	/**
	 * If job has been triggered by the user. If so the job will be performed no matter what.
	 * Otherwise we would check first if preference for auto-check is set to <code>true</code> and
	 * only then execute job.
	 */
	private boolean userTriggered;

	/**
	 * Default constructor.
	 * 
	 * @param userTriggered
	 *            If job has been triggered by the user. If so the job will be performed no matter
	 *            what. Otherwise we would check first if preference for auto-check is set to
	 *            <code>true</code> and only then execute job.
	 */
	public CheckNewVersionJob(boolean userTriggered) {
		super("Checking for new inspectIT version");
		this.userTriggered = userTriggered;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final boolean isAutoCheck = PreferencesUtils.getBooleanValue(PreferencesConstants.AUTO_CHECK_NEW_VERSION);

		if (!userTriggered && !isAutoCheck) {
			// check for the preference and don't run if it's disabled
			return Status.OK_STATUS;
		}

		final Version currentVersion = InspectIT.getDefault().getBundle().getVersion();
		Version highestVersion = currentVersion;

		// check on the github if there is a higher version available
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(GITHUB_RELEASES_API);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();

			try (InputStream inputStream = entity.getContent()) {
				Gson gson = new GsonBuilder().create();
				JsonArray readed = gson.fromJson(new InputStreamReader(inputStream), JsonArray.class);
				highestVersion = getHigestVersionFromJson(readed);
			}
		} catch (IOException exception) {
			// give feedback if user triggered it
			if (userTriggered) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createErrorDialog(
								"Could not execute the check for the new version, error occurred reading the existing versions from GitHub. Please check your Internet connection.", -1);
					}
				});
			}

			return new Status(IStatus.WARNING, InspectIT.ID, "Error occurred reading the existing versions from GitHub.", exception);
		}

		if (highestVersion.compareTo(currentVersion) > 0) {
			// we have a new version available
			// display dialog in UI thread
			final Version highestVersionFinal = highestVersion;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					NewVersionDialog dialog = new NewVersionDialog(highestVersionFinal, Display.getCurrent().getActiveShell(), isAutoCheck);
					dialog.open();
					Boolean toggleState = dialog.getToggleState();
					PreferencesUtils.saveBooleanValue(PreferencesConstants.AUTO_CHECK_NEW_VERSION, toggleState.booleanValue(), false);
				}
			});
		} else if (userTriggered) {
			// if the user triggered the check we give him feedback that no newer version of
			// inspectIT is available
			// note that we are not in UI thread
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Check for new Version", "There is no newer version of inspectIT available. Currently running version "
							+ currentVersion.toString() + " is the latest officially released version.");
				}
			});

		}

		return Status.OK_STATUS;
	}

	/**
	 * Returns the highest available version from the github JsonArray.
	 * 
	 * @param readed
	 *            JsonArray of releases.
	 * @return Returns the highest available version or 0.0.0 if no release reported in the json.
	 */
	private Version getHigestVersionFromJson(JsonArray readed) {
		Version higest = new Version(0, 0, 0);

		for (int i = 0, size = readed.size(); i < size; i++) {
			JsonObject element = (JsonObject) readed.get(i);
			String versionTag = element.get("tag_name").getAsString();
			Version version = new Version(versionTag);

			if (version.compareTo(higest) > 0) {
				higest = version;
			}
		}

		// TODO after INSPECTIT-1936 is integrated we can transfer
		// info as pre-release/link/etc to our Version object

		return higest;
	}

	/**
	 * Returns download link for the given version.
	 * 
	 * @param version
	 *            version to get the link for.
	 * @return download link.
	 */
	private String getDownloadLink(Version version) {
		return GITHUB_RELEASES_WEB_PREFIX + version.toString();
	}

	/**
	 * A bit changed {@link MessageDialogWithToggle} for displaying our message with link.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class NewVersionDialog extends MessageDialogWithToggle {

		/**
		 * Version to display in dialog.
		 */
		private Version version;

		/**
		 * @param version
		 *            version to display in dialog
		 * @param parentShell
		 *            the parent shell
		 * @param toggleState
		 *            the initial state for the toggle
		 * 
		 */
		public NewVersionDialog(Version version, Shell parentShell, boolean toggleState) {
			super(parentShell, "Check for New Version", null, "", MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0, "Enable auto check for the new version on startup",
					toggleState);
			this.setShellStyle(this.getShellStyle() | SWT.SHEET);
			this.version = version;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createMessageArea(Composite composite) {
			// image copied from super
			Image image = getImage();
			if (image != null) {
				imageLabel = new Label(composite, SWT.NULL);
				image.setBackground(imageLabel.getBackground());
				imageLabel.setImage(image);
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
			}

			// message implementation we provide on our own
			if (version != null) {
				String message = "There is a newer version of inspectIT available. The version " + version.toString()
						+ " is the latest officially released version and can be downloaded from the GitHub: " + getDownloadLink(version);

				FormText messageFormText = new FormText(composite, SWT.NO_FOCUS);
				messageFormText.setText(message, false, true);
				messageFormText.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
						try {
							IWebBrowser browser = browserSupport.createBrowser(null);
							URL url = new URL((String) e.getHref());
							browser.openURL(url);
						} catch (Exception exception) {
							InspectIT.getDefault().createErrorDialog(exception.getMessage(), exception, -1);
						}
					}
				});
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
						.applyTo(messageFormText);
			}

			return composite;
		}

	}

}
