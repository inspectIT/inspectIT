package info.novatec.inspectit.ci;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.context.AbstractContextCapture;
import info.novatec.inspectit.ci.context.impl.FieldContextCapture;
import info.novatec.inspectit.ci.context.impl.ParameterContextCapture;
import info.novatec.inspectit.ci.context.impl.ReturnContextCapture;
import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementSensorConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

/**
 * Only for testing purposes.
 * 
 * @author Ivan Senic
 * 
 */
// NOCHKALL
@SuppressWarnings("PMD")
public final class CiDataFactory {

	private final static String[] NAMES = new String[] { "Test", "Dev", "Production", "My", "Yours", "First", "Second" };

	private final static String[] CLASSES = new String[] { "*", "com.oracle.*", "my.concrete.Class", "*.spring.*", "your.concrete.Class" };

	private final static String[] METHODS = new String[] { "*", "execute", "perform*", "toString" };

	private final static String[] PARAMETERS = new String[] { "int", "long", "boolean", "java.util.String" };

	public static List<Profile> getProfiles(int size) {
		if (size == 0) {
			return null;
		}

		List<Profile> profiles = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			profiles.add(getProfile());
		}
		return profiles;
	}

	public static Profile getProfile() {
		Profile profile = new Profile();
		profile.setName(getRandomNamePrefix() + " Profile");
		profile.setDescription(RandomStringUtils.randomAlphabetic(30));
		profile.setMethodSensorAssignments(getMethodSensorAssignment(RandomUtils.nextInt(5)));
		profile.setExceptionSensorAssignments(getExceptionSensorAssignments(RandomUtils.nextInt(5)));
		profile.setExcludeRules(Collections.singletonList(getExcludeRule()));
		return profile;
	}

	public static List<Environment> getEnvironments(int size) {
		if (size == 0) {
			return null;
		}

		List<Environment> environment = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			environment.add(getEnvironment());
		}
		return environment;
	}

	public static Environment getEnvironment() {
		Environment environment = new Environment();
		environment.setName(getRandomNamePrefix() + " Environment");
		// environment.setProfiles(getProfiles(RandomUtils.nextInt(5)));
		return environment;
	}

	public static List<AgentMapping> getAgentMappings(int size) {
		if (size == 0) {
			return null;
		}

		return getAgentMappings(size, getEnvironments(RandomUtils.nextInt(5)));
	}

	public static List<AgentMapping> getAgentMappings(int size, List<Environment> environments) {
		List<AgentMapping> agentMappings = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			agentMappings.add(getAgentMapping(environments));
		}
		return agentMappings;
	}

	public static AgentMapping getAgentMapping() {
		return getAgentMapping(getEnvironments(RandomUtils.nextInt(5)));
	}

	public static AgentMapping getAgentMapping(List<Environment> environments) {
		AgentMapping mapping = new AgentMapping();
		mapping.setActive(RandomUtils.nextBoolean());
		mapping.setAgentName(RandomStringUtils.randomAlphabetic(20));
		mapping.setIpAddress(RandomStringUtils.randomNumeric(16));
		return mapping;
	}

	public static List<MethodSensorAssignment> getMethodSensorAssignment(int size) {
		if (size == 0) {
			return null;
		}

		List<MethodSensorAssignment> assignments = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			if (RandomUtils.nextBoolean()) {
				assignments.add(getSqlMethodSensorAssignment());
			} else {
				assignments.add(getTimerMethodSensorAssignment());
			}
		}
		return assignments;
	}

	public static List<ExceptionSensorAssignment> getExceptionSensorAssignments(int size) {
		if (size == 0) {
			return null;
		}

		List<ExceptionSensorAssignment> assignments = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			assignments.add(getExceptionSensorAssignment());
		}
		return assignments;
	}

	public static ExceptionSensorAssignment getExceptionSensorAssignment() {
		ExceptionSensorAssignment assignment = new ExceptionSensorAssignment();
		populateClassSensorAssignment(assignment);
		return assignment;
	}

	public static MethodSensorAssignment getSqlMethodSensorAssignment() {
		MethodSensorAssignment assignment = new MethodSensorAssignment(PreparedStatementSensorConfig.class);
		populateMethodSensorAssignment(assignment);
		return assignment;
	}

	public static ExcludeRule getExcludeRule() {
		ExcludeRule rule = new ExcludeRule();
		rule.setClassName(getRandomClass());
		return rule;
	}

	public static TimerMethodSensorAssignment getTimerMethodSensorAssignment() {
		TimerMethodSensorAssignment assignment = new TimerMethodSensorAssignment();
		populateMethodSensorAssignment(assignment);
		List<AbstractContextCapture> contextCaptures = new ArrayList<>();
		if (RandomUtils.nextBoolean()) {
			ReturnContextCapture returnContextCapture = new ReturnContextCapture();
			returnContextCapture.setDisplayName("myReturn");
			contextCaptures.add(returnContextCapture);
		}
		if (RandomUtils.nextBoolean() && CollectionUtils.isNotEmpty(assignment.getParameters())) {
			ParameterContextCapture parameterContextCapture = new ParameterContextCapture();
			parameterContextCapture.setDisplayName("myParam");
			parameterContextCapture.setIndex(0);
			List<String> paths = new ArrayList<>();
			paths.add("user");
			paths.add("surname");
			parameterContextCapture.setPaths(paths);
			contextCaptures.add(parameterContextCapture);
		}
		if (RandomUtils.nextBoolean()) {
			FieldContextCapture fieldContextCapture = new FieldContextCapture();
			fieldContextCapture.setDisplayName("myField");
			fieldContextCapture.setFieldName("message");
		}
		assignment.setCharting(RandomUtils.nextBoolean());
		if (CollectionUtils.isNotEmpty(contextCaptures)) {
			assignment.setContextCaptures(contextCaptures);
		}
		return assignment;
	}

	private static void populateMethodSensorAssignment(MethodSensorAssignment assignment) {
		populateClassSensorAssignment(assignment);
		if (RandomUtils.nextBoolean()) {
			assignment.setConstructor(true);
		} else {
			assignment.setMethodName(getRandomMethod());
		}
		if (RandomUtils.nextBoolean()) {
			assignment.setParameters(getRandomParameters());
		}
		if (RandomUtils.nextBoolean()) {
			if (RandomUtils.nextBoolean()) {
				assignment.setPublicModifier(true);
			}
			if (RandomUtils.nextBoolean()) {
				assignment.setProtectedModifier(true);
			}
			if (RandomUtils.nextBoolean()) {
				assignment.setPrivateModifier(true);
			}
			if (RandomUtils.nextBoolean()) {
				assignment.setDefaultModifier(true);
			}
		}
	}

	private static void populateClassSensorAssignment(AbstractClassSensorAssignment<?> assignment) {
		assignment.setClassName(getRandomClass());
		if (RandomUtils.nextBoolean()) {
			assignment.setInterf(true);
		} else if (RandomUtils.nextBoolean()) {
			assignment.setSuperclass(true);
		}
		if (RandomUtils.nextBoolean()) {
			assignment.setAnnotation("javax.Service");
		}
	}

	private static String getRandomNamePrefix() {
		return NAMES[RandomUtils.nextInt(NAMES.length)];
	}

	private static String getRandomClass() {
		return CLASSES[RandomUtils.nextInt(CLASSES.length)];
	}

	private static String getRandomMethod() {
		return METHODS[RandomUtils.nextInt(METHODS.length)];
	}

	private static List<String> getRandomParameters() {
		List<String> params = new ArrayList<>();
		int s = RandomUtils.nextInt(PARAMETERS.length);
		for (int i = 0; i < s; i++) {
			String p = PARAMETERS[RandomUtils.nextInt(PARAMETERS.length)];
			if (!params.contains(p)) {
				params.add(p);
			}
		}
		return params;
	}
}
