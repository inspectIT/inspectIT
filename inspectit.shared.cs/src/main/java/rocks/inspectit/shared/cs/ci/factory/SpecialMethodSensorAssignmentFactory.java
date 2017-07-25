package rocks.inspectit.shared.cs.ci.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ExecutorClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.EUMInstrumentationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ExecutorIntercepterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

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
	 * The logger.
	 */
	@Log
	private Logger log;

	/**
	 * The configuration interface service.
	 */
	@Autowired
	private IConfigurationInterfaceService ciService;

	/**
	 * Assignments for the class loading delegation.
	 */
	private Collection<SpecialMethodSensorAssignment> classLoadingDelegationAssignments;

	/**
	 * Assignment for the MBean server factory.
	 */
	private Collection<SpecialMethodSensorAssignment> mbeanServerFactoryAssignments;

	/**
	 * Assignments for the end user monitoring.
	 */
	private Collection<SpecialMethodSensorAssignment> endUserMonitoringAssignments;

	/**
	 * Assignments for the executor correlation.
	 */
	private Collection<SpecialMethodSensorAssignment> executorInterceptorAssignments;

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
		Collection<SpecialMethodSensorAssignment> assignments = new ArrayList<>(0);
		if (environment.isClassLoadingDelegation()) {
			assignments.addAll(classLoadingDelegationAssignments);
		}

		if (isJmxSensorActive(environment)) {
			assignments.addAll(mbeanServerFactoryAssignments);
		}

		if ((environment.getEumConfig() != null) && environment.getEumConfig().isEumEnabled()) {
			assignments.addAll(endUserMonitoringAssignments);
		}

		if (hasExecutorClientSensor(environment)) {
			assignments.addAll(executorInterceptorAssignments);
		}

		return assignments;
	}

	/**
	 * Returns whether the given environment contains an {@link ExecutorClientSensorConfig}.
	 *
	 * @param environment
	 *            the environment to check
	 * @return returns <code>true</code> if an {@link ExecutorClientSensorConfig} exists
	 */
	private boolean hasExecutorClientSensor(Environment environment) {
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = ciService.getProfile(profileId);

				if (!profile.isActive()) {
					continue;
				}

				// all assignments
				AbstractProfileData<?> profileData = profile.getProfileData();
				if (SensorAssignmentProfileData.class.isInstance(profileData)) {
					SensorAssignmentProfileData sapData = (SensorAssignmentProfileData) profileData;

					List<MethodSensorAssignment> sensorAssignments = sapData.getMethodSensorAssignments();

					for (MethodSensorAssignment assignment : sensorAssignments) {
						if (assignment.getSensorConfigClass().isAssignableFrom(ExecutorClientSensorConfig.class)) {
							return true;
						}
					}
				}
			} catch (Exception x) {
				if (log.isDebugEnabled()) {
					log.debug("Profile with id '{}' could not be loaded.", profileId);
				}
			}
		}
		return false;
	}

	/**
	 * Checks if Jmx sensor is active on the environment.
	 *
	 * @param environment
	 *            {@link Environment}
	 * @return <code>true</code> if jmx sensor is not <code>null</code> in environment and it's
	 *         active
	 */
	private boolean isJmxSensorActive(Environment environment) {
		return (null != environment.getJmxSensorConfig()) && environment.getJmxSensorConfig().isActive();
	}

	/**
	 * Initializes the assignments.
	 */
	@PostConstruct
	void init() {
		// init all assignments
		// class loading delegation
		SpecialMethodSensorAssignment cldDirect = new SpecialMethodSensorAssignment(ClassLoadingDelegationSensorConfig.INSTANCE);
		cldDirect.setClassName("java.lang.ClassLoader");
		cldDirect.setMethodName("loadClass");
		cldDirect.setParameters(Collections.singletonList("java.lang.String"));
		cldDirect.setPublicModifier(true);

		SpecialMethodSensorAssignment cldSuperclass = new SpecialMethodSensorAssignment(ClassLoadingDelegationSensorConfig.INSTANCE);
		cldSuperclass.setClassName("java.lang.ClassLoader");
		cldSuperclass.setMethodName("loadClass");
		cldSuperclass.setParameters(Collections.singletonList("java.lang.String"));
		cldSuperclass.setPublicModifier(true);
		cldSuperclass.setSuperclass(true);

		classLoadingDelegationAssignments = Arrays.asList(cldDirect, cldSuperclass);

		// mbean server
		SpecialMethodSensorAssignment msbAdd = new SpecialMethodSensorAssignment(MBeanServerInterceptorSensorConfig.INSTANCE);
		msbAdd.setClassName("javax.management.MBeanServerFactory");
		msbAdd.setMethodName("addMBeanServer");
		msbAdd.setParameters(Collections.singletonList("javax.management.MBeanServer"));
		msbAdd.setPrivateModifier(true);

		SpecialMethodSensorAssignment msbRemove = new SpecialMethodSensorAssignment(MBeanServerInterceptorSensorConfig.INSTANCE);
		msbRemove.setClassName("javax.management.MBeanServerFactory");
		msbRemove.setMethodName("removeMBeanServer");
		msbRemove.setParameters(Collections.singletonList("javax.management.MBeanServer"));
		msbRemove.setPrivateModifier(true);

		mbeanServerFactoryAssignments = Arrays.asList(msbAdd, msbRemove);

		// EUM
		SpecialMethodSensorAssignment eumFilterInstr = new SpecialMethodSensorAssignment(EUMInstrumentationSensorConfig.INSTANCE);
		eumFilterInstr.setClassName("javax.servlet.Filter");
		eumFilterInstr.setInterf(true);
		eumFilterInstr.setMethodName("doFilter");
		eumFilterInstr.setParameters(Arrays.asList("javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.FilterChain"));

		SpecialMethodSensorAssignment eumServletInstr = new SpecialMethodSensorAssignment(EUMInstrumentationSensorConfig.INSTANCE);
		eumServletInstr.setClassName("javax.servlet.Servlet");
		eumServletInstr.setInterf(true);
		eumServletInstr.setMethodName("service");
		eumServletInstr.setParameters(Arrays.asList("javax.servlet.ServletRequest", "javax.servlet.ServletResponse"));

		endUserMonitoringAssignments = Arrays.asList(eumFilterInstr, eumServletInstr);

		// executor clients
		SpecialMethodSensorAssignment execSubmitRunnableInstr = new SpecialMethodSensorAssignment(ExecutorIntercepterSensorConfig.INSTANCE);
		execSubmitRunnableInstr.setClassName("java.util.concurrent.Executor");
		execSubmitRunnableInstr.setInterf(true);
		execSubmitRunnableInstr.setMethodName("execute");
		execSubmitRunnableInstr.setParameters(Arrays.asList("java.lang.Runnable"));

		executorInterceptorAssignments = Arrays.asList(execSubmitRunnableInstr);
	}

}
