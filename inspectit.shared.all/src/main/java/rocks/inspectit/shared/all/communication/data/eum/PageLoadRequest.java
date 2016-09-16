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
	private long navigationStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long unloadEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long unloadEventEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long redirectStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long redirectEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long fetchStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domainLookupStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domainLookupEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long connectStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long connectEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long secureConnectionStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long requestStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long responseStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long responseEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domLoadingW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domInteractiveW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domContentLoadedEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domContentLoadedEventEndW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long domCompleteW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long loadEventStartW;
	/**
	 * refers to @see
	 * <a href="https://www.w3.org/TR/navigation-timing/#sec-navigation-timing-interface">Navigation
	 * timings</a>.
	 */
	private long loadEventEndW;

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
	public long getNavigationStartW() {
		return this.navigationStartW;
	}

	/**
	 * Sets {@link #navigationStartW}.
	 *
	 * @param navigationStartW
	 *            New value for {@link #navigationStartW}
	 */
	public void setNavigationStartW(long navigationStartW) {
		this.navigationStartW = navigationStartW;
	}

	/**
	 * Gets {@link #unloadEventStartW}.
	 *
	 * @return {@link #unloadEventStartW}
	 */
	public long getUnloadEventStartW() {
		return this.unloadEventStartW;
	}

	/**
	 * Sets {@link #unloadEventStartW}.
	 *
	 * @param unloadEventStartW
	 *            New value for {@link #unloadEventStartW}
	 */
	public void setUnloadEventStartW(long unloadEventStartW) {
		this.unloadEventStartW = unloadEventStartW;
	}

	/**
	 * Gets {@link #unloadEventEndW}.
	 *
	 * @return {@link #unloadEventEndW}
	 */
	public long getUnloadEventEndW() {
		return this.unloadEventEndW;
	}

	/**
	 * Sets {@link #unloadEventEndW}.
	 *
	 * @param unloadEventEndW
	 *            New value for {@link #unloadEventEndW}
	 */
	public void setUnloadEventEndW(long unloadEventEndW) {
		this.unloadEventEndW = unloadEventEndW;
	}

	/**
	 * Gets {@link #redirectStartW}.
	 *
	 * @return {@link #redirectStartW}
	 */
	public long getRedirectStartW() {
		return this.redirectStartW;
	}

	/**
	 * Sets {@link #redirectStartW}.
	 *
	 * @param redirectStartW
	 *            New value for {@link #redirectStartW}
	 */
	public void setRedirectStartW(long redirectStartW) {
		this.redirectStartW = redirectStartW;
	}

	/**
	 * Gets {@link #redirectEndW}.
	 *
	 * @return {@link #redirectEndW}
	 */
	public long getRedirectEndW() {
		return this.redirectEndW;
	}

	/**
	 * Sets {@link #redirectEndW}.
	 *
	 * @param redirectEndW
	 *            New value for {@link #redirectEndW}
	 */
	public void setRedirectEndW(long redirectEndW) {
		this.redirectEndW = redirectEndW;
	}

	/**
	 * Gets {@link #fetchStartW}.
	 *
	 * @return {@link #fetchStartW}
	 */
	public long getFetchStartW() {
		return this.fetchStartW;
	}

	/**
	 * Sets {@link #fetchStartW}.
	 *
	 * @param fetchStartW
	 *            New value for {@link #fetchStartW}
	 */
	public void setFetchStartW(long fetchStartW) {
		this.fetchStartW = fetchStartW;
	}

	/**
	 * Gets {@link #domainLookupStartW}.
	 *
	 * @return {@link #domainLookupStartW}
	 */
	public long getDomainLookupStartW() {
		return this.domainLookupStartW;
	}

	/**
	 * Sets {@link #domainLookupStartW}.
	 *
	 * @param domainLookupStartW
	 *            New value for {@link #domainLookupStartW}
	 */
	public void setDomainLookupStartW(long domainLookupStartW) {
		this.domainLookupStartW = domainLookupStartW;
	}

	/**
	 * Gets {@link #domainLookupEndW}.
	 *
	 * @return {@link #domainLookupEndW}
	 */
	public long getDomainLookupEndW() {
		return this.domainLookupEndW;
	}

	/**
	 * Sets {@link #domainLookupEndW}.
	 *
	 * @param domainLookupEndW
	 *            New value for {@link #domainLookupEndW}
	 */
	public void setDomainLookupEndW(long domainLookupEndW) {
		this.domainLookupEndW = domainLookupEndW;
	}

	/**
	 * Gets {@link #connectStartW}.
	 *
	 * @return {@link #connectStartW}
	 */
	public long getConnectStartW() {
		return this.connectStartW;
	}

	/**
	 * Sets {@link #connectStartW}.
	 *
	 * @param connectStartW
	 *            New value for {@link #connectStartW}
	 */
	public void setConnectStartW(long connectStartW) {
		this.connectStartW = connectStartW;
	}

	/**
	 * Gets {@link #connectEndW}.
	 *
	 * @return {@link #connectEndW}
	 */
	public long getConnectEndW() {
		return this.connectEndW;
	}

	/**
	 * Sets {@link #connectEndW}.
	 *
	 * @param connectEndW
	 *            New value for {@link #connectEndW}
	 */
	public void setConnectEndW(long connectEndW) {
		this.connectEndW = connectEndW;
	}

	/**
	 * Gets {@link #secureConnectionStartW}.
	 *
	 * @return {@link #secureConnectionStartW}
	 */
	public long getSecureConnectionStartW() {
		return this.secureConnectionStartW;
	}

	/**
	 * Sets {@link #secureConnectionStartW}.
	 *
	 * @param secureConnectionStartW
	 *            New value for {@link #secureConnectionStartW}
	 */
	public void setSecureConnectionStartW(long secureConnectionStartW) {
		this.secureConnectionStartW = secureConnectionStartW;
	}

	/**
	 * Gets {@link #requestStartW}.
	 *
	 * @return {@link #requestStartW}
	 */
	public long getRequestStartW() {
		return this.requestStartW;
	}

	/**
	 * Sets {@link #requestStartW}.
	 *
	 * @param requestStartW
	 *            New value for {@link #requestStartW}
	 */
	public void setRequestStartW(long requestStartW) {
		this.requestStartW = requestStartW;
	}

	/**
	 * Gets {@link #responseStartW}.
	 *
	 * @return {@link #responseStartW}
	 */
	public long getResponseStartW() {
		return this.responseStartW;
	}

	/**
	 * Sets {@link #responseStartW}.
	 *
	 * @param responseStartW
	 *            New value for {@link #responseStartW}
	 */
	public void setResponseStartW(long responseStartW) {
		this.responseStartW = responseStartW;
	}

	/**
	 * Gets {@link #responseEndW}.
	 *
	 * @return {@link #responseEndW}
	 */
	public long getResponseEndW() {
		return this.responseEndW;
	}

	/**
	 * Sets {@link #responseEndW}.
	 *
	 * @param responseEndW
	 *            New value for {@link #responseEndW}
	 */
	public void setResponseEndW(long responseEndW) {
		this.responseEndW = responseEndW;
	}

	/**
	 * Gets {@link #domLoadingW}.
	 *
	 * @return {@link #domLoadingW}
	 */
	public long getDomLoadingW() {
		return this.domLoadingW;
	}

	/**
	 * Sets {@link #domLoadingW}.
	 *
	 * @param domLoadingW
	 *            New value for {@link #domLoadingW}
	 */
	public void setDomLoadingW(long domLoadingW) {
		this.domLoadingW = domLoadingW;
	}

	/**
	 * Gets {@link #domInteractiveW}.
	 *
	 * @return {@link #domInteractiveW}
	 */
	public long getDomInteractiveW() {
		return this.domInteractiveW;
	}

	/**
	 * Sets {@link #domInteractiveW}.
	 *
	 * @param domInteractiveW
	 *            New value for {@link #domInteractiveW}
	 */
	public void setDomInteractiveW(long domInteractiveW) {
		this.domInteractiveW = domInteractiveW;
	}

	/**
	 * Gets {@link #domContentLoadedEventStartW}.
	 *
	 * @return {@link #domContentLoadedEventStartW}
	 */
	public long getDomContentLoadedEventStartW() {
		return this.domContentLoadedEventStartW;
	}

	/**
	 * Sets {@link #domContentLoadedEventStartW}.
	 *
	 * @param domContentLoadedEventStartW
	 *            New value for {@link #domContentLoadedEventStartW}
	 */
	public void setDomContentLoadedEventStartW(long domContentLoadedEventStartW) {
		this.domContentLoadedEventStartW = domContentLoadedEventStartW;
	}

	/**
	 * Gets {@link #domContentLoadedEventEndW}.
	 *
	 * @return {@link #domContentLoadedEventEndW}
	 */
	public long getDomContentLoadedEventEndW() {
		return this.domContentLoadedEventEndW;
	}

	/**
	 * Sets {@link #domContentLoadedEventEndW}.
	 *
	 * @param domContentLoadedEventEndW
	 *            New value for {@link #domContentLoadedEventEndW}
	 */
	public void setDomContentLoadedEventEndW(long domContentLoadedEventEndW) {
		this.domContentLoadedEventEndW = domContentLoadedEventEndW;
	}

	/**
	 * Gets {@link #domCompleteW}.
	 *
	 * @return {@link #domCompleteW}
	 */
	public long getDomCompleteW() {
		return this.domCompleteW;
	}

	/**
	 * Sets {@link #domCompleteW}.
	 *
	 * @param domCompleteW
	 *            New value for {@link #domCompleteW}
	 */
	public void setDomCompleteW(long domCompleteW) {
		this.domCompleteW = domCompleteW;
	}

	/**
	 * Gets {@link #loadEventStartW}.
	 *
	 * @return {@link #loadEventStartW}
	 */
	public long getLoadEventStartW() {
		return this.loadEventStartW;
	}

	/**
	 * Sets {@link #loadEventStartW}.
	 *
	 * @param loadEventStartW
	 *            New value for {@link #loadEventStartW}
	 */
	public void setLoadEventStartW(long loadEventStartW) {
		this.loadEventStartW = loadEventStartW;
	}

	/**
	 * Gets {@link #loadEventEndW}.
	 *
	 * @return {@link #loadEventEndW}
	 */
	public long getLoadEventEndW() {
		return this.loadEventEndW;
	}

	/**
	 * Sets {@link #loadEventEndW}.
	 *
	 * @param loadEventEndW
	 *            New value for {@link #loadEventEndW}
	 */
	public void setLoadEventEndW(long loadEventEndW) {
		this.loadEventEndW = loadEventEndW;
	}

	/**
	 * Gets {@link #firstpaint}.
	 *
	 * @return {@link #firstpaint}
	 */
	public double getFirstpaint() {
		return firstpaint;
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
	 * Rebases all navigation timing api values to the navigation start.
	 */
	public void baseline() {
		this.setConnectEndW(Math.max(connectEndW - navigationStartW, 0L));
		this.setConnectStartW(Math.max(connectStartW - navigationStartW, 0L));
		this.setDomainLookupEndW(Math.max(domainLookupEndW - navigationStartW, 0L));
		this.setDomainLookupStartW(Math.max(domainLookupStartW - navigationStartW, 0L));
		this.setDomCompleteW(Math.max(domCompleteW - navigationStartW, 0L));
		this.setDomContentLoadedEventEndW(Math.max(domContentLoadedEventEndW - navigationStartW, 0L));
		this.setDomContentLoadedEventStartW(Math.max(domContentLoadedEventStartW - navigationStartW, 0L));
		this.setDomInteractiveW(Math.max(domInteractiveW - navigationStartW, 0L));
		this.setDomLoadingW(Math.max(domLoadingW - navigationStartW, 0L));
		this.setFetchStartW(Math.max(fetchStartW - navigationStartW, 0L));
		this.setLoadEventEndW(Math.max(loadEventEndW - navigationStartW, 0L));
		this.setLoadEventStartW(Math.max(loadEventStartW - navigationStartW, 0L));
		this.setRedirectEndW(Math.max(redirectEndW - navigationStartW, 0L));
		this.setRedirectStartW(Math.max(redirectStartW - navigationStartW, 0L));
		this.setRequestStartW(Math.max(requestStartW - navigationStartW, 0L));
		this.setResponseEndW(Math.max(responseEndW - navigationStartW, 0L));
		this.setResponseStartW(Math.max(responseStartW - navigationStartW, 0L));
		this.setSecureConnectionStartW(Math.max(secureConnectionStartW - navigationStartW, 0L));
		this.setUnloadEventEndW(Math.max(unloadEventEndW - navigationStartW, 0L));
		this.setUnloadEventStartW(Math.max(unloadEventStartW - navigationStartW, 0L));
		this.setNavigationStartW(0L);
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
		if (this.connectEndW != other.connectEndW) {
			return false;
		}
		if (this.connectStartW != other.connectStartW) {
			return false;
		}
		if (this.domCompleteW != other.domCompleteW) {
			return false;
		}
		if (this.domContentLoadedEventEndW != other.domContentLoadedEventEndW) {
			return false;
		}
		if (this.domContentLoadedEventStartW != other.domContentLoadedEventStartW) {
			return false;
		}
		if (this.domInteractiveW != other.domInteractiveW) {
			return false;
		}
		if (this.domLoadingW != other.domLoadingW) {
			return false;
		}
		if (this.domainLookupEndW != other.domainLookupEndW) {
			return false;
		}
		if (this.domainLookupStartW != other.domainLookupStartW) {
			return false;
		}
		if (this.fetchStartW != other.fetchStartW) {
			return false;
		}
		if (Double.doubleToLongBits(this.firstpaint) != Double.doubleToLongBits(other.firstpaint)) {
			return false;
		}
		if (this.loadEventEndW != other.loadEventEndW) {
			return false;
		}
		if (this.loadEventStartW != other.loadEventStartW) {
			return false;
		}
		if (this.navigationStartW != other.navigationStartW) {
			return false;
		}
		if (this.redirectEndW != other.redirectEndW) {
			return false;
		}
		if (this.redirectStartW != other.redirectStartW) {
			return false;
		}
		if (this.requestStartW != other.requestStartW) {
			return false;
		}
		if (this.responseEndW != other.responseEndW) {
			return false;
		}
		if (this.responseStartW != other.responseStartW) {
			return false;
		}
		if (this.secureConnectionStartW != other.secureConnectionStartW) {
			return false;
		}
		if (Double.doubleToLongBits(this.speedindex) != Double.doubleToLongBits(other.speedindex)) {
			return false;
		}
		if (this.unloadEventEndW != other.unloadEventEndW) {
			return false;
		}
		return this.unloadEventStartW == other.unloadEventStartW;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + (int) (this.connectEndW ^ (this.connectEndW >>> 32));
		result = (prime * result) + (int) (this.connectStartW ^ (this.connectStartW >>> 32));
		result = (prime * result) + (int) (this.domCompleteW ^ (this.domCompleteW >>> 32));
		result = (prime * result) + (int) (this.domContentLoadedEventEndW ^ (this.domContentLoadedEventEndW >>> 32));
		result = (prime * result) + (int) (this.domContentLoadedEventStartW ^ (this.domContentLoadedEventStartW >>> 32));
		result = (prime * result) + (int) (this.domInteractiveW ^ (this.domInteractiveW >>> 32));
		result = (prime * result) + (int) (this.domLoadingW ^ (this.domLoadingW >>> 32));
		result = (prime * result) + (int) (this.domainLookupEndW ^ (this.domainLookupEndW >>> 32));
		result = (prime * result) + (int) (this.domainLookupStartW ^ (this.domainLookupStartW >>> 32));
		result = (prime * result) + (int) (this.fetchStartW ^ (this.fetchStartW >>> 32));
		long temp;
		temp = Double.doubleToLongBits(this.firstpaint);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (int) (this.loadEventEndW ^ (this.loadEventEndW >>> 32));
		result = (prime * result) + (int) (this.loadEventStartW ^ (this.loadEventStartW >>> 32));
		result = (prime * result) + (int) (this.navigationStartW ^ (this.navigationStartW >>> 32));
		result = (prime * result) + (int) (this.redirectEndW ^ (this.redirectEndW >>> 32));
		result = (prime * result) + (int) (this.redirectStartW ^ (this.redirectStartW >>> 32));
		result = (prime * result) + (int) (this.requestStartW ^ (this.requestStartW >>> 32));
		result = (prime * result) + (int) (this.responseEndW ^ (this.responseEndW >>> 32));
		result = (prime * result) + (int) (this.responseStartW ^ (this.responseStartW >>> 32));
		result = (prime * result) + (int) (this.secureConnectionStartW ^ (this.secureConnectionStartW >>> 32));
		temp = Double.doubleToLongBits(this.speedindex);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (int) (this.unloadEventEndW ^ (this.unloadEventEndW >>> 32));
		result = (prime * result) + (int) (this.unloadEventStartW ^ (this.unloadEventStartW >>> 32));
		return result;
	}
}
