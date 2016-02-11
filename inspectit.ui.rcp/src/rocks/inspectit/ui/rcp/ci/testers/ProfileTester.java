package rocks.inspectit.ui.rcp.ci.testers;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;

/**
 * Tester for the {@link Profile} properties.
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileTester extends PropertyTester {

	/**
	 * Tester property for the active profile.
	 */
	private static final String ACTIVE_PROFILE_PROPERTY = "isActiveProfile";

	/**
	 * Tester property for the default profile.
	 */
	private static final String DEFAULT_PROFILE_PROPERTY = "isDefaultProfile";

	/**
	 * Tester property for the common profile.
	 */
	private static final String COMMON_PROFILE_PROPERTY = "isCommonProfile";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Profile profile = null;
		if (receiver instanceof Profile) {
			profile = (Profile) receiver;
		} else if (receiver instanceof IProfileProvider) {
			profile = ((IProfileProvider) receiver).getProfile();
		} else {
			return false;
		}

		if (COMMON_PROFILE_PROPERTY.equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isCommonProfile();
			}
		}

		if (DEFAULT_PROFILE_PROPERTY.equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isDefaultProfile();
			}
		}

		if (ACTIVE_PROFILE_PROPERTY.equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isActive();
			}
		}

		return false;
	}

}
