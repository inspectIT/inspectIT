package rocks.inspectit.shared.cs.ci.factory;

import java.util.ArrayList;
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

	private Collection<SpecialMethodSensorAssignment> endUserMonitoringAssignments;

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
		ArrayList<SpecialMethodSensorAssignment> result = new ArrayList<>();
		if (environment.isClassLoadingDelegation()) {
			result.addAll(classLoadingDelegationAssignments);
		}
		if (environment.getEumConfig().isEumEnabled()) {
			result.addAll(endUserMonitoringAssignments);
		}
		return result;
	}

	/**
	 * Initializes the assignments.
	 */
	@PostConstruct
	public void init() {
		// init all assignments
		buildClassLoaderDelegationAssignments();
		buildEndUserMonitoringAssignments();
	}

	private void buildEndUserMonitoringAssignments() {
		SpecialMethodSensorAssignment eumFilterInstr = new SpecialMethodSensorAssignment(SpecialInstrumentationType.EUM_SERVLET_OR_FILTER_INSPECTION);
		eumFilterInstr.setClassName("javax.servlet.Filter");
		eumFilterInstr.setInterf(true);
		eumFilterInstr.setMethodName("doFilter");
		eumFilterInstr.setParameters(Arrays.asList("javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.FilterChain"));

		SpecialMethodSensorAssignment eumServletInstr = new SpecialMethodSensorAssignment(SpecialInstrumentationType.EUM_SERVLET_OR_FILTER_INSPECTION);
		eumServletInstr.setClassName("javax.servlet.Servlet");
		eumServletInstr.setInterf(true);
		eumServletInstr.setMethodName("service");
		eumServletInstr.setParameters(Arrays.asList("javax.servlet.ServletRequest", "javax.servlet.ServletResponse"));
		endUserMonitoringAssignments = Arrays.asList(eumFilterInstr, eumServletInstr);
	}

	/**
	 *
	 */
	private void buildClassLoaderDelegationAssignments() {
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
