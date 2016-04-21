package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * This class represents a TimerData Object with the relevant timer information needed for the
 * {@link #problemOccurrence} objects.
 *
 * @author Christian Voegele
 *
 */
public class TimerDataProblemOccurrence {

	/**
	 * The CPU duration.
	 */
	@JsonProperty(value = "cpuDuration")
	private double cpuDuration;

	/**
	 * The complete duration.
	 */
	@JsonProperty(value = "duration")
	private double duration;

	/**
	 * The exclusive duration.
	 */
	@JsonProperty(value = "exclusiveDuration")
	private double exclusiveDuration;

	/**
	 * The exclusiveCount.
	 */
	@JsonProperty(value = "exclusiveCount")
	private double exclusiveCount;

	/**
	 * The SQL-String of the Statement.
	 */
	@JsonProperty(value = "sql")
	private String sql = "UNDEFINED";

	/**
	 * The URI of the HTTPTimerData.
	 */
	@JsonProperty(value = "uri")
	private String uri = "UNDEFINED";

	/**
	 * Default constructor.
	 *
	 * @param timerData
	 *            The timerData that is taken into account
	 */
	public TimerDataProblemOccurrence(final TimerData timerData) {
		if (timerData == null) {
			throw new IllegalArgumentException("TimerData cannot be null!");
		}

		this.duration = timerData.getDuration();
		this.cpuDuration = timerData.getCpuDuration();
		this.exclusiveDuration = timerData.getExclusiveDuration();
		this.exclusiveCount = timerData.getExclusiveCount();

		if (timerData instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) timerData;
			this.sql = sqlStatementData.getSql();
		} else if (timerData instanceof HttpTimerData) {
			HttpTimerData httpTimerData = (HttpTimerData) timerData;
			this.uri = httpTimerData.getHttpInfo().getUri();
		}
	}

	/**
	 * Gets {@link #cpuDuration}.
	 *
	 * @return {@link #cpuDuration}
	 */
	public final double getCpuDuration() {
		return this.cpuDuration;
	}

	/**
	 * Sets {@link #cpuDuration}.
	 *
	 * @param cpuDuration
	 *            New value for {@link #cpuDuration}
	 */
	public final void setCpuDuration(double cpuDuration) {
		this.cpuDuration = cpuDuration;
	}

	/**
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	public final double getDuration() {
		return this.duration;
	}

	/**
	 * Sets {@link #duration}.
	 *
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public final void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Gets {@link #exclusiveDuration}.
	 *
	 * @return {@link #exclusiveDuration}
	 */
	public final double getExclusiveDuration() {
		return this.exclusiveDuration;
	}

	/**
	 * Sets {@link #exclusiveDuration}.
	 *
	 * @param exclusiveDuration
	 *            New value for {@link #exclusiveDuration}
	 */
	public final void setExclusiveDuration(double exclusiveDuration) {
		this.exclusiveDuration = exclusiveDuration;
	}

	/**
	 * Gets {@link #exclusiveCount}.
	 *
	 * @return {@link #exclusiveCount}
	 */
	public final double getExclusiveCount() {
		return this.exclusiveCount;
	}

	/**
	 * Sets {@link #exclusiveCount}.
	 *
	 * @param exclusiveCount
	 *            New value for {@link #exclusiveCount}
	 */
	public final void setExclusiveCount(double exclusiveCount) {
		this.exclusiveCount = exclusiveCount;
	}

	/**
	 * Gets {@link #sql}.
	 *
	 * @return {@link #sql}
	 */
	public final String getSql() {
		return this.sql;
	}

	/**
	 * Sets {@link #sql}.
	 *
	 * @param sql
	 *            New value for {@link #sql}
	 */
	public final void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Gets {@link #uri}.
	 *
	 * @return {@link #uri}
	 */
	public final String getUri() {
		return this.uri;
	}

	/**
	 * Sets {@link #uri}.
	 *
	 * @param uri
	 *            New value for {@link #uri}
	 */
	public final void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.exclusiveCount);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.cpuDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.duration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.exclusiveDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.sql == null) ? 0 : this.sql.hashCode());
		result = (prime * result) + ((this.uri == null) ? 0 : this.uri.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimerDataProblemOccurrence other = (TimerDataProblemOccurrence) obj;
		if (Double.doubleToLongBits(this.exclusiveCount) != Double.doubleToLongBits(other.exclusiveCount)) {
			return false;
		}
		if (Double.doubleToLongBits(this.cpuDuration) != Double.doubleToLongBits(other.cpuDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.exclusiveDuration) != Double.doubleToLongBits(other.exclusiveDuration)) {
			return false;
		}
		if (this.sql == null) {
			if (other.sql != null) {
				return false;
			}
		} else if (!this.sql.equals(other.sql)) {
			return false;
		}
		if (this.uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!this.uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
