package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.service.IRegistrationService;

/**
 * Interface for anybody that can provide {@link IInstrumentationApplier}.
 * 
 * @author Ivan Senic
 * 
 */
public interface IInstrumentationApplierProvider {

	/**
	 * Helper method that can return a proper {@link IInstrumentationApplier} that relates to the
	 * assignment type.
	 * 
	 * @param environment
	 *            {@link Environment} assignment belongs to.
	 * @param registrationService
	 *            {@link IRegistrationService} for the registration properties.
	 * @return {@link IInstrumentationApplier} that can be used to set instrumentation points to
	 *         class and method types.
	 */
	IInstrumentationApplier getInstrumentationApplier(Environment environment, IRegistrationService registrationService);

}
