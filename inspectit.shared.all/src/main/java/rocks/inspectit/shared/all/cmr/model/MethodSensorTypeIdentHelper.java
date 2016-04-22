package rocks.inspectit.shared.all.cmr.model;

import org.apache.commons.collections.MapUtils;

/**
 * Helper methods for the {@link MethodSensorTypeIdent}.
 *
 * @author Ivan Senic
 *
 */
public final class MethodSensorTypeIdentHelper {

	/**
	 * Key in sensor settings that denote regular expression.
	 */
	private static final Object REGEX_KEY = "regEx";

	/**
	 * Key in sensor settings that denote regular expression template.
	 */
	private static final Object REGEX_TEMPLATE_KEY = "regExTemplate";

	/**
	 * Private constructor.
	 */
	private MethodSensorTypeIdentHelper() {
	}

	/**
	 * Returns the regular expression definition from the {@link MethodSensorTypeIdent} if one is
	 * set.
	 *
	 * @param methodSensorTypeIdent
	 *            {@link MethodSensorTypeIdent}.
	 * @return Regular expression or <code>null</code> if it's not set.
	 */
	public static String getRegEx(MethodSensorTypeIdent methodSensorTypeIdent) {
		if (MapUtils.isNotEmpty(methodSensorTypeIdent.getSettings())) {
			Object regEx = methodSensorTypeIdent.getSettings().get(REGEX_KEY);
			if (null != regEx) {
				return regEx.toString();
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * Returns the regular expression definition from the {@link MethodSensorTypeIdent} if one is
	 * set.
	 *
	 * @param methodSensorTypeIdent
	 *            {@link MethodSensorTypeIdent}.
	 * @return Regular expression or <code>null</code> if it's not set.
	 */
	public static String getRegExTemplate(MethodSensorTypeIdent methodSensorTypeIdent) {
		if (MapUtils.isNotEmpty(methodSensorTypeIdent.getSettings())) {
			Object template = methodSensorTypeIdent.getSettings().get(REGEX_TEMPLATE_KEY);
			if (null != template) {
				return template.toString();
			} else {
				return null;
			}
		}
		return null;
	}
}
