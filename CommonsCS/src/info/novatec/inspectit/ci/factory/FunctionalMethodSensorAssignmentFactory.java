package info.novatec.inspectit.ci.factory;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.instrumentation.config.FunctionalInstrumentationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.stereotype.Component;

/**
 * Factory that can return appropriate {@link FunctionalMethodSensorAssignment}s based on the
 * {@link Environment} properties.
 *
 * @author Ivan Senic
 *
 */
@Component
@XmlTransient
public class FunctionalMethodSensorAssignmentFactory {

	/**
	 * Assignments for the class loading delegation.
	 */
	private Collection<FunctionalMethodSensorAssignment> classLoadingDelegationAssignments;

	/**
	 * Private as factory.
	 */
	protected FunctionalMethodSensorAssignmentFactory() {
	}

	/**
	 * Returns all functional assignments that should be active for the given {@link Environment}.
	 *
	 * @param environment
	 *            {@link Environment} holding the properties.
	 * @return Returns all functional assignments that should be active for the given
	 *         {@link Environment}.
	 */
	public Collection<FunctionalMethodSensorAssignment> getFunctionalAssignments(Environment environment) {
		if (!environment.isClassLoadingDelegation()) {
			return Collections.emptyList();
		}
		return classLoadingDelegationAssignments;
	}

	/**
	 * Initializes the assignments.
	 */
	@PostConstruct
	public void init() {
		// init all assignments
		// class loading delegation
		FunctionalMethodSensorAssignment cldDirect = new FunctionalMethodSensorAssignment(FunctionalInstrumentationType.CLASS_LOADING_DELEGATION);
		cldDirect.setClassName("java.lang.ClassLoader");
		cldDirect.setMethodName("loadClass");
		cldDirect.setParameters(Collections.singletonList("java.lang.String"));
		cldDirect.setPublicModifier(true);

		FunctionalMethodSensorAssignment cldSuperclass = new FunctionalMethodSensorAssignment(FunctionalInstrumentationType.CLASS_LOADING_DELEGATION);
		cldSuperclass.setClassName("java.lang.ClassLoader");
		cldSuperclass.setMethodName("loadClass");
		cldSuperclass.setParameters(Collections.singletonList("java.lang.String"));
		cldSuperclass.setPublicModifier(true);
		cldSuperclass.setSuperclass(true);

		classLoadingDelegationAssignments = Arrays.asList(cldDirect, cldSuperclass);
	}

}
