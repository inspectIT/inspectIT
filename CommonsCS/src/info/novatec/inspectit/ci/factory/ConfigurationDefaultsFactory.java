package info.novatec.inspectit.ci.factory;

import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionMetaDataSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.HttpSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.StatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.TimerSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.CompilationSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.CpuSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.MemorySensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.RuntimeSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.SystemSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.ThreadSensorConfig;
import info.novatec.inspectit.ci.strategy.IStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.SimpleBufferStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.TimeSendingStrategyConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defaults factory defines what is default for our configuration.
 * 
 * @author Ivan Senic
 * 
 */
public final class ConfigurationDefaultsFactory {

	/**
	 * Private factory.
	 */
	private ConfigurationDefaultsFactory() {
	}

	/**
	 * Returns default sending strategy, that's {@link TimeSendingStrategyConfig}.
	 * 
	 * @return Returns default sending strategy.
	 */
	public static IStrategyConfig getDefaultSendingStrategy() {
		return new TimeSendingStrategyConfig();
	}

	/**
	 * Returns default buffer strategy. That's {@link SimpleBufferStrategyConfig}.
	 * 
	 * @return Returns default buffer strategy.
	 */
	public static IStrategyConfig getDefaultBufferStrategy() {
		return new SimpleBufferStrategyConfig();
	}

	/**
	 * Returns all available {@link IPlatformSensorConfig}s.
	 * 
	 * @return Returns all available {@link IPlatformSensorConfig}s.
	 */
	public static List<IPlatformSensorConfig> getAvailablePlatformSensorConfigs() {
		List<IPlatformSensorConfig> platformSensorConfigs = new ArrayList<>();
		platformSensorConfigs.add(new ClassLoadingSensorConfig());
		platformSensorConfigs.add(new CompilationSensorConfig());
		platformSensorConfigs.add(new CpuSensorConfig());
		platformSensorConfigs.add(new MemorySensorConfig());
		platformSensorConfigs.add(new RuntimeSensorConfig());
		platformSensorConfigs.add(new SystemSensorConfig());
		platformSensorConfigs.add(new ThreadSensorConfig());
		return platformSensorConfigs;
	}

	/**
	 * Returns all available {@link IMethodSensorConfig}s.
	 * 
	 * @return Returns all available {@link IMethodSensorConfig}s.
	 */
	public static List<IMethodSensorConfig> getAvailableMethodSensorConfigs() {
		List<IMethodSensorConfig> methodSensorConfigs = new ArrayList<>();
		methodSensorConfigs.add(new ConnectionMetaDataSensorConfig());
		methodSensorConfigs.add(new ConnectionSensorConfig());
		methodSensorConfigs.add(new HttpSensorConfig());
		methodSensorConfigs.add(new InvocationSequenceSensorConfig());
		methodSensorConfigs.add(new PreparedStatementParameterSensorConfig());
		methodSensorConfigs.add(new PreparedStatementSensorConfig());
		methodSensorConfigs.add(new StatementSensorConfig());
		methodSensorConfigs.add(new TimerSensorConfig());
		return methodSensorConfigs;
	}

	/**
	 * Returns default {@link IExceptionSensorConfig}. That's {@link ExceptionSensorConfig}.
	 * 
	 * @return Returns default {@link IExceptionSensorConfig}.
	 */
	public static IExceptionSensorConfig getDefaultExceptionSensorConfig() {
		return new ExceptionSensorConfig();
	}

	/**
	 * Returns set of the default exclude rules.
	 * 
	 * @return Returns set of the default exclude rules.
	 */
	public static Set<ExcludeRule> getDefaultExcludeRules() {
		Set<ExcludeRule> excludeRules = new HashSet<>();
		excludeRules.add(new ExcludeRule("info.novatec.inspectit.*"));
		excludeRules.add(new ExcludeRule("$Proxy*"));
		excludeRules.add(new ExcludeRule("com.sun.*$Proxy*"));
		excludeRules.add(new ExcludeRule("sun.*"));
		excludeRules.add(new ExcludeRule("java.lang.ThreadLocal"));
		excludeRules.add(new ExcludeRule("java.lang.ref.Reference"));
		excludeRules.add(new ExcludeRule("*_WLStub"));
		excludeRules.add(new ExcludeRule("*[]"));
		return excludeRules;
	}
}
