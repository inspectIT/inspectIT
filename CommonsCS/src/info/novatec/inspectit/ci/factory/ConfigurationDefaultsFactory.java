package info.novatec.inspectit.ci.factory;

import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionMetaDataSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.HttpSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.Log4jLoggingSensorConfig;
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
import java.util.List;

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
		methodSensorConfigs.add(new Log4jLoggingSensorConfig());
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

}
