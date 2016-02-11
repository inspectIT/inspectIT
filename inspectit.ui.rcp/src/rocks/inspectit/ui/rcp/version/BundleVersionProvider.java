package rocks.inspectit.ui.rcp.version;

import rocks.inspectit.shared.all.version.InvalidVersionException;
import rocks.inspectit.shared.all.version.VersionProvider;
import rocks.inspectit.ui.rcp.InspectIT;

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
