package info.novatec.inspectit.rcp.version;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.version.InvalidVersionException;
import info.novatec.inspectit.version.VersionProvider;

/**
 * Provides the version of inspectIT in an OSGI environment.
 *
 * @author Stefan Siegl
 */
public class BundleVersionProvider implements VersionProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readVersion() throws InvalidVersionException {
		return InspectIT.getDefault().getBundle().getVersion().toString();
	}

}
