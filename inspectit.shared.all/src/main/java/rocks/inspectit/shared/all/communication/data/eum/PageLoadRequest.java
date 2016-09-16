package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Representing a page load request.
 *
 * @author David Monschein
 */
public class PageLoadRequest extends Request {
	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 4366053612538374800L;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double navigationStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double unloadEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double unloadEventEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double redirectStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double redirectEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double fetchStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domainLookupStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domainLookupEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double connectStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double connectEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double secureConnectionStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double requestStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double responseStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double responseEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domLoadingW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domInteractiveW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domContentLoadedEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domContentLoadedEventEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double domCompleteW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double loadEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private double loadEventEndW;

	/**
	 * UEM speed index.
	 * @see <a href="https://github.com/WPO-Foundation/RUM-SpeedIndex">RUM speedindex</a>
	 */
	private double speedindex;

	/**
	 * First paint event which is involved in the speedindex calculation progress.
	 */
	private double firstpaint;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestType getRequestType() {
		return RequestType.PAGELOAD;
	}

	/**
	 * Gets {@link #navigationStartW}.
	 *
	 * @return {@link #navigationStartW}
	 */
	public double getNavigationStartW() {
		return this.navigationStartW;
	}

	/**
	 * Sets {@link #navigationStartW}.
	 *
	 * @param navigationStartW
	 *            New value for {@link #navigationStartW}
	 */
	public void setNavigationStartW(double navigationStartW) {
		this.navigationStartW = navigationStartW;
	}

