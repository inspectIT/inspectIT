package info.novatec.inspectit.version;

/**
 * Holds information about an released version. Beside {@link #version} information, this class
 * holds additional information about a release.
 * 
 * @author Ivan Senic
 * 
 */
public class VersionRelease implements Comparable<VersionRelease> {

	/**
	 * Version information.
	 */
	private final Version version;

	/**
	 * If this is a pre-release/preview version.
	 */
	private boolean preRelease;

	/**
	 * Link to the release.
	 */
	private String link;

	/**
	 * Default constructor.
	 * 
	 * @param version
	 *            {@link Version} information.
	 */
	public VersionRelease(Version version) {
		this.version = version;
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param version
	 *            Version information.
	 * @param preRelease
	 *            If this is a pre-release/preview version.
	 * @param link
	 *            Link to the release.
	 */
	public VersionRelease(Version version, boolean preRelease, String link) {
		this.version = version;
		this.preRelease = preRelease;
		this.link = link;
	}

	/**
	 * Gets {@link #preRelease}.
	 * 
	 * @return {@link #preRelease}
	 */
	public boolean isPreRelease() {
		return preRelease;
	}

	/**
	 * Sets {@link #preRelease}.
	 * 
	 * @param preRelease
	 *            New value for {@link #preRelease}
	 */
	public void setPreRelease(boolean preRelease) {
		this.preRelease = preRelease;
	}

	/**
	 * Gets {@link #link}.
	 * 
	 * @return {@link #link}
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets {@link #link}.
	 * 
	 * @param link
	 *            New value for {@link #link}
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Gets {@link #version}.
	 * 
	 * @return {@link #version}
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(VersionRelease o) {
		return version.compareTo(o.version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + (preRelease ? 1231 : 1237);
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		VersionRelease other = (VersionRelease) obj;
		if (link == null) {
			if (other.link != null) {
				return false;
			}
		} else if (!link.equals(other.link)) {
			return false;
		}
		if (preRelease != other.preRelease) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
