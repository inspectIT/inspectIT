package info.novatec.inspectit.version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import info.novatec.inspectit.exception.enumeration.VersioningErrorCodeEnum;

/**
 * Mirrors the version scheme of inspectIT.
 *
 * A version string is connected by dots. It provides the major, minor and micro version followed by
 * an unique and always increasing build number.
 *
 * <p>
 * Use the inner class StringConverter to create versions from string based representations: <code>
 * Version version = Version.StringConverter.create("1.6.2.234")
 * </code>
 * <p>
 *
 * Examples: "1.6.3.234" -> major: 1, minor: 6, micro: 3, build 234
 *
 * @author Stefan Siegl
 */
public final class Version implements Comparable<Version> {

	/**
	 * The major version.
	 */
	private int major;

	/**
	 * The minor version.
	 */
	private int minor;

	/**
	 * The micro version.
	 */
	private int micro;

	/**
	 * The build number.
	 */
	private int buildno;

	/**
	 * Constructor.
	 */
	public Version() {
	}

	/**
	 * Constructor.
	 *
	 * @param major
	 *            major.
	 * @param minor
	 *            minor.
	 * @param buildno
	 *            buildno.
	 */
	public Version(int major, int minor, int buildno) {
		this.major = major;
		this.minor = minor;
		this.buildno = buildno;
	}

	/**
	 * Constructor.
	 *
	 * @param major
	 *            major.
	 * @param minor
	 *            minor.
	 * @param micro
	 *            micro.
	 * @param buildno
	 *            buildno.
	 */
	public Version(int major, int minor, int micro, int buildno) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.buildno = buildno;
	}

	/**
	 * Gets {@link #major}.
	 * 
	 * @return {@link #major}
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Sets {@link #major}.
	 * 
	 * @param major
	 *            New value for {@link #major}
	 */
	public void setMajor(int major) {
		this.major = major;
	}

	/**
	 * Gets {@link #minor}.
	 * 
	 * @return {@link #minor}
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Sets {@link #minor}.
	 * 
	 * @param minor
	 *            New value for {@link #minor}
	 */
	public void setMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * Gets {@link #micro}.
	 * 
	 * @return {@link #micro}
	 */
	public int getMicro() {
		return micro;
	}

	/**
	 * Sets {@link #micro}.
	 * 
	 * @param micro
	 *            New value for {@link #micro}
	 */
	public void setMicro(int micro) {
		this.micro = micro;
	}

	/**
	 * Gets {@link #buildno}.
	 * 
	 * @return {@link #buildno}
	 */
	public int getBuildno() {
		return buildno;
	}

	/**
	 * Sets {@link #buildno}.
	 * 
	 * @param buildno
	 *            New value for {@link #buildno}
	 */
	public void setBuildno(int buildno) {
		this.buildno = buildno;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Version o) {
		return new CompareToBuilder().append(major, o.major).append(minor, o.minor).append(micro, o.micro).append(buildno, o.buildno).toComparison();
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
		Version other = (Version) obj;
		if (buildno != other.buildno) {
			return false;
		}
		if (major != other.major) {
			return false;
		}
		if (micro != other.micro) {
			return false;
		}
		if (minor != other.minor) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + buildno;
		result = prime * result + major;
		result = prime * result + micro;
		result = prime * result + minor;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return major + "." + minor + "." + micro + "." + buildno;
	}

	/**
	 * Creates a new Version and checks for correctness.
	 *
	 * @param stringRepresentation
	 *            the string based representation.
	 * @return a version object that represent the version string.
	 * @throws InvalidVersionException
	 *             in case the version string is not valid.
	 */
	public static Version verifyAndCreate(String stringRepresentation) throws InvalidVersionException {
		Version version = new Version();

		if (StringUtils.isEmpty(stringRepresentation)) {
			throw new InvalidVersionException(VersioningErrorCodeEnum.VERSION_INVALID, null);
		}

		String[] splitByDot = StringUtils.split(stringRepresentation, ".");
		switch (splitByDot.length) {
		case 3:
			// micro is optional for historical reasons. This is basically a safeguard if an
			// older version is passed.
			version.major = getIntFromString(splitByDot[0]);
			version.minor = getIntFromString(splitByDot[1]);
			version.buildno = getIntFromString(splitByDot[2]);
			break;
		case 4:
			version.major = getIntFromString(splitByDot[0]);
			version.minor = getIntFromString(splitByDot[1]);
			version.micro = getIntFromString(splitByDot[2]);
			version.buildno = getIntFromString(splitByDot[3]);
			break;
		default:
			throw new InvalidVersionException("The version " + stringRepresentation + " does not follow the format major.minor[.micro].build", VersioningErrorCodeEnum.VERSION_INVALID, null);
		}

		return version;
	}

	/**
	 * Tries to convert one string element of the version to Integer.
	 *
	 * @param element
	 *            the sub element of the version.
	 * @return the integer representation
	 * @throws InvalidVersionException
	 *             if the conversion cannot be performed.
	 */
	private static int getIntFromString(String element) throws InvalidVersionException {
		try {
			return Integer.parseInt(element);
		} catch (NumberFormatException e) {
			throw new InvalidVersionException("Cannot convert sub element of the version to String", VersioningErrorCodeEnum.VERSION_INVALID, e);
		}
	}

}