	/**
	 * Gets {@link #unloadEventStartW} baselined to the navigation start.
	 *
	 * @return {@link #unloadEventStartW}
	 */
	public double getUnloadEventStartWBaseline() {
		return Math.max(this.unloadEventStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #unloadEventStartW}.
	 *
	 * @param unloadEventStartW
	 *            New value for {@link #unloadEventStartW}
	 */
	public void setUnloadEventStartW(double unloadEventStartW) {
		this.unloadEventStartW = unloadEventStartW;
	}

	/**
	 * Gets {@link #unloadEventEndW} baselined to the navigation start.
	 *
	 * @return {@link #unloadEventEndW}
	 */
	public double getUnloadEventEndWBaseline() {
		return Math.max(this.unloadEventEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #unloadEventEndW}.
	 *
	 * @param unloadEventEndW
	 *            New value for {@link #unloadEventEndW}
	 */
	public void setUnloadEventEndW(double unloadEventEndW) {
		this.unloadEventEndW = unloadEventEndW;
	}

	/**
	 * Gets {@link #redirectStartW} baselined to the navigation start.
	 *
	 * @return {@link #redirectStartW}
	 */
	public double getRedirectStartWBaseline() {
		return Math.max(this.redirectStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #redirectStartW}.
	 *
	 * @param redirectStartW
	 *            New value for {@link #redirectStartW}
	 */
	public void setRedirectStartW(double redirectStartW) {
		this.redirectStartW = redirectStartW;
	}

	/**
	 * Gets {@link #redirectEndW} baselined to the navigation start.
	 *
	 * @return {@link #redirectEndW}
	 */
	public double getRedirectEndWBaseline() {
		return Math.max(this.redirectEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #redirectEndW}.
	 *
	 * @param redirectEndW
	 *            New value for {@link #redirectEndW}
	 */
	public void setRedirectEndW(double redirectEndW) {
		this.redirectEndW = redirectEndW;
	}

	/**
	 * Gets {@link #fetchStartW} baselined to the navigation start.
	 *
	 * @return {@link #fetchStartW}
	 */
	public double getFetchStartWBaseline() {
		return Math.max(this.fetchStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #fetchStartW}.
	 *
	 * @param fetchStartW
	 *            New value for {@link #fetchStartW}
	 */
	public void setFetchStartW(double fetchStartW) {
		this.fetchStartW = fetchStartW;
	}

	/**
	 * Gets {@link #domainLookupStartW} baselined to the navigation start.
	 *
	 * @return {@link #domainLookupStartW}
	 */
	public double getDomainLookupStartWBaseline() {
		return Math.max(this.domainLookupStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domainLookupStartW}.
	 *
	 * @param domainLookupStartW
	 *            New value for {@link #domainLookupStartW}
	 */
	public void setDomainLookupStartW(double domainLookupStartW) {
		this.domainLookupStartW = domainLookupStartW;
	}

	/**
	 * Gets {@link #domainLookupEndW} baselined to the navigation start.
	 *
	 * @return {@link #domainLookupEndW}
	 */
	public double getDomainLookupEndWBaseline() {
		return Math.max(this.domainLookupEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domainLookupEndW}.
	 *
	 * @param domainLookupEndW
	 *            New value for {@link #domainLookupEndW}
	 */
	public void setDomainLookupEndW(double domainLookupEndW) {
		this.domainLookupEndW = domainLookupEndW;
	}

	/**
	 * Gets {@link #connectStartW} baselined to the navigation start.
	 *
	 * @return {@link #connectStartW}
	 */
	public double getConnectStartWBaseline() {
		return Math.max(this.connectStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #connectStartW}.
	 *
	 * @param connectStartW
	 *            New value for {@link #connectStartW}
	 */
	public void setConnectStartW(double connectStartW) {
		this.connectStartW = connectStartW;
	}

	/**
	 * Gets {@link #connectEndW} baselined to the navigation start.
	 *
	 * @return {@link #connectEndW}
	 */
	public double getConnectEndWBaseline() {
		return Math.max(this.connectEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #connectEndW}.
	 *
	 * @param connectEndW
	 *            New value for {@link #connectEndW}
	 */
	public void setConnectEndW(double connectEndW) {
		this.connectEndW = connectEndW;
	}

	/**
	 * Gets {@link #secureConnectionStartW} baselined to the navigation start.
	 *
	 * @return {@link #secureConnectionStartW}
	 */
	public double getSecureConnectionStartWBaseline() {
		return Math.max(this.secureConnectionStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #secureConnectionStartW}.
	 *
	 * @param secureConnectionStartW
	 *            New value for {@link #secureConnectionStartW}
	 */
	public void setSecureConnectionStartW(double secureConnectionStartW) {
		this.secureConnectionStartW = secureConnectionStartW;
	}

	/**
	 * Gets {@link #requestStartW} baselined to the navigation start.
	 *
	 * @return {@link #requestStartW}
	 */
	public double getRequestStartWBaseline() {
		return Math.max(this.requestStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #requestStartW}.
	 *
	 * @param requestStartW
	 *            New value for {@link #requestStartW}
	 */
	public void setRequestStartW(double requestStartW) {
		this.requestStartW = requestStartW;
	}

	/**
	 * Gets {@link #responseStartW} baselined to the navigation start.
	 *
	 * @return {@link #responseStartW}
	 */
	public double getResponseStartWBaseline() {
		return Math.max(this.responseStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #responseStartW}.
	 *
	 * @param responseStartW
	 *            New value for {@link #responseStartW}
	 */
	public void setResponseStartW(double responseStartW) {
		this.responseStartW = responseStartW;
	}

	/**
	 * Gets {@link #responseEndW}.
	 *
	 * @return {@link #responseEndW}
	 */
	public double getResponseEndWBaseline() {
		return Math.max(this.responseEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #responseEndW}.
	 *
	 * @param responseEndW
	 *            New value for {@link #responseEndW}
	 */
	public void setResponseEndW(double responseEndW) {
		this.responseEndW = responseEndW;
	}

	/**
	 * Gets {@link #domLoadingW} baselined to the navigation start.
	 *
	 * @return {@link #domLoadingW}
	 */
	public double getDomLoadingWBaseline() {
		return Math.max(this.domLoadingW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domLoadingW}.
	 *
	 * @param domLoadingW
	 *            New value for {@link #domLoadingW}
	 */
	public void setDomLoadingW(double domLoadingW) {
		this.domLoadingW = domLoadingW;
	}

	/**
	 * Gets {@link #domInteractiveW} baselined to the navigation start.
	 *
	 * @return {@link #domInteractiveW}
	 */
	public double getDomInteractiveWBaseline() {
		return Math.max(this.domInteractiveW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domInteractiveW}.
	 *
	 * @param domInteractiveW
	 *            New value for {@link #domInteractiveW}
	 */
	public void setDomInteractiveW(double domInteractiveW) {
		this.domInteractiveW = domInteractiveW;
	}

	/**
	 * Gets {@link #domContentLoadedEventStartW} baselined to the navigation start.
	 *
	 * @return {@link #domContentLoadedEventStartW}
	 */
	public double getDomContentLoadedEventStartWBaseline() {
		return Math.max(this.domContentLoadedEventStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domContentLoadedEventStartW}.
	 *
	 * @param domContentLoadedEventStartW
	 *            New value for {@link #domContentLoadedEventStartW}
	 */
	public void setDomContentLoadedEventStartW(double domContentLoadedEventStartW) {
		this.domContentLoadedEventStartW = domContentLoadedEventStartW;
	}

	/**
	 * Gets {@link #domContentLoadedEventEndW} baselined to the navigation start.
	 *
	 * @return {@link #domContentLoadedEventEndW}
	 */
	public double getDomContentLoadedEventEndWBaseline() {
		return Math.max(this.domContentLoadedEventEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domContentLoadedEventEndW}.
	 *
	 * @param domContentLoadedEventEndW
	 *            New value for {@link #domContentLoadedEventEndW}
	 */
	public void setDomContentLoadedEventEndW(double domContentLoadedEventEndW) {
		this.domContentLoadedEventEndW = domContentLoadedEventEndW;
	}

	/**
	 * Gets {@link #domCompleteW} baselined to the navigation start.
	 *
	 * @return {@link #domCompleteW}
	 */
	public double getDomCompleteWBaseline() {
		return Math.max(this.domCompleteW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #domCompleteW}.
	 *
	 * @param domCompleteW
	 *            New value for {@link #domCompleteW}
	 */
	public void setDomCompleteW(double domCompleteW) {
		this.domCompleteW = domCompleteW;
	}

	/**
	 * Gets {@link #loadEventStartW} baselined to the navigation start.
	 *
	 * @return {@link #loadEventStartW}
	 */
	public double getLoadEventStartWBaseline() {
		return Math.max(this.loadEventStartW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #loadEventStartW}.
	 *
	 * @param loadEventStartW
	 *            New value for {@link #loadEventStartW}
	 */
	public void setLoadEventStartW(double loadEventStartW) {
		this.loadEventStartW = loadEventStartW;
	}

	/**
	 * Gets {@link #loadEventEndW} baselined to the navigation start.
	 *
	 * @return {@link #loadEventEndW}
	 */
	public double getLoadEventEndWBaseline() {
		return Math.max(this.loadEventEndW - navigationStartW, 0L);
	}

	/**
	 * Sets {@link #loadEventEndW}.
	 *
	 * @param loadEventEndW
	 *            New value for {@link #loadEventEndW}
	 */
	public void setLoadEventEndW(double loadEventEndW) {
		this.loadEventEndW = loadEventEndW;
	}

	/**
	 * Gets {@link #serialVersionUID}.
	 *
	 * @return {@link #serialVersionUID}
	 */
	public static double getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Gets {@link #unloadEventStartW}.
	 *
	 * @return {@link #unloadEventStartW}
	 */
	public double getUnloadEventStartW() {
		return this.unloadEventStartW;
	}

	/**
	 * Gets {@link #unloadEventEndW}.
	 *
	 * @return {@link #unloadEventEndW}
	 */
	public double getUnloadEventEndW() {
		return this.unloadEventEndW;
	}

	/**
	 * Gets {@link #redirectStartW}.
	 *
	 * @return {@link #redirectStartW}
	 */
	public double getRedirectStartW() {
		return this.redirectStartW;
	}

	/**
	 * Gets {@link #redirectEndW}.
	 *
	 * @return {@link #redirectEndW}
	 */
	public double getRedirectEndW() {
		return this.redirectEndW;
	}

	/**
	 * Gets {@link #fetchStartW}.
	 *
	 * @return {@link #fetchStartW}
	 */
	public double getFetchStartW() {
		return this.fetchStartW;
	}

	/**
	 * Gets {@link #domainLookupStartW}.
	 *
	 * @return {@link #domainLookupStartW}
	 */
	public double getDomainLookupStartW() {
		return this.domainLookupStartW;
	}

	/**
	 * Gets {@link #domainLookupEndW}.
	 *
	 * @return {@link #domainLookupEndW}
	 */
	public double getDomainLookupEndW() {
		return this.domainLookupEndW;
	}

	/**
	 * Gets {@link #connectStartW}.
	 *
	 * @return {@link #connectStartW}
	 */
	public double getConnectStartW() {
		return this.connectStartW;
	}

	/**
	 * Gets {@link #connectEndW}.
	 *
	 * @return {@link #connectEndW}
	 */
	public double getConnectEndW() {
		return this.connectEndW;
	}

	/**
	 * Gets {@link #secureConnectionStartW}.
	 *
	 * @return {@link #secureConnectionStartW}
	 */
	public double getSecureConnectionStartW() {
		return this.secureConnectionStartW;
	}

	/**
	 * Gets {@link #requestStartW}.
	 *
	 * @return {@link #requestStartW}
	 */
	public double getRequestStartW() {
		return this.requestStartW;
	}

	/**
	 * Gets {@link #responseStartW}.
	 *
	 * @return {@link #responseStartW}
	 */
	public double getResponseStartW() {
		return this.responseStartW;
	}

	/**
	 * Gets {@link #responseEndW}.
	 *
	 * @return {@link #responseEndW}
	 */
	public double getResponseEndW() {
		return this.responseEndW;
	}

	/**
	 * Gets {@link #domLoadingW}.
	 *
	 * @return {@link #domLoadingW}
	 */
	public double getDomLoadingW() {
		return this.domLoadingW;
	}

	/**
	 * Gets {@link #domInteractiveW}.
	 *
	 * @return {@link #domInteractiveW}
	 */
	public double getDomInteractiveW() {
		return this.domInteractiveW;
	}

	/**
	 * Gets {@link #domContentLoadedEventStartW}.
	 *
	 * @return {@link #domContentLoadedEventStartW}
	 */
	public double getDomContentLoadedEventStartW() {
		return this.domContentLoadedEventStartW;
	}

	/**
	 * Gets {@link #domContentLoadedEventEndW}.
	 *
	 * @return {@link #domContentLoadedEventEndW}
	 */
	public double getDomContentLoadedEventEndW() {
		return this.domContentLoadedEventEndW;
	}

	/**
	 * Gets {@link #domCompleteW}.
	 *
	 * @return {@link #domCompleteW}
	 */
	public double getDomCompleteW() {
		return this.domCompleteW;
	}

	/**
	 * Gets {@link #loadEventStartW}.
	 *
	 * @return {@link #loadEventStartW}
	 */
	public double getLoadEventStartW() {
		return this.loadEventStartW;
	}

	/**
	 * Gets {@link #loadEventEndW}.
	 *
	 * @return {@link #loadEventEndW}
	 */
	public double getLoadEventEndW() {
		return this.loadEventEndW;
	}

	/**
	 * Gets {@link #firstpaint}.
	 *
	 * @return {@link #firstpaint}
	 */
	public double getFirstpaint() {
		return this.firstpaint;
	}

	/**
	 * Sets {@link #firstpaint}.
	 *
	 * @param firstpaint
	 *            New value for {@link #firstpaint}
	 */
	public void setFirstpaint(double firstpaint) {
		this.firstpaint = firstpaint;
	}

	/**
	 * Gets {@link #speedindex}.
	 *
	 * @return {@link #speedindex}
	 */
	public double getSpeedindex() {
		return speedindex;
	}

	/**
	 * Sets {@link #speedindex}.
	 *
	 * @param speedindex
	 *            New value for {@link #speedindex}
	 */
	public void setSpeedindex(double speedindex) {
		this.speedindex = speedindex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(this.connectEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.connectStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domCompleteW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domContentLoadedEventEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domContentLoadedEventStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domInteractiveW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domLoadingW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domainLookupEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.domainLookupStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.fetchStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.firstpaint);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.loadEventEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.loadEventStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.navigationStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.redirectEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.redirectStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.requestStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.responseEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.responseStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.secureConnectionStartW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.speedindex);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.unloadEventEndW);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.unloadEventStartW);
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PageLoadRequest other = (PageLoadRequest) obj;
		if (Double.doubleToLongBits(this.connectEndW) != Double.doubleToLongBits(other.connectEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.connectStartW) != Double.doubleToLongBits(other.connectStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domCompleteW) != Double.doubleToLongBits(other.domCompleteW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domContentLoadedEventEndW) != Double.doubleToLongBits(other.domContentLoadedEventEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domContentLoadedEventStartW) != Double.doubleToLongBits(other.domContentLoadedEventStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domInteractiveW) != Double.doubleToLongBits(other.domInteractiveW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domLoadingW) != Double.doubleToLongBits(other.domLoadingW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domainLookupEndW) != Double.doubleToLongBits(other.domainLookupEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.domainLookupStartW) != Double.doubleToLongBits(other.domainLookupStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.fetchStartW) != Double.doubleToLongBits(other.fetchStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.firstpaint) != Double.doubleToLongBits(other.firstpaint)) {
			return false;
		}
		if (Double.doubleToLongBits(this.loadEventEndW) != Double.doubleToLongBits(other.loadEventEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.loadEventStartW) != Double.doubleToLongBits(other.loadEventStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.navigationStartW) != Double.doubleToLongBits(other.navigationStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.redirectEndW) != Double.doubleToLongBits(other.redirectEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.redirectStartW) != Double.doubleToLongBits(other.redirectStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.requestStartW) != Double.doubleToLongBits(other.requestStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.responseEndW) != Double.doubleToLongBits(other.responseEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.responseStartW) != Double.doubleToLongBits(other.responseStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.secureConnectionStartW) != Double.doubleToLongBits(other.secureConnectionStartW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.speedindex) != Double.doubleToLongBits(other.speedindex)) {
			return false;
		}
		if (Double.doubleToLongBits(this.unloadEventEndW) != Double.doubleToLongBits(other.unloadEventEndW)) {
			return false;
		}
		if (Double.doubleToLongBits(this.unloadEventStartW) != Double.doubleToLongBits(other.unloadEventStartW)) {
			return false;
		}
		return true;
	}


}
