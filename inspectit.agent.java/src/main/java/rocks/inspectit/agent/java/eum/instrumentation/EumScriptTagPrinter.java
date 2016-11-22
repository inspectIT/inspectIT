package rocks.inspectit.agent.java.eum.instrumentation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utiltiy class for printing out the script tags to inject for the JS agent. This includes both the
 * inline script with the settings as well as the tag for the actual agent script.
 *
 * @author Jonas Kunz
 *
 */
public class EumScriptTagPrinter implements Cloneable {

	/**
	 * The path to JS Agent source, including the file name.
	 */
	private String scriptSourceURL;

	/**
	 * A map of settings to pass to the agent.
	 */
	private Map<String, String> settings;

	/**
	 * Default Constructor.
	 */
	public EumScriptTagPrinter() {
		settings = new HashMap<String, String>();
	}

	/**
	 * Gets {@link #scriptSourceURL}.
	 *
	 * @return {@link #scriptSourceURL}
	 */
	public String getScriptSourceURL() {
		return this.scriptSourceURL;
	}

	/**
	 * Sets {@link #scriptSourceURL}.
	 *
	 * @param scriptSourceURL
	 *            New value for {@link #scriptSourceURL}
	 */
	public void setScriptSourceURL(String scriptSourceURL) {
		this.scriptSourceURL = scriptSourceURL;
	}

	/**
	 * Gets {@link #settings}.
	 *
	 * @return {@link #settings}
	 */
	public Map<String, String> getSettings() {
		return Collections.unmodifiableMap(this.settings);
	}

	/**
	 * Sets an entry of {@link #settings}.
	 *
	 * @param optionName
	 *            the name of the option to set
	 * @param value
	 *            the value to set
	 */
	public void setSetting(String optionName, String value) {
		settings.put(optionName, value);
	}

	/**
	 * Prints out the actual tags to inject.
	 *
	 * @return the tags string
	 */
	public String printTags() {
		// TODO: maybe add a comment tag here stating that this stuff comes from inspectIT?
		StringBuilder tags = new StringBuilder();
		tags.append("<script type=\"text/javascript\">" + "window.inspectIT_settings = {");
		boolean isFirstSettings = true;
		for (Entry<String, String> setting : settings.entrySet()) {
			if (!isFirstSettings) {
				tags.append(',');
			} else {
				isFirstSettings = false;
			}
			tags.append(setting.getKey()).append(':').append(setting.getValue());
		}
		tags.append("}; </script> <script type=\"text/javascript\" src=\"");
		tags.append(scriptSourceURL);
		tags.append("\"></script>");
		return tags.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EumScriptTagPrinter clone() { // NOPMD
		EumScriptTagPrinter result = new EumScriptTagPrinter();
		result.scriptSourceURL = this.getScriptSourceURL();
		result.settings = new HashMap<String, String>(this.getSettings());
		return result;
	}

}
