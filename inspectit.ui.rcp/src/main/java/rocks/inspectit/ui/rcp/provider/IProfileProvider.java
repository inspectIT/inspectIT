package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.shared.cs.ci.Profile;

/**
 * Interface for {@link Profile} provider. Note that this interface extends the
 * {@link ICmrRepositoryProvider} which in fact denotes to which CMR profile belongs.
 *
 * @author Ivan Senic
 *
 */
public interface IProfileProvider extends ICmrRepositoryProvider, Comparable<IProfileProvider> {

	/**
	 * Returns {@link Profile}.
	 *
	 * @return Returns {@link Profile}.
	 */
	Profile getProfile();
}
