package rocks.inspectit.shared.cs.ci;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.util.EMailUtils;

/**
 * XML element which represents a threshold used for alerting purpose.
 *
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "alerting-definition")
public class AlertingDefinition extends AbstractCiData {

	/**
	 * Threshold types.
	 *
	 * @author Marius Oehler
	 *
	 */
	public enum ThresholdType {
		/**
		 * The specified threshold is an upper threshold. Alert is issued if the threshold is
		 * exceeded.
		 */
		UPPER_THRESHOLD("Upper Threshold"),

		/**
		 * The specified threshold is an lower threshold. Alert is issued if the threshold is
		 * undercut.
		 */
		LOWER_THRESHOLD("Lower Threshold");

		/**
		 * The name of the threshold type.
		 */
		private String name;

		/**
		 * Constructor.
		 *
		 * @param name
		 *            the name of the threshold type
		 */
		ThresholdType(String name) {
			this.name = name;
		}

		/**
		 * Gets {@link #name}.
		 *
		 * @return {@link #name}
		 */
		public String getName() {
			return this.name;
		}
	}

	/**
	 * The threshold.
	 */
	@XmlAttribute(name = "threshold")
	private Double threshold;

	/**
	 * The field to check against the threshold.
	 */
	@XmlAttribute(name = "field")
	private String field;

	/**
	 * The type of the specified threshold.
	 */
	@XmlAttribute(name = "threshold-type")
	private ThresholdType thresholdType = ThresholdType.UPPER_THRESHOLD;

	/**
	 * The measurement to monitor.
	 */
	@XmlAttribute(name = "measurement")
	private String measurement;

	/**
	 * The tags used to select the monitored data.
	 */
	@XmlElementWrapper(name = "tags")
	private Map<String, String> tags = new HashMap<>(0);

	/**
	 * The duration between consecutive checks in minutes.
	 */
	@XmlAttribute(name = "timerange")
	private Long timeRange;

	/**
	 * List of e-mails which receives a notification when the threshold is violated.
	 */
	@XmlElementWrapper(name = "notification-email-addresses")
	@XmlElement(name = "notification-email-address")
	private List<String> notificationEmailAddresses = new ArrayList<>(0);

	/**
	 * Default constructor.
	 */
	public AlertingDefinition() {
	}

	/**
	 * Clone constructor.
	 *
	 * @param template
	 *            template for the new instance
	 */
	public AlertingDefinition(AlertingDefinition template) {
		super(template);

		if (template != null) {
			this.threshold = template.threshold;
			this.field = template.field;
			this.thresholdType = template.thresholdType;
			this.measurement = template.measurement;
			this.timeRange = template.timeRange;
			if (notificationEmailAddresses != null) {
				this.notificationEmailAddresses = new ArrayList<>(template.notificationEmailAddresses);
			}
			if (tags != null) {
				this.tags = new HashMap<>(template.tags);
			}
		}
	}

	/**
	 * Adds a email address to the alerting definition.
	 *
	 * @param email
	 *            the email address to add
	 * @return result of the adding (as specified by {@link Collection#add(Object)})
	 */
	public boolean addNotificationEmailAddress(String email) {
		if (email == null) {
			throw new IllegalArgumentException("Adding email adress 'null'.");
		} else if (email.isEmpty()) {
			throw new IllegalArgumentException("Adding empty email address.");
		} else if (!EMailUtils.isValidEmailAddress(email)) {
			throw new IllegalArgumentException("Adding invalid email address '" + email + "'.");
		} else if (notificationEmailAddresses.contains(email)) {
			throw new IllegalArgumentException("Adding email address '" + email + "'.");
		} else {
			return notificationEmailAddresses.add(email);
		}
	}

	/**
	 * Gets {@link #field}.
	 *
	 * @return {@link #field}
	 */
	public String getField() {
		return field;
	}

	/**
	 * Gets {@link #measurement}.
	 *
	 * @return {@link #measurement}
	 */
	public String getMeasurement() {
		return measurement;
	}

	/**
	 * Gets {@link #notificationEmailAddresses}.
	 *
	 * @return {@link #notificationEmailAddresses}
	 */
	public List<String> getNotificationEmailAddresses() {
		return Collections.unmodifiableList(notificationEmailAddresses);
	}

	/**
	 * Gets {@link #tags}.
	 *
	 * @return {@link #tags}
	 */
	public Map<String, String> getTags() {
		return Collections.unmodifiableMap(tags);
	}

	/**
	 * Gets {@link #threshold}.
	 *
	 * @return {@link #threshold}
	 */
	public double getThreshold() {
		return threshold.doubleValue();
	}

	/**
	 * Gets {@link #thresholdType}.
	 *
	 * @return {@link #thresholdType}
	 */
	public ThresholdType getThresholdType() {
		return thresholdType;
	}

	/**
	 * Gets {@link #timeRange}.
	 *
	 * @param unit
	 *            the time unit of the returned value
	 * @return {@link #timeRange}
	 */
	public long getTimeRange(TimeUnit unit) {
		return unit.convert(timeRange, TimeUnit.MINUTES);
	}

	/**
	 * Puts a tag represented by the given key-value to the alerting definition. A previously added
	 * tag containing the same key will be overridden.
	 *
	 * @param tagKey
	 *            the tag key
	 * @param tagValue
	 *            the tag value
	 * @return returns the previous value related to the given key (as specified by
	 *         {@link Map#put(Object, Object)}
	 */
	public String putTag(String tagKey, String tagValue) {
		if (tagKey == null) {
			throw new IllegalArgumentException("Putting tag with key 'null'.");
		} else if (tagValue == null) {
			throw new IllegalArgumentException("Putting tag with value 'null'.");
		} else if (tagKey.isEmpty()) {
			throw new IllegalArgumentException("Putting tag with empty key.");
		} else if (tagValue.isEmpty()) {
			throw new IllegalArgumentException("Putting tag with empty value.");
		} else {
			return tags.put(tagKey, tagValue);
		}
	}

	/**
	 * Removes the given email address from the alerting definition.
	 *
	 * @param email
	 *            the email address to remove
	 * @return result of the removing (as specified by {@link Collection#remove(Object)}
	 */
	public boolean removeNotificationEmailAddress(String email) {
		if (email == null) {
			throw new IllegalArgumentException("Adding email adress 'null'.");
		} else if (email.isEmpty()) {
			throw new IllegalArgumentException("Adding empty email address.");
		} else {
			return notificationEmailAddresses.remove(email);
		}
	}

	/**
	 * Removes the tag with the given key from the alerting definition.
	 *
	 * @param tagKey
	 *            the key of the tag to remove
	 */
	public void removeTag(String tagKey) {
		if (tagKey == null) {
			throw new IllegalArgumentException("Removing tag with key 'null'.");
		} else if (tagKey.isEmpty()) {
			throw new IllegalArgumentException("Removing tag with empty key.");
		} else if (!tags.containsKey(tagKey)) {
			throw new IllegalArgumentException("Removing tag with key '" + tagKey + "'.");
		} else {
			tags.remove(tagKey);
		}
	}

	/**
	 * Replaces the current notification email addresses with the ones of the given list.
	 *
	 * @param newNotificationEmailAddresses
	 *            new email addresses
	 */
	public void replaceNotificationEmailAddresses(List<String> newNotificationEmailAddresses) {
		if (newNotificationEmailAddresses == null) {
			throw new IllegalArgumentException("Replacing notification email list with 'null'.");
		}

		notificationEmailAddresses.clear();

		for (String emailAddress : newNotificationEmailAddresses) {
			addNotificationEmailAddress(emailAddress);
		}
	}

	/**
	 * Replacing the current tags with the tags contained on the given map.
	 *
	 * @param newTags
	 *            map contains the new tags
	 */
	public void replaceTags(Map<String, String> newTags) {
		if (newTags == null) {
			throw new IllegalArgumentException("Replacing the current tags with a null map.");
		}

		tags.clear();

		for (Entry<String, String> tag : newTags.entrySet()) {
			putTag(tag.getKey(), tag.getValue());
		}
	}

	/**
	 * Sets {@link #field}.
	 *
	 * @param field
	 *            New value for {@link #field}
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * Sets {@link #measurement}.
	 *
	 * @param measurement
	 *            New value for {@link #measurement}
	 */
	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}

	/**
	 * Sets {@link #threshold}.
	 *
	 * @param threshold
	 *            New value for {@link #threshold}
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Sets {@link #thresholdType}.
	 *
	 * @param thresholdType
	 *            New value for {@link #thresholdType}
	 */
	public void setThresholdType(ThresholdType thresholdType) {
		this.thresholdType = thresholdType;
	}

	/**
	 * Sets {@link #timeRange}.
	 *
	 * @param timeRange
	 *            New value for {@link #timeRange}
	 * @param unit
	 *            the time unit of the given time range
	 */
	public void setTimeRange(Long timeRange, TimeUnit unit) {
		this.timeRange = TimeUnit.MINUTES.convert(timeRange, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((field == null) ? 0 : field.hashCode());
		result = (prime * result) + ((measurement == null) ? 0 : measurement.hashCode());
		result = (prime * result) + ((notificationEmailAddresses == null) ? 0 : notificationEmailAddresses.hashCode());
		result = (prime * result) + ((tags == null) ? 0 : tags.hashCode());
		result = (prime * result) + ((threshold == null) ? 0 : threshold.hashCode());
		result = (prime * result) + ((thresholdType == null) ? 0 : thresholdType.hashCode());
		result = (prime * result) + ((timeRange == null) ? 0 : timeRange.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AlertingDefinition other = (AlertingDefinition) obj;
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		if (measurement == null) {
			if (other.measurement != null) {
				return false;
			}
		} else if (!measurement.equals(other.measurement)) {
			return false;
		}
		if (notificationEmailAddresses == null) {
			if (other.notificationEmailAddresses != null) {
				return false;
			}
		} else if (!notificationEmailAddresses.equals(other.notificationEmailAddresses)) {
			return false;
		}
		if (tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!tags.equals(other.tags)) {
			return false;
		}
		if (threshold == null) {
			if (other.threshold != null) {
				return false;
			}
		} else if (!threshold.equals(other.threshold)) {
			return false;
		}
		if (thresholdType != other.thresholdType) {
			return false;
		}
		if (timeRange == null) {
			if (other.timeRange != null) {
				return false;
			}
		} else if (!timeRange.equals(other.timeRange)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String thresholdString = threshold == null ? "" : " threshold=" + getThreshold();
		String timeRangeString = timeRange == null ? "" : ", timerange=" + getTimeRange(TimeUnit.MINUTES);
		return getName() + thresholdString + ", type=" + getThresholdType() + timeRangeString;
	}

}
