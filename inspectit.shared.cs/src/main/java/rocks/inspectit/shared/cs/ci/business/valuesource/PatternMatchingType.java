package rocks.inspectit.shared.cs.ci.business.valuesource;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Matching type definition, defining how a string value is compared against another value.
 *
 * @author Alexander Wert
 *
 */
@XmlEnum(String.class)
public enum PatternMatchingType {
	/**
	 * string starts with.
	 */
	@XmlEnumValue("starts-with") STARTS_WITH,

	/**
	 * string contains.
	 */
	@XmlEnumValue("contains") CONTAINS,

	/**
	 * string ends with.
	 */
	@XmlEnumValue("ends-with") ENDS_WITH,

	/**
	 * string equals.
	 */
	@XmlEnumValue("equals") EQUALS,

	/**
	 * string matches regular expression.
	 */
	@XmlEnumValue("regex") REGEX;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		switch (this) {
		case CONTAINS:
			return "contains";
		case ENDS_WITH:
			return "ends with";
		case EQUALS:
			return "equals";
		case STARTS_WITH:
			return "starts with";
		case REGEX:
			return "matches regex";
		default:
			return "";
		}
	};

}
