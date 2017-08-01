package rocks.inspectit.shared.cs.ci.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.CloseableHttpAsyncClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.EUMInstrumentationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ExecutorIntercepterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.HttpClientBuilderSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;

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
	 * Assignments for the http client builder.
	 */
	private Collection<SpecialMethodSensorAssignment> httpClientBuilderAssignments;

	/**
	 * Assignments for the closeable http async client.
	 */
	private Collection<SpecialMethodSensorAssignment> closeableHttpAsyncClientAssignments;

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

		assignments.addAll(executorInterceptorAssignments);

		assignments.addAll(httpClientBuilderAssignments);
		assignments.addAll(closeableHttpAsyncClientAssignments);

		return assignments;
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

		SpecialMethodSensorAssignment httpClientBuilderAddInterceptorInstr = new SpecialMethodSensorAssignment(HttpClientBuilderSensorConfig.INSTANCE);
		httpClientBuilderAddInterceptorInstr.setClassName("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");
		httpClientBuilderAddInterceptorInstr.setMethodName("build");

		httpClientBuilderAssignments = Arrays.asList(httpClientBuilderAddInterceptorInstr);

		SpecialMethodSensorAssignment closeableHttpAsyncClientInstr = new SpecialMethodSensorAssignment(CloseableHttpAsyncClientSensorConfig.INSTANCE);
		closeableHttpAsyncClientInstr.setClassName("org.apache.http.impl.nio.client.CloseableHttpAsyncClient");
		closeableHttpAsyncClientInstr.setMethodName("execute");
		closeableHttpAsyncClientInstr
		.setParameters(Arrays.asList("org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback"));

		closeableHttpAsyncClientAssignments = Arrays.asList(closeableHttpAsyncClientInstr);
	}

}
