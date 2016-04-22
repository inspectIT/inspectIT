package rocks.inspectit.shared.cs.cmr.property.configuration.impl;

import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.impl.BytePropertyUpdate;

/**
 * Property holding byte count values. This property parses {@link String} values that represent the
 * byte count and vice versa.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "byte-property")
public class ByteProperty extends SingleProperty<Long> {

	/**
	 * Used value in {@link String}.
	 */
	@XmlAttribute(name = "used-value")
	private String usedValue;

	/**
	 * Default value in {@link String}.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private String defaultValue;

	/**
	 * No-arg constructor.
	 */
	public ByteProperty() {
	}

	/**
	 *
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @param logicalName
	 *            The logical name of the property that is used in the configuration.
	 * @param defaultValue
	 *            Default value.
	 * @param advanced
	 *            If the property is advanced, thus should be available only to expert users.
	 * @param serverRestartRequired
	 *            If the change of this property should trigger server restart.
	 * @throws IllegalArgumentException
	 *             If name, section, logical name or default value are <code>null</code>.
	 * @see SingleProperty#SingleProperty(String, String, String, Object, boolean, boolean)
	 */
	public ByteProperty(String name, String description, String logicalName, Long defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getDefaultValue() {
		return fromString(defaultValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setDefaultValue(Long defaultValue) {
		this.defaultValue = toString(defaultValue.longValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long getUsedValue() {
		if (null != usedValue) {
			return fromString(usedValue);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUsedValue(Long usedValue) {
		if (null != usedValue) {
			this.usedValue = toString(usedValue.longValue());
		} else {
			this.usedValue = null; // NOPMD
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPropertyUpdate<Long> createPropertyUpdate(Long updateValue) {
		return new BytePropertyUpdate(this, updateValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long parseLiteral(String literal) {
		return fromString(literal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedValue() {
		if (null != usedValue) {
			return usedValue;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Returns the bytes number from human readable string.
	 *
	 * @param str
	 *            Human readable string.
	 * @return Bytes number.
	 */
	public static Long fromString(String str) {
		if (null == str) {
			return null;
		}

		int exp = 0;
		int length = str.length();
		Character c = str.charAt(length - 2);
		switch (c) {
		case 'E':
			exp++;
		case 'P':
			exp++;
		case 'T':
			exp++;
		case 'G':
			exp++;
		case 'M':
			exp++;
		case 'K':
			exp++;
		default:
			break;
		}

		String number = (exp == 0) ? str.substring(0, length - 1) : str.substring(0, length - 2);
		try {
			return Long.valueOf((long) (Double.parseDouble(number) * Math.pow(1024, exp)));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Returns the human readable bytes number.
	 *
	 * @param bytes
	 *            Bytes to transform.
	 * @return Human readable string.
	 */
	public static String toString(long bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return bytes + "B";
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = String.valueOf("KMGTPE".charAt(exp - 1));
			// must be in English locale, otherwise Double.parseDouble may create problems
			String result = String.format(Locale.ENGLISH, "%f", bytes / Math.pow(unit, exp));

			// make sure we don't save unnecessary zeros
			int end = result.length();
			for (; end > 0; end--) {
				char c = result.charAt(end - 1);
				if (c == '.') {
					end--;
					break;
				} else if (c != '0') {
					break;
				}
			}

			if (end == result.length()) {
				return result + pre + "B";
			} else {
				return result.substring(0, end) + pre + "B";
			}

		}
	}

}
