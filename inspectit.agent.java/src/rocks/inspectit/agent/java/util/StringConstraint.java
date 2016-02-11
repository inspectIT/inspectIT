package info.novatec.inspectit.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods for string manipulation purposes as cropping a string to a specified
 * length.
 * 
 * @author Patrick Eschenbach
 */
public class StringConstraint {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StringConstraint.class);

	/**
	 * Boolean indicating whether to use three trailing dots or not.
	 */
	private static final boolean USE_TRAILING_DOTS = true;

	/**
	 * Global definition of the maximal string length.
	 */
	private static final int MAX_STRING_LENGTH = Integer.MAX_VALUE;

	/**
	 * The effective string length which is defined as the smaller one of the sensor's configuration
	 * or MAX_STRING_LENGTH.
	 */
	private int effectiveStringLength;

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param parameter
	 *            Parameter map to extract the constrain information.
	 */
	public StringConstraint(Map<String, Object> parameter) {
		effectiveStringLength = MAX_STRING_LENGTH;

		String value = (String) parameter.get("stringLength");
		if (value != null) {
			try {
				int configStringLength = Integer.parseInt(value);

				// only use the given length if smaller than the max length and not smaller than 0
				if (configStringLength < MAX_STRING_LENGTH && configStringLength >= 0) {
					effectiveStringLength = configStringLength;
				}
			} catch (NumberFormatException e) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Property 'stringLength' is not defined correctly. Using unlimited string length.");
				}
			}
		}
	}

	/**
	 * Crops the given string based on this instance's configuration. If the string is shorter than
	 * the specified string length the given string is not altered.
	 * 
	 * @param string
	 *            The string to crop.
	 * @return The cropped string.
	 */
	public String crop(String string) {
		if (null == string || string.length() <= effectiveStringLength) {
			return string;
		}

		if (effectiveStringLength == 0) {
			return "";
		}

		String cropped = string.substring(0, effectiveStringLength);
		if (USE_TRAILING_DOTS) {
			cropped = appendTrailingDots(cropped);
		}
		return cropped;
	}

	/**
	 * Analyzes the given map and tries to re-use the values in String arrays and the string arrays
	 * themself to converse memory. A new String array for values is only created if one entry in
	 * the values needed cropping to ensure that the original map is not changed.
	 * 
	 * @param original
	 *            the Map<String, String[]> that potentially needs cropping
	 * @return a new Map<String, String[]> that contains cropped Strings that potentially re-use the
	 *         String references of the initial Map if the Strings did not change.
	 */
	public Map<String, String[]> crop(final Map<String, String[]> original) {
		Map<String, String[]> result = new HashMap<String, String[]>(original.size());

		for (Map.Entry<String, String[]> entry : original.entrySet()) {
			String key = entry.getKey();
			if (null == key) {
				continue;
			}

			String[] value = entry.getValue();

			String[] convertedValue = null;
			if (null == value) {
				convertedValue = new String[1];
				convertedValue[0] = "<notset>";
			} else {
				boolean croppingWasNeeded = false;
				convertedValue = value;

				for (int i = 0, l = value.length; i < l; i++) {
					String curValue = value[i];
					String croppingResult = crop(curValue);

					// Identity comparison is on purpose
					if (curValue != croppingResult & !croppingWasNeeded) { // NOPMD
						// The string reference was changed and thus cropped
						croppingWasNeeded = true;

						// we need to change to an own array as we cannot reuse the
						// existing array as we would change strings of the application
						convertedValue = new String[value.length];

						System.arraycopy(value, 0, convertedValue, 0, i);

						// and add the one we are currently dealing with
						convertedValue[i] = croppingResult;
					}

					if (croppingWasNeeded) {
						// we already have a copied array so we can add to this.
						convertedValue[i] = croppingResult;
					}

					// if we do not find anything that needed cropping we just re-use
					// the old representation.
				}
			}

			result.put(key, convertedValue);
		}

		return result;
	}

	/**
	 * Crops the given string and adds the given final character to the string's end. If the string
	 * is shorter than the specified string length the given string is not altered.
	 * 
	 * @param string
	 *            The string to crop.
	 * @param finalChar
	 *            The character to append at the end.
	 * @return A cropped string ending with the given final character.
	 */
	public String cropKeepFinalCharacter(String string, char finalChar) {
		String cropped = crop(string);

		if (null == string || string.equals(cropped)) {
			return string;
		}

		if (cropped.length() == 0) {
			return cropped;
		}
		return cropped + finalChar;
	}

	/**
	 * Appends three dots to the given string to indicate that the string was cropped.
	 * 
	 * @param string
	 *            The string to append the dots to.
	 * @return A new string equal to the given one + three dots.
	 */
	private String appendTrailingDots(String string) {
		return string + "...";
	}
}
