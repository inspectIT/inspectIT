package info.novatec.inspectit.util;

import java.text.DateFormat;
import java.util.Date;

/**
 * Class defining the timeframe.
 * 
 * @author Ivan Senic
 * 
 */
public class TimeFrame implements Comparable<TimeFrame> {

	/**
	 * Date representing start of the time frame.
	 */
	private Date from;

	/**
	 * Date representing end of the time frame.
	 */
	private Date to;

	/**
	 * No-arg constructor.
	 */
	public TimeFrame() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param from
	 *            Date representing start of the time frame.
	 * @param to
	 *            Date representing end of the time frame.
	 */
	public TimeFrame(Date from, Date to) {
		super();
		this.from = from;
		this.to = to;
	}

	/**
	 * Gets {@link #from}.
	 * 
	 * @return {@link #from}
	 */
	public Date getFrom() {
		return from;
	}

	/**
	 * Sets {@link #from}.
	 * 
	 * @param from
	 *            New value for {@link #from}
	 */
	public void setFrom(Date from) {
		this.from = from;
	}

	/**
	 * Gets {@link #to}.
	 * 
	 * @return {@link #to}
	 */
	public Date getTo() {
		return to;
	}

	/**
	 * Sets {@link #to}.
	 * 
	 * @param to
	 *            New value for {@link #to}
	 */
	public void setTo(Date to) {
		this.to = to;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		TimeFrame other = (TimeFrame) obj;
		if (from == null) {
			if (other.from != null) {
				return false;
			}
		} else if (!from.equals(other.from)) {
			return false;
		}
		if (to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!to.equals(other.to)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Comparing by from date.
	 */
	public int compareTo(TimeFrame other) {
		return from.compareTo(other.getFrom());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance().format(from) + " - " + DateFormat.getDateTimeInstance().format(to);
	}

}
