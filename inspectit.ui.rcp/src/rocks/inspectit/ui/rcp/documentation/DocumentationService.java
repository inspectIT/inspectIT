package info.novatec.inspectit.rcp.documentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import info.novatec.inspectit.version.InvalidVersionException;
import info.novatec.inspectit.version.Version;
import info.novatec.inspectit.version.VersionService;

/**
 * Provides services to access the publicly available documentation page of inspectIT.
 *
 * @author Stefan Siegl
 */
@Service
public class DocumentationService {

	/**
	 * Link to the documentation page that refers to the concrete documentation of a version. This
	 * can be used if no version can be read.
	 */
	public static final String DOCUMENTATION_ENDUSER_HOME = "https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home";

	/**
	 * Link to the documentation page of a concrete version. Note that the major version needs to be
	 * added, like DOC15 for version 1.5.
	 */
	public static final String DOCUMENTATION_ENDUSER_SPECIFICVERSION = "https://inspectit-performance.atlassian.net/wiki/display/DOC";

	/**
	 * The search URL for the public inspectIT documentation.
	 */
	protected static final String DOCUMENTATION_SEARCH_URL = "https://inspectit-performance.atlassian.net/wiki/dosearchsite.action?queryString=";

	// https://inspectit-performance.atlassian.net/wiki/dosearchsite.action?queryString=s&startIndex=0&where=DOC14
	/**
	 * The version service. Injected via Spring.
	 */
	private VersionService versionService;

	/**
	 * Finds the appropriate documentation URL and takes into account the current version of
	 * inspectIT. If no version is found, the version-independent page will be shown.
	 *
	 * @return the version-dependent documentation URL of inspectIT.
	 */
	public String getDocumentationUrl() {
		try {
			Version version = versionService.getVersion();
			return DOCUMENTATION_ENDUSER_SPECIFICVERSION + getSpaceKey(version) + "/Home";
		} catch (InvalidVersionException e) {
			return DOCUMENTATION_ENDUSER_HOME;
		}
	}

	/**
	 * Builds the appropriate URL for searching within the enduser documentation of inspectIT. If
	 * the version can be retrieved, the search is filtered to only be applied on the space of this
	 * version. If the version cannot be retrieved, a general search is executed.
	 *
	 * If no search string is given, the normal documentation page is returned using the
	 * <code>getDocumentationUrl</code> method.
	 *
	 * @param searchString
	 *            the word(s) to search for.
	 * @return the appropriate URL for searching within the enduser documentation of inspectIT
	 */
	public String getSearchUrlFor(String searchString) {
		if (StringUtils.isNotEmpty(searchString)) {
			StringBuilder stringBuilder = new StringBuilder(DOCUMENTATION_SEARCH_URL);
			String[] words = StringUtils.split(searchString);
			for (int i = 0; i < words.length; i++) {
				stringBuilder.append(words[i]);
				if (i < words.length - 1) {
					stringBuilder.append('+');
				}
			}

			try {
				// if we know our version, we can restrict the search into
				// the correct documentation space
				Version version = versionService.getVersion();
				stringBuilder.append("&where=DOC");
				stringBuilder.append(getSpaceKey(version));
			} catch (InvalidVersionException e) { // NOPMD NOCHK
				// we cannot read the version, thus we just use the
				// unspecific search without specifying the concrete
				// documentation space.
			}

			return stringBuilder.toString();
		}
		return getDocumentationUrl();
	}

	/**
	 * Builds the space key from the version.
	 *
	 * @param version
	 *            the inspectIT version.
	 * @return the space key for this version.
	 */
	private String getSpaceKey(Version version) {
		return new StringBuilder().append(version.getMajor()).append(version.getMinor()).toString();
	}

	/**
	 * Necessary for injection.
	 *
	 * @param versionService
	 *            the version Service.
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

}
