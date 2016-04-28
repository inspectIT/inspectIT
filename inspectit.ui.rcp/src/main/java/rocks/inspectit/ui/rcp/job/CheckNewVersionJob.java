package rocks.inspectit.ui.rcp.job;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import rocks.inspectit.shared.all.version.InvalidVersionException;
import rocks.inspectit.shared.all.version.Version;
import rocks.inspectit.shared.all.version.VersionRelease;
import rocks.inspectit.shared.all.version.VersionService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;

/**
 * Job for checking if a new version of inspectIT exists on the GitHub.
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

		VersionService versionService = InspectIT.getService(VersionService.class);
		Version currentVersion;
		try {
			currentVersion = versionService.getVersion();
		} catch (InvalidVersionException e) {
			return new Status(IStatus.ERROR, InspectIT.ID, "Can not read the inspectIT version.", e);
		}
		VersionRelease highestVersionRelease = null;

		// check on the github if there is a higher version available
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(GITHUB_RELEASES_API);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();

			try (InputStream inputStream = entity.getContent()) {
				Gson gson = new GsonBuilder().create();
				JsonElement readed = gson.fromJson(new InputStreamReader(inputStream), JsonElement.class);
				if (!(readed instanceof JsonArray)) {
					// sometimes we are not getting JsonArray from the GitHub
					return new Status(userTriggered ? IStatus.ERROR : IStatus.WARNING, InspectIT.ID,
							"Check new version failed due to the invalid API response. Try again later. Received object: " + readed);
				}
				highestVersionRelease = getHighestVersionFromJson((JsonArray) readed);
			}
		} catch (IOException | InvalidVersionException exception) {
			// give feedback if user triggered it
			return new Status(userTriggered ? IStatus.ERROR : IStatus.WARNING, InspectIT.ID, "Error occurred reading the existing versions from GitHub during check for new version job.", exception);
		}

		if (highestVersionRelease.getVersion().compareTo(currentVersion) > 0) {
			// we have a new version available
			// display dialog in UI thread
			final VersionRelease highestVersionFinal = highestVersionRelease;
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
			final Version currentVersionFinal = currentVersion;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Check for new Version", "There is no newer version of inspectIT available. Currently running version "
							+ currentVersionFinal.toString() + " is the latest officially released version.");
				}
			});

		}

		return Status.OK_STATUS;
	}

	/**
	 * Returns the highest available version release from the github JsonArray.
	 * 
	 * @param jsonArray
	 *            JsonArray of releases.
	 * @return Returns the highest available version release or 0.0.0 (empty release) if no release
	 *         reported in the json.
	 * @throws InvalidVersionException
	 *             If Json contains version tags that will cause the {@link InvalidVersionException}
	 *             .
	 */
	private VersionRelease getHighestVersionFromJson(JsonArray jsonArray) throws InvalidVersionException {
		Version highest = new Version(0, 0, 0);
		VersionRelease versionRelease = new VersionRelease(highest);

		for (int i = 0, size = jsonArray.size(); i < size; i++) {
			JsonObject element = (JsonObject) jsonArray.get(i);
			String versionTag = element.get("tag_name").getAsString();
			Version version = Version.verifyAndCreate(versionTag);

			if (version.compareTo(highest) > 0) {
				highest = version;
				versionRelease = new VersionRelease(version);

				// set pre-release and download link
				boolean preRelease = element.get("prerelease").getAsBoolean();
				versionRelease.setPreRelease(preRelease);
				String htmlLink = element.get("html_url").getAsString();
				versionRelease.setLink(htmlLink);
			}
		}

		return versionRelease;
	}

	/**
	 * A bit changed {@link MessageDialogWithToggle} for displaying our message with link.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class NewVersionDialog extends MessageDialogWithToggle {

		/**
		 * Version to display in dialog.
		 */
		private VersionRelease versionRelease;

		/**
		 * @param versionRelease
		 *            Version release to display in dialog
		 * @param parentShell
		 *            the parent shell
		 * @param toggleState
		 *            the initial state for the toggle
		 * 
		 */
		public NewVersionDialog(VersionRelease versionRelease, Shell parentShell, boolean toggleState) {
			super(parentShell, "Check for New Version", null, "", MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0, "Enable auto check for the new version on startup",
					toggleState);
			this.setShellStyle(this.getShellStyle() | SWT.SHEET);
			this.versionRelease = versionRelease;
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
			if (versionRelease != null) {
				String type = versionRelease.isPreRelease() ? "preview" : "stable";
				String message = "There is a newer version of inspectIT available. The version " + versionRelease.getVersion().toString() + " is the latest officially released " + type
						+ " version and can be downloaded from the GitHub: " + versionRelease.getLink();

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
