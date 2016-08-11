package rocks.inspectit.shared.cs.ci.factory;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpUrlConnectionInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQConsumerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQListenerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.SimpleBufferStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.TimeSendingStrategyConfig;

/**
 * Defaults factory defines what is default for our configuration.
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
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
		methodSensorConfigs.add(new ConnectionSensorConfig());
		methodSensorConfigs.add(new HttpSensorConfig());
		methodSensorConfigs.add(new InvocationSequenceSensorConfig());
		methodSensorConfigs.add(new PreparedStatementParameterSensorConfig());
		methodSensorConfigs.add(new PreparedStatementSensorConfig());
		methodSensorConfigs.add(new StatementSensorConfig());
		methodSensorConfigs.add(new TimerSensorConfig());
		methodSensorConfigs.add(new Log4jLoggingSensorConfig());
		methodSensorConfigs.add(new RemoteApacheHttpClientV40InserterSensorConfig());
		methodSensorConfigs.add(new RemoteHttpExtractorSensorConfig());
		methodSensorConfigs.add(new RemoteHttpUrlConnectionInserterSensorConfig());
		methodSensorConfigs.add(new RemoteJettyHttpClientV61InserterSensorConfig());
		methodSensorConfigs.add(new RemoteMQConsumerExtractorSensorConfig());
		methodSensorConfigs.add(new RemoteMQInserterSensorConfig());
		methodSensorConfigs.add(new RemoteMQListenerExtractorSensorConfig());
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
	 * Returns default {@link JmxAttributeDescriptor}.
	 *
	 * @return Returns default {@link JmxAttributeDescriptor}.
	 */
	public static JmxSensorConfig getDefaultJmxSensorConfig() {
		return new JmxSensorConfig();
	}

}
