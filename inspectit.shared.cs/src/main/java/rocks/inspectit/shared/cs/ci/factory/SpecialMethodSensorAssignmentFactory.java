package rocks.inspectit.shared.cs.ci.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.instrumentation.config.SpecialInstrumentationType;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;

/**
 * Factory that can return appropriate {@link SpecialMethodSensorAssignment}s based on the
 * {@link Environment} properties.
 *
 * @author Ivan Senic
 *
 */
@Component
@XmlTransient
public class SpecialMethodSensorAssignmentFactory {

	/**
	 * Assignments for the class loading delegation.
	 */
	private Collection<SpecialMethodSensorAssignment> classLoadingDelegationAssignments;

	/**
	 * Private as factory.
	 */
	protected SpecialMethodSensorAssignmentFactory() {
	}

	/**
	 * Returns all special assignments that should be active for the given {@link Environment}.
	 *
	 * @param environment
	 *            {@link Environment} holding the properties.
	 * @return Returns all functional assignments that should be active for the given
	 *         {@link Environment}.
	 */
	public Collection<SpecialMethodSensorAssignment> getSpecialAssignments(Environment environment) {
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
		SpecialMethodSensorAssignment cldDirect = new SpecialMethodSensorAssignment(SpecialInstrumentationType.CLASS_LOADING_DELEGATION);
		cldDirect.setClassName("java.lang.ClassLoader");
		cldDirect.setMethodName("loadClass");
		cldDirect.setParameters(Collections.singletonList("java.lang.String"));
		cldDirect.setPublicModifier(true);

		SpecialMethodSensorAssignment cldSuperclass = new SpecialMethodSensorAssignment(SpecialInstrumentationType.CLASS_LOADING_DELEGATION);
		cldSuperclass.setClassName("java.lang.ClassLoader");
		cldSuperclass.setMethodName("loadClass");
		cldSuperclass.setParameters(Collections.singletonList("java.lang.String"));
		cldSuperclass.setPublicModifier(true);
		cldSuperclass.setSuperclass(true);

		classLoadingDelegationAssignments = Arrays.asList(cldDirect, cldSuperclass);
	}

}
