package rocks.inspectit.shared.cs.communication.data.diagnosis;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * This class represents a Diagnosis TimerData Object with the relevant timer information needed for
 * the {@link #ProblemOccurrence} objects.
 *
 * @author Christian Voegele
 *
 */
public class DiagnosisTimerData {

	/**
	 * The CPU duration.
	 */
	@JsonProperty(value = "cpuDuration")
	protected double cpuDuration;

	/**
	 * The complete duration.
	 */
	@JsonProperty(value = "duration")
	protected double duration;

	/**
	 * The exclusive duration.
	 */
	@JsonProperty(value = "exclusiveDuration")
	protected double exclusiveDuration;

	/**
	 * Additional metaData of type {@link MetaDataType} can be stored.
	 */
	@JsonProperty(value = "metaData")
	protected Map<MetaDataType, String> metaData = new HashMap<MetaDataType, String>();

	/**
	 * Default constructor.
	 *
	 * @param timerData
	 *            the timerData that is taken into account
	 */
	public DiagnosisTimerData(final TimerData timerData) {
		if (timerData == null) {
			throw new IllegalArgumentException("TimerData cannot be null!");
		}

		this.duration = timerData.getDuration();
		this.cpuDuration = timerData.getCpuDuration();
		this.exclusiveDuration = timerData.getExclusiveDuration();

		if (timerData instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) timerData;
			this.metaData.put(MetaDataType.SQL, sqlStatementData.getSql());
		} else if (timerData instanceof HttpTimerData) {
			HttpTimerData httpTimerData = (HttpTimerData) timerData;
			this.metaData.put(MetaDataType.URI, httpTimerData.getHttpInfo().getUri());
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
	public double getDuration() {
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
	public double getExclusiveDuration() {
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
	 * Gets {@link #metaData}.
	 *
	 * @return {@link #metaData}
	 */
	public final Map<MetaDataType, String> getMetaData() {
		return this.metaData;
	}

	/**
	 * Sets {@link #metaData}.
	 *
	 * @param metaData
	 *            New value for {@link #metaData}
	 */
	public final void setMetaData(Map<MetaDataType, String> metaData) {
		this.metaData = metaData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.cpuDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.duration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.exclusiveDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.metaData == null) ? 0 : this.metaData.hashCode());
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
		DiagnosisTimerData other = (DiagnosisTimerData) obj;
		if (Double.doubleToLongBits(this.cpuDuration) != Double.doubleToLongBits(other.cpuDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (Double.doubleToLongBits(this.exclusiveDuration) != Double.doubleToLongBits(other.exclusiveDuration)) {
			return false;
		}
		if (this.metaData == null) {
			if (other.metaData != null) {
				return false;
			}
		} else if (!this.metaData.equals(other.metaData)) {
			return false;
		}
		return true;
	}

	/**
	 * @author Christian Voegele
	 *
	 */
	public enum MetaDataType {
		/**
		 * Meta Data SQL.
		 */
		SQL,
		/**
		 * Meta Data URI.
		 */
		URI
	}

}
