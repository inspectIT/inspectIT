package info.novatec.inspectit.rcp.ci.testers;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.provider.IProfileProvider;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester for the {@link Profile} properties.
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileTester extends PropertyTester {

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

		if ("isCommonProfile".equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isCommonProfile();
			}
		}

		if ("isDefaultProfile".equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isDefaultProfile();
			}
		}

		if ("isActiveProfile".equals(property)) {
			if (expectedValue instanceof Boolean) {
				return ((Boolean) expectedValue).booleanValue() == profile.isActive();
			}
		}

		return false;
	}

}
