package rocks.inspectit.shared.all.communication.data.eum;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Represents the initial request responsible for loadign hte page showed in this tab. This request
 * is always a direct child of the corresponding {@link PageLoadAction}.
 *
 * @author David Monschein, Jonas Kunz
 *
 */
public class PageLoadRequest extends AbstractRequest {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2379341294938690998L;

	/**
	 * If the capturing of the navigation timings or the speedindex was enabled, this field will
	 * hold the measured values.
	 */
	@JsonSerialize(include = Inclusion.NON_NULL)
	@JsonProperty
	private NavigationTimings navigationTimings;

	/**
	 * Stores the number of resources this pageload request laoded explicitly.
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	@JsonProperty
	private int resourceCount = -1;

	/**
	 * Stores the navigation timings and the speedindex if the corresponding modules are enabled.
	 * All timing fields (everything except the speedindex) store timestamps in milliseconds
	 * relative to the epoche.
	 *
	 * if a certain timing was not available, the corresponding field holds a zero value isntead.
	 *
	 * @author Jonas Kunz
	 *
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class NavigationTimings implements Serializable {

		/**
		 * serial version UID.
		 */
		private static final long serialVersionUID = 102220423619146599L;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("navigationStartW")
		private double navigationStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("unloadEventStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double unloadEventStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("unloadEventEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double unloadEventEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("redirectStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double redirectStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("redirectEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double redirectEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("fetchStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double fetchStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domainLookupStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domainLookupStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domainLookupEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domainLookupEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("connectStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double connectStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("connectEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double connectEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("secureConnectionStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double secureConnectionStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("requestStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double requestStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("responseStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double responseStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("responseEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double responseEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domLoadingW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domLoading = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domInteractiveW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domInteractive = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domContentLoadedEventStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domContentLoadedEventStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domContentLoadedEventEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domContentLoadedEventEnd = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("domCompleteW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double domComplete = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("loadEventStartW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double loadEventStart = 0;

		/**
		 * refers to @see <a href=
		 * "https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
		 * timings</a>.
		 */
		@JsonProperty("loadEventEndW")
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double loadEventEnd = 0;


		/**
		 * UEM speed index.
		 *
		 * @see <a href="https://github.com/WPO-Foundation/RUM-SpeedIndex">RUM speedindex</a>
		 */
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double speedIndex = 0;

		/**
		 * First paint event which is involved in the speedindex calculation progress.
		 */
		@JsonSerialize(include = Inclusion.NON_DEFAULT)
		private double firstPaint = 0;

		/**
		 * Gets {@link #navigationStart}.
		 *
		 * @return {@link #navigationStart}
		 */
		public double getNavigationStart() {
			return this.navigationStart;
		}

		/**
		 * Gets {@link #unloadEventStart}.
		 *
		 * @return {@link #unloadEventStart}
		 */
		public double getUnloadEventStart() {
			return this.unloadEventStart;
		}

		/**
		 * Gets {@link #unloadEventEnd}.
		 *
		 * @return {@link #unloadEventEnd}
		 */
		public double getUnloadEventEnd() {
			return this.unloadEventEnd;
		}

		/**
		 * Gets {@link #redirectStart}.
		 *
		 * @return {@link #redirectStart}
		 */
		public double getRedirectStart() {
			return this.redirectStart;
		}

		/**
		 * Gets {@link #redirectEnd}.
		 *
		 * @return {@link #redirectEnd}
		 */
		public double getRedirectEnd() {
			return this.redirectEnd;
		}

		/**
		 * Gets {@link #fetchStart}.
		 *
		 * @return {@link #fetchStart}
		 */
		public double getFetchStart() {
			return this.fetchStart;
		}

		/**
		 * Gets {@link #domainLookupStart}.
		 *
		 * @return {@link #domainLookupStart}
		 */
		public double getDomainLookupStart() {
			return this.domainLookupStart;
		}

		/**
		 * Gets {@link #domainLookupEnd}.
		 *
		 * @return {@link #domainLookupEnd}
		 */
		public double getDomainLookupEnd() {
			return this.domainLookupEnd;
		}

		/**
		 * Gets {@link #connectStart}.
		 *
		 * @return {@link #connectStart}
		 */
		public double getConnectStart() {
			return this.connectStart;
		}

		/**
		 * Gets {@link #connectEnd}.
		 *
		 * @return {@link #connectEnd}
		 */
		public double getConnectEnd() {
			return this.connectEnd;
		}

		/**
		 * Gets {@link #secureConnectionStart}.
		 *
		 * @return {@link #secureConnectionStart}
		 */
		public double getSecureConnectionStart() {
			return this.secureConnectionStart;
		}

		/**
		 * Gets {@link #requestStart}.
		 *
		 * @return {@link #requestStart}
		 */
		public double getRequestStart() {
			return this.requestStart;
		}

		/**
		 * Gets {@link #responseStart}.
		 *
		 * @return {@link #responseStart}
		 */
		public double getResponseStart() {
			return this.responseStart;
		}

		/**
		 * Gets {@link #responseEnd}.
		 *
		 * @return {@link #responseEnd}
		 */
		public double getResponseEnd() {
			return this.responseEnd;
		}

		/**
		 * Gets {@link #domLoading}.
		 *
		 * @return {@link #domLoading}
		 */
		public double getDomLoading() {
			return this.domLoading;
		}

		/**
		 * Gets {@link #domInteractive}.
		 *
		 * @return {@link #domInteractive}
		 */
		public double getDomInteractive() {
			return this.domInteractive;
		}

		/**
		 * Gets {@link #domContentLoadedEventStart}.
		 *
		 * @return {@link #domContentLoadedEventStart}
		 */
		public double getDomContentLoadedEventStart() {
			return this.domContentLoadedEventStart;
		}

		/**
		 * Gets {@link #domContentLoadedEventEnd}.
		 *
		 * @return {@link #domContentLoadedEventEnd}
		 */
		public double getDomContentLoadedEventEnd() {
			return this.domContentLoadedEventEnd;
		}

		/**
		 * Gets {@link #domComplete}.
		 *
		 * @return {@link #domComplete}
		 */
		public double getDomComplete() {
			return this.domComplete;
		}

		/**
		 * Gets {@link #loadEventStart}.
		 *
		 * @return {@link #loadEventStart}
		 */
		public double getLoadEventStart() {
			return this.loadEventStart;
		}

		/**
		 * Gets {@link #loadEventEnd}.
		 *
		 * @return {@link #loadEventEnd}
		 */
		public double getLoadEventEnd() {
			return this.loadEventEnd;
		}

		/**
		 * Gets {@link #speedIndex}.
		 *
		 * @return {@link #speedIndex}
		 */
		public double getSpeedIndex() {
			return this.speedIndex;
		}

		/**
		 * Gets {@link #firstPaint}.
		 *
		 * @return {@link #firstPaint}
		 */
		public double getFirstPaint() {
			return this.firstPaint;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(this.connectEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.connectStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domComplete);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domContentLoadedEventEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domContentLoadedEventStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domInteractive);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domLoading);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domainLookupEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.domainLookupStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.fetchStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.firstPaint);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.loadEventEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.loadEventStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.navigationStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.redirectEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.redirectStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.requestStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.responseEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.responseStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.secureConnectionStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.speedIndex);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.unloadEventEnd);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.unloadEventStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
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
			NavigationTimings other = (NavigationTimings) obj;
			if (Double.doubleToLongBits(this.connectEnd) != Double.doubleToLongBits(other.connectEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.connectStart) != Double.doubleToLongBits(other.connectStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domComplete) != Double.doubleToLongBits(other.domComplete)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domContentLoadedEventEnd) != Double.doubleToLongBits(other.domContentLoadedEventEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domContentLoadedEventStart) != Double.doubleToLongBits(other.domContentLoadedEventStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domInteractive) != Double.doubleToLongBits(other.domInteractive)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domLoading) != Double.doubleToLongBits(other.domLoading)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domainLookupEnd) != Double.doubleToLongBits(other.domainLookupEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.domainLookupStart) != Double.doubleToLongBits(other.domainLookupStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.fetchStart) != Double.doubleToLongBits(other.fetchStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.firstPaint) != Double.doubleToLongBits(other.firstPaint)) {
				return false;
			}
			if (Double.doubleToLongBits(this.loadEventEnd) != Double.doubleToLongBits(other.loadEventEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.loadEventStart) != Double.doubleToLongBits(other.loadEventStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.navigationStart) != Double.doubleToLongBits(other.navigationStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.redirectEnd) != Double.doubleToLongBits(other.redirectEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.redirectStart) != Double.doubleToLongBits(other.redirectStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.requestStart) != Double.doubleToLongBits(other.requestStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.responseEnd) != Double.doubleToLongBits(other.responseEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.responseStart) != Double.doubleToLongBits(other.responseStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.secureConnectionStart) != Double.doubleToLongBits(other.secureConnectionStart)) {
				return false;
			}
			if (Double.doubleToLongBits(this.speedIndex) != Double.doubleToLongBits(other.speedIndex)) {
				return false;
			}
			if (Double.doubleToLongBits(this.unloadEventEnd) != Double.doubleToLongBits(other.unloadEventEnd)) {
				return false;
			}
			if (Double.doubleToLongBits(this.unloadEventStart) != Double.doubleToLongBits(other.unloadEventStart)) { // NOPMD
				return false;
			}
			return true;
		}



	}

	/**
	 * Gets {@link #navigationTimings}.
	 *
	 * @return {@link #navigationTimings}
	 */
	public NavigationTimings getNavigationTimings() {
		return this.navigationTimings;
	}

	/**
	 * Gets {@link #resourceCount}.
	 *
	 * @return {@link #resourceCount}
	 */
	public int getResourceCount() {
		return this.resourceCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.navigationTimings == null) ? 0 : this.navigationTimings.hashCode());
		result = (prime * result) + this.resourceCount;
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
		PageLoadRequest other = (PageLoadRequest) obj;
		if (this.navigationTimings == null) {
			if (other.navigationTimings != null) {
				return false;
			}
		} else if (!this.navigationTimings.equals(other.navigationTimings)) {
			return false;
		}
		if (this.resourceCount != other.resourceCount) { // NOPMD
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAsyncCall() {
		return false;
	}

}
