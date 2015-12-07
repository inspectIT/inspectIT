package info.novatec.inspectit.ci.business;

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
	@XmlEnumValue("starts-with")
	STARTS_WITH,

	/**
	 * string contains.
	 */
	@XmlEnumValue("contains")
	CONTAINS,

	/**
	 * string ends with.
	 */
	@XmlEnumValue("ends-with")
	ENDS_WITH,

	/**
	 * string equals.
	 */
	@XmlEnumValue("equals")
	EQUALS;

	/**
	 * {@inheritDoc}
	 */
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

		default:
			return "";
		}
	};

}
