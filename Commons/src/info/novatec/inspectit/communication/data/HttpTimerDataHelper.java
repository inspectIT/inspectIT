package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.util.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helper class for {@link HttpTimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public final class HttpTimerDataHelper {

	/**
	 * Private constructor.
	 */
	private HttpTimerDataHelper() {
	}

	/**
	 * Transforms the URI from the {@link HttpTimerData} with the given regular expression. If
	 * template is provided all found groups will be replaced in template, if template specifies
	 * them.
	 * 
	 * @param httpTimerData
	 *            {@link HttpTimerData}
	 * @param regEx
	 *            Regular expression
	 * @param regExTemplate
	 *            Template
	 * @return Transformed URI.
	 * @throws IllegalArgumentException
	 *             If {@link HttpTimerData} does not define the URI, if regular expression is
	 *             <code>null</code> or can not be compiled.
	 */
	public static String getTransformedUri(HttpTimerData httpTimerData, String regEx, String regExTemplate) throws IllegalArgumentException {
		if (!httpTimerData.isUriDefined()) {
			throw new IllegalArgumentException("URI is not defined for the given HttpTimerData.");
		}
		if (null == regEx) {
			throw new IllegalArgumentException("Regular expression to use can not be null.");
		}
		try {
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(httpTimerData.getUri());
			String result = "";
			if (null != regExTemplate) {
				result = regExTemplate;
			}
			if (matcher.find()) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					if (null != regExTemplate) {
						String matched = matcher.group(i);
						if (null != matched) {
							result = result.replace("$" + i + "$", matcher.group(i));
						}
					} else {
						result += matcher.group(i);
					}
				}
				if (!ObjectUtils.equals(result, regExTemplate)) {
					return result;
				}
			}
			return "Regular Expression " + regEx + " does not match URI";
		} catch (PatternSyntaxException patternSyntaxException) {
			throw new IllegalArgumentException("Provided Regular expression is not correct.", patternSyntaxException);
		}
	}
}
